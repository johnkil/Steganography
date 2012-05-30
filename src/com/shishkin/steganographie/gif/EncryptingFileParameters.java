package com.shishkin.steganographie.gif;


/**
 * 
 * @author e.shishkin
 *
 */
public class EncryptingFileParameters {
	
	private int possibleTextLength;
	
	/**
	 * Default constructor.
	 * 
	 * @param possibleTextLength
	 */
	public EncryptingFileParameters(int possibleTextLength) {
		this.possibleTextLength = possibleTextLength;
	}
	
	/**
	 * Receive possible text length.
	 * 
	 * @return possibleTextLength
	 */
	public int getPossibleTextLength() {
		return possibleTextLength;
	}
	
	
}
