package com.shishkin.steganographie;

import java.io.File;
import java.io.IOException;

/**
 * Public Steganography encryptor interface 
 * for encrypt/decrypt of the message in picture.
 * 
 * @author e.shishkin
 *
 */
public interface IEncryptor {
	
	/**
	 * Encrypt of the message in picture.
	 * 
	 * @param in - source image
	 * @param out - encrypted image
	 * @param text - source message
	 * 
	 * @throws UnableToEncodeException
	 * @throws IOException
	 */
	public void encrypt(File in, File out, byte[] data) throws UnableToEncodeException, IOException;
	
	/**
	 * Decrypt of the message in picture.
	 * 
	 * @param in - source image
	 * @return encrypted message
	 * 
	 * @throws UnableToDecodeException
	 * @throws IOException
	 */
	public byte[] decrypt(File in) throws UnableToDecodeException, IOException;
	
}
