package com.shishkin.steganographie;



/**
 * Provides some operations with bit arrays
 * 
 * @author e.shishkin
 *
 */
public final class Binary {
	
	public static byte[] toBitArray(byte value) {
		byte[] bits = new byte[] {0,0,0,0,0,0,0,0};
		if (value < 0) {
			value = (byte)(value + Byte.MAX_VALUE + 1);
			bits[7] = 1;
		}
		
		int i = 0;
		while (true) {
			bits[i] = (byte)(value % 2);
			if ( (value != 1) && (value != 0) ) {
				value /= 2;
				i++;
			} else {
				break;
			}
		}
		bits[i] = value;
		
		return bits;
	}
	
	public static byte[] toBitArray(short value) {
		byte[] bits = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		if (value < 0) {
			value = (short)(value + Short.MAX_VALUE + 1);
			bits[15] = 1;
		}
		
		int i = 0;
		while (true) {
			bits[i] = (byte)(value % 2);
			if ( (value != 1) && (value != 0) ) {
				value /= 2;
				i++;
			} else {
				break;
			}
		}
		bits[i] = (byte)value;
		
		return bits;
	}
	
	public static byte[] toBitArray(int value) {
		byte[] bits = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		if (value < 0) {
			value = (int)(value + Integer.MAX_VALUE + 1);
			bits[31] = 1;
		}
		
		int i = 0;
		while (true) {
			bits[i] = (byte)(value % 2);
			if ( (value != 1) && (value != 0) ) {
				value /= 2;
				i++;
			} else {
				break;
			}
		}
		bits[i] = (byte)value;
		
		return bits;
	}
	
	public static byte toByte(byte[] bitArray) {
		byte res = 0;
		if (bitArray.length > 8) {
			return res;
		}
		
		int r = 0;
		for (int i = 0; i < bitArray.length; i++) {
			r += bitArray[i]*Math.pow(2, i);
		}
		
		res = (byte)r;
		
		return res;
	}
	
	public static short toShort(byte[] bitArray) {
		short res = 0;
		if (bitArray.length > 16) {
			return res;
		}
		
		int r = 0;
		for (int i = 0; i < bitArray.length; i++) {
			r += bitArray[i]*Math.pow(2, i);
		}
		
		res = (short)r;
		
		return res;
	}
	
	public static int toInt(byte[] bitArray) {
		int res = 0;
		if (bitArray.length > 32) {
			return res;
		}
		
		int r = 0;
		for (int i = 0; i < bitArray.length; i++) {
			r += bitArray[i]*Math.pow(2, i);
		}
		
		res = r;
		
		return res;
	}
	
	public static String toString(byte[] bitArray) {
		String res = new String("");
		for (int i = bitArray.length-1; i >= 0; i--) {
			res = res.concat(String.valueOf(bitArray[i]));
		}
		
		return res;
	}
	
	public static byte[] subarray(byte[] src, int off, int len) {
		if (off >= src.length) {
			return null;
		}
		byte[] bits = new byte[len];
		
		for (int i = 0; (i < len) && (off+i < src.length); i++) {
			bits[i] = src[off+i];
		}
		
		return bits;
	}
	
	public static short splitBytes(byte highByte, byte lowByte) {
		byte[] highBits = toBitArray(highByte);
		byte[] lowBits = toBitArray(lowByte);
		
		byte[] bits = new byte[16];
		for (int i = 0; i < lowBits.length; i++) {
			bits[i] = lowBits[i];
		}
		for (int i = 0; i < highBits.length; i++) {
			bits[i+lowBits.length] = highBits[0];
		}
		
		return toShort(bits);
	}
	
}
