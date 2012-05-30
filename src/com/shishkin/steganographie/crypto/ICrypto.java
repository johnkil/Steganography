package com.shishkin.steganographie.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * Cryptographer interface.
 * 
 * @author e.shishkin
 *
 */
public interface ICrypto {
	
	/**
	 * Encryption of the source data using a certain key.
	 * 
	 * @param data
	 * @param key
	 * @return encrypted data
	 */
	public byte[] encrypt(byte[] data, String key) throws IOException, GeneralSecurityException;
	
	/**
	 * Decryption of encrypted data using a certain key.
	 * 
	 * @param data
	 * @param key
	 * @return decrypted data
	 */
	public byte[] decrypt(byte[] data, String key) throws IOException, GeneralSecurityException;

}
