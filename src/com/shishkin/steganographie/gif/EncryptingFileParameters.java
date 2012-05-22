package com.shishkin.steganographie.gif;


/**
 * 
 * @author e.shishkin
 *
 */
public class EncryptingFileParameters {
	
	public EncryptingFileParameters(int possibleTextLength) {
		this.possibleTextLength = possibleTextLength;
	}
	
	public int getPossibleTextLength() {
		return possibleTextLength;
	}
	
	private int possibleTextLength;
	
}
