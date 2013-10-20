package com.analyticobjects.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * A utility class for manipulation of byte arrays.
 *
 * @author Joel Bondurant
 * @since 2011.0317
 */
public class ByteUtility {
	
	public final static int SIZE_IN_BITS = 8;

	private ByteUtility() {
	} // makes sure that no one tries to instantiate this.

	/**
	 * Hex characters for use producing human readable strings.
	 */
	public static final String hexChars = "0123456789ABCDEF";

	/**
	 * Converts a byte array to hex string without leading 0x.
	 *
	 * @param byteArray A byte array to convert to a hex string.
	 * @return A string representing the hex representation of the input.
	 */
	public static String toHexString(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder(2 + 2 * byteArray.length);
		for (final byte b : byteArray) {
			sb.append(hexChars.charAt((b & 0xF0) >> 4)).append(hexChars.charAt((b & 0x0F)));
		}
		return sb.toString();
	}

	/**
	 * Converts a byte array to hex string with leading 0x.
	 *
	 * @param byteArray A byte array to convert to a hex string.
	 * @return A string representing the hex representation of the input.
	 */
	public static String toHexString0x(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		return "0x" + toHexString(byteArray);
	}
	
	/**
	 * Convert a byte array to a binary string.
	 * @param byteArray A byte array.
	 * @return 0bXXXXXXXXXXXX
	 */
	public static String toBinString0b(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		return "0b" + toBinString(byteArray);
	}
	
	/**
	 * Convert a byte array to a binary string.
	 * @param byteArray A byte array.
	 * @return Binary representation.
	 */
	public static String toBinString(byte[] byteArray) {
		StringBuilder sb = new StringBuilder();
		for (int byteNum = byteArray.length - 1; byteNum >= 0; byteNum--) {
			for (int bitNum = SIZE_IN_BITS - 1; bitNum >= 0; bitNum--) {
				if (ByteUtility.bitAt(bitNum, byteArray[byteNum])) {
					sb.append("1");
				} else {
					sb.append("0");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Concatenate or join two byte arrays.
	 *
	 * @param a The leader byte array.
	 * @param b The follower byte array.
	 * @return [a, b] / a.join(b) / (a[0], ... a[a.length - 1], b[0], ... b[b.length - 1])
	 */
	public static byte[] concatenate(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 * Examine the values of bits within a byte.
	 *
	 * @param offset A zero indexed offset for the bits in the byte. [0,7]
	 * @param aByte An input byte.
	 * @return The bit value of the byte at the given offset.
	 */
	public static boolean bitAt(int offset, byte aByte) {
		return (aByte & (1 << offset)) != 0;
	}
	
	/**
	 * Set a bit within a byte.
	 * 
	 * @param offset The bit offset within the byte.
	 * @param bitValue The bit value to set.
	 * @param aByte A byte of bits.
	 * @return A new byte with the settings applied.
	 */
	public static byte setBitAt(int offset, boolean bitValue, byte aByte) {
		return (byte) ((bitValue) ? (aByte | (1 << offset)) : (aByte & ~(1 << offset)));
	}

	/**
	 * Convert a long to 8 bytes.
	 *
	 * @param aLong A long.
	 * @return A 2's compliment byte representation of the long.
	 */
	public static byte[] longToBytes(long aLong) {
		return ByteBuffer.allocate(8).putLong(aLong).array();
	}

	/**
	 * Read all bytes from a uri into a byte array with buffering.
	 *
	 * @param filePath A file path.
	 * @return The byte array representation of the supplied uri.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static byte[] readFully(Path filePath) throws FileNotFoundException, IOException {
		return readFully(new FileInputStream(filePath.toFile()));
	}

	/**
	 * Read all bytes from an input stream into a byte array with buffering.
	 *
	 * @param inputStream Any input stream of bytes.
	 * @return The byte array representation of the input stream.
	 * @throws IOException
	 */
	public static byte[] readFully(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[131072];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		return output.toByteArray();
	}

	/**
	 * Write a byte array to a uri with buffering.
	 *
	 * @param filePath A file path.
	 * @param bytes The byte array representation of the uri.
	 * @throws IOException
	 */
	public static void writeFully(Path filePath, byte[] bytes) throws IOException {
		try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
			bufferedOutputStream.write(bytes);
			bufferedOutputStream.flush();
		}
	}
	
	public static byte[] copy(byte[] aByte) {
		return Arrays.copyOf(aByte, aByte.length);
	}

}
