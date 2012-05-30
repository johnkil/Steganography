package com.shishkin.steganographie.gif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.shishkin.steganographie.Binary;
import com.shishkin.steganographie.IEncryptor;
import com.shishkin.steganographie.UnableToDecodeException;
import com.shishkin.steganographie.UnableToEncodeException;

/**
 * Class GIF image encryptor using the method of Least Significant Bit (LSB).
 * 
 * @author e.shishkin
 *
 */
public class GIFEncryptorByLSBMethod implements IEncryptor {
	
	protected byte[] checkSequence = new byte[]{0,1,0,1,0,1,0,1};
	protected int firstLSBit = 0;
	protected int secondLSBit = 1;
	
	@Override
	public void encrypt(File in, File out, byte[] data) throws UnableToEncodeException, NullPointerException, IOException {
		if (in == null) {
			throw new NullPointerException("Input file is null");
		}
		if (out == null) {
			throw new NullPointerException("Output file is null");
		}
		if (data == null) {
			throw new NullPointerException("Text is null");
		}
		
		// read bytes from input file
		byte[] bytes = new byte[(int)in.length()];
		InputStream is = new FileInputStream(in);
		is.read(bytes);
		is.close();
		
		// check format
		if (!(new String(bytes, 0, 6)).equals("GIF89a")) {
			throw new UnableToEncodeException("Input file has wrong GIF format");
		}
		
		// read palette size property from first three bits in the 10-th byte from the file
		byte[] b10 = Binary.toBitArray(bytes[10]);
		byte bsize = Binary.toByte(new byte[] {b10[0], b10[1], b10[2]});
		
		// calculate color count and possible message length
		int bOrigColorCount = (int)Math.pow(2, bsize+1);
		int possibleMessageLength = bOrigColorCount*3/4;
		int possibleTextLength = possibleMessageLength-2;// one byte for check and one byte for message length
		
		if (possibleTextLength < data.length) {
			throw new UnableToEncodeException("Text is too big");
		}
		
		int n = 13;
		
		// write check sequence
		for (int i = 0; i < checkSequence.length/2; i++) {
			byte[] ba = Binary.toBitArray(bytes[n]);
			ba[firstLSBit] = checkSequence[2*i];
			ba[secondLSBit] = checkSequence[2*i+1];
			bytes[n] = Binary.toByte(ba);
			n++;
		}
		
		// write text length
		byte[] cl = Binary.toBitArray((byte)data.length);
		for (int i = 0; i < cl.length/2; i++) {
			byte[] ba = Binary.toBitArray(bytes[n]);
			ba[firstLSBit] = cl[2*i];
			ba[secondLSBit] = cl[2*i+1];
			bytes[n] = Binary.toByte(ba);
			n++;
		}
		
		// write message
		byte[] textBytes = data;
		for (int i = 0; i < textBytes.length; i++) {
			byte[] c = Binary.toBitArray(textBytes[i]);
			for (int ci = 0; ci < c.length/2; ci++) {
				byte[] ba = Binary.toBitArray(bytes[n]);
				ba[firstLSBit] = c[2*ci];
				ba[secondLSBit] = c[2*ci+1];
				bytes[n] = Binary.toByte(ba);
				n++;
			}
		}
		
		// write output file
		OutputStream os = new FileOutputStream(out);
		os.write(bytes);
		os.close();
	}
	
	@Override
	public byte[] decrypt(File in) throws UnableToDecodeException, NullPointerException, IOException {
		if (in == null) {
			throw new NullPointerException("Input file is null");
		}
		
		// read bytes from input file
		byte[] bytes = new byte[(int)in.length()];
		InputStream is = new FileInputStream(in);
		is.read(bytes);
		is.close();
		
		// check format
		if (!(new String(bytes, 0, 6)).equals("GIF89a")) {
			throw new UnableToDecodeException("Input file has wrong GIF format");
		}
		
		// read palette size property from first three bits in the 10-th byte from the file
		byte[] b10 = Binary.toBitArray(bytes[10]);
		byte bsize = Binary.toByte(new byte[] {b10[0], b10[1], b10[2]});
		
		// calculate color count and possible message length
		int bOrigColorCount = (int)Math.pow(2, bsize+1);
		int possibleMessageLength = bOrigColorCount*3/4;
		int possibleTextLength = possibleMessageLength-2;	// one byte for check and one byte for message length
		
		int n = 13;
		
		// read check sequence
		byte[] csBits = new byte[checkSequence.length];
		for (int i = 0; i < 4; i++) {
			byte[] ba = Binary.toBitArray(bytes[n]);
			csBits[2*i] = ba[firstLSBit];
			csBits[2*i+1] = ba[secondLSBit];
			n++;
		}
		byte cs = Binary.toByte(csBits);
		
		if (cs != Binary.toByte(checkSequence)) {
			throw new UnableToDecodeException("There is no encrypted message in the image (Check sequence is incorrect)");
		}
		
		// read text length
		byte[] cl = new byte[8];
		for (int i = 0; i < 4; i++) {
			byte[] ba = Binary.toBitArray(bytes[n]);
			cl[2*i] = ba[firstLSBit];
			cl[2*i+1] = ba[secondLSBit];
			n++;
		}
		byte textLength = Binary.toByte(cl);
		
		if (textLength < 0) {
			throw new UnableToDecodeException("Decoded text length is less than 0");
		}
		if (possibleTextLength < textLength) {
			throw new UnableToDecodeException("There is no messages (Decoded message length (" + textLength + ") is less than Possible message length (" + possibleTextLength + "))");
		}
		
		// read text bits and make text bytes
		byte[] bt = new byte[textLength];
		for (int i = 0; i < bt.length; i++) {
			byte[] bc = new byte[8];
			for (int bci = 0; bci < bc.length/2; bci++) {
				byte[] ba = Binary.toBitArray(bytes[n]);
				bc[2*bci] = ba[firstLSBit];
				bc[2*bci+1] = ba[secondLSBit];
				n++;
			}
			bt[i] = Binary.toByte(bc);
		}
		
		return bt;
	}
	
	/**
	 * Calculate encrypting file parameters.
	 * 
	 * @param f - file
	 * @return encrypting file parameters
	 * @throws UnableToEncodeException
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public static EncryptingFileParameters getEncryptingFileParameters(File f) throws UnableToEncodeException, NullPointerException, IOException {
		if (f == null) {
			throw new NullPointerException("Input file is null");
		}
		
		byte[] bytes = new byte[(int)f.length()];
		InputStream is = new FileInputStream(f);
		is.read(bytes);
		is.close();
		
		if (!(new String(bytes, 0, 6)).equals("GIF89a")) {
			throw new UnableToEncodeException("Input file has wrong GIF format");
		}
		
		byte[] b10 = Binary.toBitArray(bytes[10]);
		byte bsize = Binary.toByte(new byte[] {b10[0], b10[1], b10[2]});
		
		int bOrigColorCount = (int)Math.pow(2, bsize+1);
		int possibleMessageLength = bOrigColorCount*3/4;
		int possibleTextLength = possibleMessageLength-2;	// one byte for check and one byte for message length
		
		return new EncryptingFileParameters(possibleTextLength);
	}
	
}
