package com.shishkin.steganographie.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

/**
 * Class for compressing strings.
 * (Ex. http://stackoverflow.com/questions/6717165/how-can-i-compress-and-decompress-a-string-using-gzipoutputstream-that-is-compat)
 * 
 * @author e.shishkin
 * 
 */
public class StringCompressor implements ICompressor {
	private static final String LOG_TAG = StringCompressor.class.getSimpleName();

	@Override
	public byte[] compress(byte[] data) throws IOException {
		Log.v(LOG_TAG, "compress() called");
		// create ZIP output stream 
		ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
		GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(os));
		try {
			// ñompression data
			zos.write(data);
		} finally {
			zos.close();
		}
		return os.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] compressed) throws IOException {
		Log.v(LOG_TAG, "decompress() called");
		final int BUFFER_SIZE = 32;
		// create input stream from copressed data
		ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(is), BUFFER_SIZE);
		// create output stream for decompressed data
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int bytesRead;
		try {
			// UNZIP compressed data
			while ((bytesRead = zis.read(data)) != -1) {
				out.write(data, 0, bytesRead);
		    }
		} finally {
			zis.close();
			out.close();
		}
		return out.toByteArray();
	}

}
