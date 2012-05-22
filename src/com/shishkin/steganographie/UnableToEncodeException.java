package com.shishkin.steganographie;

/**
 * 
 * @author e.shishkin
 *
 */
public class UnableToEncodeException extends Exception {
	
	private static final long serialVersionUID = 23L;
	
	public UnableToEncodeException() {
		super();
	}
	
	public UnableToEncodeException(String message) {
		super(message);
	}
	
}
