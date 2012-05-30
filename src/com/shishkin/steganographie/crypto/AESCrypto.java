package com.shishkin.steganographie.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * The implementation of synchronous encryption.
 * (Ex. http://stackoverflow.com/questions/6788018/android-encryption-decryption-with-aes)
 * 
 * @author e.shishkin
 * 
 */
public class AESCrypto implements ICrypto {
	private static final String LOG_TAG = AESCrypto.class.getSimpleName();

	@Override
	public byte[] encrypt(byte[] data, String key) throws IOException,
			GeneralSecurityException {
		Log.v(LOG_TAG, "encrypt() called");
		// initialize cipher
		SecretKeySpec skeySpec = new SecretKeySpec(getKey(key), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(data);
		// return crypt(data, cipher);
		return encrypted;
	}

	@Override
	public byte[] decrypt(byte[] data, String key) throws IOException,
			GeneralSecurityException {
		Log.v(LOG_TAG, "encrypt() called");
		// initialize cipher
		SecretKeySpec skeySpec = new SecretKeySpec(getKey(key), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(data);
		// return crypt(data, cipher);
		return decrypted;
	}

	/**
	 * Convert string key to byte array key.
	 * 
	 * @param keyStr
	 * @return byte array key
	 * @throws GeneralSecurityException
	 */
	private byte[] getKey(String keyStr) throws GeneralSecurityException {
		Log.v(LOG_TAG, "getKey() called: keyStr=" + keyStr);
		// addition current key to 16 bytes
		byte[] key = new byte[16];
		byte[] passwordByteArray = keyStr.getBytes();
		for (int i = 0; i < passwordByteArray.length; i++) {
			key[i] = passwordByteArray[i];
		}
		return key;
	}

	/**
	 * Data encryption/decryption.
	 * 
	 * @param data
	 * @param cipher
	 * @return resulting byte array
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@Deprecated
	private byte[] crypt(byte[] data, Cipher cipher) throws IOException, GeneralSecurityException {
		Log.v(LOG_TAG, "crypt() called");
		
		// init data
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];
		
		// create input & output stream
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int inLength = 0;
		boolean hasNextBlock = true;
		try {
			// crypt input data
			while (hasNextBlock) {
				inLength = in.read(inBytes);
				if (inLength == blockSize) {
					int outLength = cipher.update(inBytes, 0, blockSize,
							outBytes);
					out.write(outBytes, 0, outLength);
				} else {
					hasNextBlock = false;
				}
			}
			// finishes a multi-part transformation (encryption or decryption)
			if (inLength > 0) {
				outBytes = cipher.doFinal(inBytes, 0, inLength);
			} else {
				outBytes = cipher.doFinal();
			}
			out.write(outBytes);
		} finally {
			in.close();
			out.close();
		}

		return out.toByteArray();
	}

}
