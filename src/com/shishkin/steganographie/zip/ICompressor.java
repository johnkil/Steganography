package com.shishkin.steganographie.zip;

import java.io.IOException;

/**
 * Interface for data compression.
 * 
 * @author e.shishkin
 *
 */
public interface ICompressor {
	
	/**
	 * Compression object.
	 * 
	 * @param data
	 * @return compressed data
	 * @throws IOException
	 */
	public byte[] compress(byte[] data) throws IOException;
	
	/**
	 * Decompression object.
	 * 
	 * @param compressed - compressed data
	 * @return original data
	 * @throws IOException
	 */
	public byte[] decompress(byte[] compressed) throws IOException;

}
