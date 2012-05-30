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
 * Class GIF image encryptor using the method of extended palette.
 * 
 * @author e.shishkin
 *
 */
public class GIFEncryptorByPaletteExtensionMethod implements IEncryptor {
	
	protected byte[] checkSequence = new byte[]{0,1,0,1,0,1,0,1};
	
	@Override
	public void encrypt(File in, File out, byte[] data) throws UnableToEncodeException, IOException, NullPointerException {
		if (in == null) {
			throw new NullPointerException("Input file is null");
		}
		if (out == null) {
			throw new NullPointerException("Output file is null");
		}
		if (data == null) {
			throw new NullPointerException("Text is null");
		}
		
		// read bytes from file
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
		
		if (bsize == 7) {
			throw new UnableToEncodeException("Palette size is maximum (256 colors). There is no free space");
		}
		
		// calculate original color count
		int bOrigColorCount = (int)Math.pow(2, bsize+1);
		
		// calculate new palette size to contain all message
		int newBsize = bsize;
		int possibleMessageLength = (int)(Math.pow(2, newBsize+1) - bOrigColorCount)*3;
		int possibleTextLength = possibleMessageLength-3;	// one byte for check and two bytes for message length
		while ( (newBsize < 7) && (possibleTextLength < data.length) ) {
			newBsize++;
			possibleMessageLength = (int)(Math.pow(2, newBsize+1) - bOrigColorCount)*3;
			possibleTextLength = possibleMessageLength-3;
		}
		
		if (possibleTextLength < data.length) {		
			throw new UnableToEncodeException("Text is too big. Max text lenght for this image is " + possibleTextLength);
		}
		
		// set new palette property to the 10-th byte in the file
		byte[] newBsizeBits = Binary.toBitArray((byte)newBsize);
		b10[0] = newBsizeBits[0];
		b10[1] = newBsizeBits[1];
		b10[2] = newBsizeBits[2];
		bytes[10] = Binary.toByte(b10);
		
		// create message array
		byte[] messageArray = new byte[possibleMessageLength];
		
		// create bit array from text length value and divide it into two arrays
		byte[] lengthBytes = Binary.toBitArray((short)data.length);
		byte[] lowTextLengthByte = Binary.subarray(lengthBytes, 0, 8);
		byte[] highTextLengthByte = Binary.subarray(lengthBytes, 8, 8);
		
		// write bytes of check sequence and of two parts of message length
		messageArray[possibleMessageLength-1] = Binary.toByte(checkSequence);
		messageArray[possibleMessageLength-2] = Binary.toByte(highTextLengthByte);
		messageArray[possibleMessageLength-3] = Binary.toByte(lowTextLengthByte);
		
		// write text bytes
		byte[] textBytes = data;
		for (int i = 0; i < data.length; i++) {
			messageArray[messageArray.length-i-4] = textBytes[i];
		}
		
		// write output file
		OutputStream os = new FileOutputStream(out);
		for (int i = 0; i < 13+3*bOrigColorCount; i++) {
			os.write(bytes[i]);
		}
		os.write(messageArray);
		for (int i = 13+3*bOrigColorCount; i < bytes.length; i++) {
			os.write(bytes[i]);
		}
		os.close();
	}
	
	@Override
	public byte[] decrypt(File in) throws UnableToDecodeException, IOException, NullPointerException {
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
		int possibleMessageLength = (int)Math.pow(2, bsize+1)*3;
		int possibleTextLength = possibleMessageLength-3;
		
		int n = 13;
		
		// read check sequence
		byte cs = bytes[n + bOrigColorCount*3 - 1];
		if (cs != Binary.toByte(checkSequence)) {
			throw new UnableToDecodeException("There is no encrypted message in the image (Check sequence is incorrect)");
		}
		
		// read two text length bytes and split them to one
		byte highTextLengthByte = bytes[n + bOrigColorCount*3 - 2];
		byte lowTextLengthByte = bytes[n + bOrigColorCount*3 - 3];
		short textLength = Binary.splitBytes(highTextLengthByte, lowTextLengthByte);
		
		if (textLength < 0) {
			throw new UnableToDecodeException("Decoded text length is less than 0");
		}
		if (possibleTextLength < textLength) {
			throw new UnableToDecodeException("There is no encrypted message (Decoded message length (" + textLength + ") is less than Possible message length (" + possibleTextLength + "))");
		}
		
		// read text bytes
		byte[] bt = new byte[textLength];
		for (int i = 0; i < bt.length; i++) {
			bt[i] = bytes[n + bOrigColorCount*3-1 - i - 3];
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
		
		int possibleMessageLength = (int)(256 - Math.pow(2, bsize))*3;
		int possibleTextLength = possibleMessageLength-3;	// one byte for check and two bytes for message length
		
		return new EncryptingFileParameters(possibleTextLength);
	}
	
}
