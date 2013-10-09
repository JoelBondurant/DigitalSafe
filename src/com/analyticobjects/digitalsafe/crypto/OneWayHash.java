package com.analyticobjects.digitalsafe.crypto;

import com.analyticobjects.utility.ByteUtility;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class to provide a one-way hash useful for mangling passphrases.
 *
 * @author Joel Bondurant
 * @version 2013.09
 */
public final class OneWayHash {

	private static final String SHA_256 = "SHA-256";
	private static final String UTF8 = "UTF-8";
	private static final String STATIC_SALT = "{[Me Some SALT! X?~BxVzzJ14Ry]}";

	/**
	 * Deterministically mix a string up. The purpose of this is to make dictionary attacks harder.
	 *
	 * @param str A string to mix up.
	 * @return One mixed up string.
	 */
	private static String mix(String str) {
		String result = str;
		String holder;
		int iters = str.length() / 4;
		for (int i = 0; i < iters; i++) {
			holder = mixStep(result);
			result = holder;
		}
		return result;
	}

	/**
	 * A single step in deterministically mixing a string up.
	 *
	 * @param str A string to mix up.
	 * @return One slightly mixed up string.
	 */
	private static String mixStep(String str) {
		if (str == null || str.isEmpty()) {
			return "";
		}
		if (str.length() == 1) {
			return str;
		}
		if (str.length() == 2) {
			StringBuilder sb = new StringBuilder(str);
			return sb.reverse().toString();
		}
		StringBuilder sb = new StringBuilder();
		String char1 = String.valueOf(str.charAt(0));
		String char2 = String.valueOf(str.charAt(1));
		String char3 = String.valueOf(str.charAt(2));
		if ((char1.compareTo(char2) > 0) && (char1.compareTo(char3) < 0)) {
			return sb.append(mixStep(str.substring(2))).append(str.charAt(1)).append(str.charAt(0)).toString();
		} else if ((char1.compareTo(char2) > 0) && (char1.compareTo(char3) > 0)) {
			String mixReverse = (new StringBuilder(mixStep(str.substring(2)))).reverse().toString();
			return sb.append(str.charAt(1)).append(mixReverse).append(str.charAt(0)).toString();
		} else if ((char1.compareTo(char2) < 0) && (char1.compareTo(char3) > 0)) {
			return sb.append(str.charAt(0)).append(mixStep(str.substring(2))).append(str.charAt(1)).toString();
		} else if ((char1.compareTo(char2) < 0) && (char1.compareTo(char3) < 0)) {
			String mixReverse = (new StringBuilder(mixStep(str.substring(2)))).reverse().toString();
			return sb.append(str.charAt(0)).append(mixReverse).append(str.charAt(1)).toString();
		}
		return sb.append(str.charAt(1)).append(str.charAt(0)).append(mixStep(str.substring(2))).toString();
	}

	/**
	 * Perform simple operations to a string to derive a longer string.
	 *
	 * @param str A string to derive a longer string from. A password for example.
	 * @return A longer string with characters all mixed up.
	 */
	private static String deriveLongerString(String str) {
		StringBuilder sb = new StringBuilder(str);
		StringBuilder builder = new StringBuilder();
		builder.append(sb.toString().toLowerCase());
		builder.append(sb.toString().toUpperCase());
		StringBuilder result = new StringBuilder();
		result.append(sb); // add raw unmixed input to confuse any statistical analysis on mixing.
		result.append(mix(builder.toString()));
		result.append(mix(builder.reverse().toString()));
		return result.toString();
	}

	/**
	 * Generate a one-way 256 bit password hash from a string. The hash is derived by first applying simple 
	 * string transformations to produce more characters and mix the letter ordering. Salts are also mixed in.
	 * Then iterative application of SHA-256 is applied mixing in the salt and input repeatedly in a step dependent
	 * manner which mixes in prior iteration results.
	 *
	 * @param stringToMangle The passphrase or string to hash.
	 * @param salt Entropy ain't what it used to be.
	 * @param iterations Tune to make computation as difficult as needed. (Math.max(10007, iterations) will be used.)
	 * @return A 256 bit hash of the input.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] hash256(String stringToMangle, String salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest sha256 = MessageDigest.getInstance(SHA_256);
		StringBuilder sb = new StringBuilder();
		sb.append(deriveLongerString(stringToMangle));
		sb.append(STATIC_SALT);
		sb.append(deriveLongerString(salt));
		byte[] rawInput = sb.toString().getBytes(UTF8);
		byte[] byteHolder1 = rawInput;
		byte[] byteHolder2;
		byte[] byteHolder3;
		int numWalls = 101;
		int wallThickness = 7;
		int wallInterval = iterations / numWalls;
		int minimumIterations = 10007;
		for (int i = 0; i < Math.max(minimumIterations, iterations); i++) {
			byteHolder2 = sha256.digest(byteHolder1);
			if ((i % wallInterval) < wallThickness) {
                // make things much more difficult to invert at each wall interval for wall thickness steps.
				// combining the full raw input in each wall iteration to make it extra noninvertible.
				if ((i % 2) == 0) {
					byteHolder3 = sha256.digest(ByteUtility.concatenate(byteHolder2, rawInput));
				} else {
					byteHolder3 = sha256.digest(ByteUtility.concatenate(rawInput, byteHolder2));
				}
				byteHolder1 = sha256.digest(ByteUtility.concatenate(byteHolder2, byteHolder3));
			} else {
				byteHolder1 = sha256.digest(byteHolder2); // The "easy" to invert step.
			}
		}
		return byteHolder1;
	}
	
	/**
	 * Generate a one-way 512 bit password hash from a string. The hash is derived from two different very different
	 * calls to hash256.
	 *
	 * @param stringToMangle The passphrase or string to hash.
	 * @param salt Entropy ain't what it used to be.
	 * @param iterations Tune to make computation as difficult as needed. (Math.max(30011, iterations) will be used.)
	 * @return A 512 bit hash of the input.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] hash512(String stringToMangle, String salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] hash256a = hash256(stringToMangle, salt, (iterations + 3) / 2);
		byte[] hash256b = hash256(ByteUtility.toHexString(hash256a), stringToMangle, (iterations + 1) / 2);
		return ByteUtility.concatenate(hash256a, hash256b);
	}

}
