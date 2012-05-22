package com.shishkin.steganographie;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author e.shishkin
 *
 */
public interface Encryptor {
	
	public void encrypt(File in, File out, String text) throws UnableToEncodeException, IOException;
	
	public String decrypt(File in) throws UnableToDecodeException, IOException;
	
}
