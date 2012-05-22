package com.shishkin.steganographie;

/**
 * 
 * @author e.shishkin
 *
 */
public class UnableToDecodeException extends Exception {
	
	private static final long serialVersionUID = 24L;
	
	public UnableToDecodeException() {
		super();
	}
	
	public UnableToDecodeException(String message) {
		super(message);
	}
	
}
