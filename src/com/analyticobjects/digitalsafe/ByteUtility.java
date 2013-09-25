package com.analyticobjects.digitalsafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for manipulation of byte arrays.
 *
 * @author Joel Bondurant
 * @version 2011.0317
 */
public class ByteUtility {

	private ByteUtility() {} // makes sure that no one tries to instantiate this.
	
	/**
	 * Hex characters for use producing human readable strings.
	 */
	public static final String hexChars = "0123456789ABCDEF";

	/**
	 * Converts a byte array to hex string with leading 0x.
	 *
	 * @param byteArray A byte array to convert to a hex string.
	 * @return A string representing the hex representation of the input.
	 */
	public static String toHexString(byte [] byteArray) {
		if (byteArray == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder(2 + 2 * byteArray.length);
		sb.append("0x");
		for (final byte b: byteArray) {
			sb.append(hexChars.charAt((b & 0xF0) >> 4)).append(hexChars.charAt((b & 0x0F)));
		}
		return sb.toString();
	}

	/**
	 * Parse a string delimited list of integers into an integer list. 
	 * @param intStrList A list of integers delimited by ...
	 * @param delimiter ... delimited by this.
	 * @return A list of the integers.
	 */
	public static List<Integer> intStrList2intList(String intStrList, String delimiter) {
                String[] intStrArr = intStrList.split(delimiter);
		List<Integer> intList = new ArrayList<>(intStrArr.length);
		for(String intStr: intStrArr) {
			intList.add(Integer.parseInt(intStr));
		}
		return intList;
	}

        
        /**
         * Read all bytes from an input stream into a byte array.
         * @param inputStream Any input stream of bytes.
         * @return The byte array representation of the input stream.
         * @throws IOException 
         */
        public static byte[] readFully(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }


}
