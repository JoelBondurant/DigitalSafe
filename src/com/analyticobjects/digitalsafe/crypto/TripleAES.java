package com.analyticobjects.digitalsafe.crypto;

import com.analyticobjects.digitalsafe.database.SecureDatabase;
import com.analyticobjects.digitalsafe.exceptions.PassphraseExpiredException;
import com.analyticobjects.utility.ByteUtility;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A class to wrap up an implementation of triple AES. I decided to use a triple-128 bit AES rather than using Oracle's
 * higher strength stuff due to not wanting to deal with licensing and export rules.
 * 
 * TODO: Java 8 will support static interface methods. (Cipher.encrypt/decrypt)
 * TODO: Get rid of this NSA sponsored AES crap and implement Triple-Twofish.
 *
 * @author Joel Bondurant
 * @since 2013.08
 */
public final class TripleAES {

	private static final String KEYGEN = "PBKDF2WithHmacSHA1";
	// I do my own padding, becasue javax.crypto has some padding related mistakes.
	private static final String AES = "AES/CBC/NoPadding";
	private static final int AES_BLOCK_SIZE = 16; // 16 byte (128 bit) blocks.
	private static final int AES_KEY_LENGTH = 128;

	/**
	 * Decrypts DigitalSafe's custom 3-AES encrypted data.
	 *
	 * @param passphrase The passphrase to decrypt with.
	 * @param encryptedData The encrypted data to decrypt.
	 * @return
	 * @throws PassphraseExpiredException
	 */
	public static byte[] decrypt(Passphrase passphrase, byte[] encryptedData) throws PassphraseExpiredException {
		if (encryptedData.length == 0) {
			return new byte[0];
		}
		byte[] byteHolder1;
		byte[] byteHolder2 = null;
		try {
			List<Cipher> ciphers = cipherList(passphrase.getPassphraseHash(), Cipher.DECRYPT_MODE);
			byteHolder1 = ciphers.get(2).doFinal(encryptedData);
			byteHolder2 = ciphers.get(1).doFinal(byteHolder1);
			byteHolder1 = ciphers.get(0).doFinal(byteHolder2);
			byteHolder2 = unPad4AES(byteHolder1);
		} catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | InvalidKeyException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return byteHolder2;
	}

	/**
	 * DigitalSafe applies three independent layers of AES (3-AES), with keys derived somewhat arguably independently
	 * from a single password. The effective raw key space size is approximately 384 bits, despite an 8 character
	 * passphrase (the weakest allowed) will have maximum of 128 bits. Dictionary attacks and brute force attacks are 
	 * protected against with several steps of computationally intense passphrase hashing.
	 *
	 * @param passphrase The passphrase to encrypt with.
	 * @param unencryptedData Raw unpadded data to encrypt.
	 * @return Heavily encrypted data.
	 * @throws PassphraseExpiredException
	 */
	public static byte[] encrypt(Passphrase passphrase, byte[] unencryptedData) throws PassphraseExpiredException {
		if (unencryptedData.length == 0) {
			return new byte[0];
		}
		byte[] byteHolder1;
		byte[] byteHolder2 = null;
		try {
			List<Cipher> ciphers = cipherList(passphrase.getPassphraseHash(), Cipher.ENCRYPT_MODE);
			byteHolder1 = pad4AES(unencryptedData);
			byteHolder2 = ciphers.get(0).doFinal(byteHolder1);
			byteHolder1 = ciphers.get(1).doFinal(byteHolder2);
			byteHolder2 = ciphers.get(2).doFinal(byteHolder1);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return byteHolder2;
	}

	/**
	 * Removes my custom AES compatible padding from unencrypted data.
	 *
	 * @param paddedUnencryptedData Unencrypted data which has been padded.
	 * @return Raw unencrypted unpadded data.
	 * @throws BadPaddingException
	 */
	private static byte[] unPad4AES(byte[] paddedUnencryptedData) throws BadPaddingException {
		if (paddedUnencryptedData.length == 0) {
			throw new BadPaddingException();
		}
		if ((paddedUnencryptedData.length % AES_BLOCK_SIZE) != 0) {
			throw new BadPaddingException();
		}
		byte lastByte = paddedUnencryptedData[paddedUnencryptedData.length - 1];
		if ((lastByte < 0) || (lastByte >= AES_BLOCK_SIZE)) {
			throw new BadPaddingException();
		}
		for (int i = 1; i <= (lastByte + AES_BLOCK_SIZE); i++) {
			byte aByte = paddedUnencryptedData[paddedUnencryptedData.length - i];
			if (aByte != lastByte) {
				throw new BadPaddingException();
			}
		}
		int unencryptedSize = paddedUnencryptedData.length - lastByte - AES_BLOCK_SIZE;
		byte[] unencryptedData = new byte[unencryptedSize];
		System.arraycopy(paddedUnencryptedData, 0, unencryptedData, 0, unencryptedSize);
		return unencryptedData;
	}

	/**
	 * Pad unencrypted data for 16 byte block ciphers. For some reason javax.crypto has PKCS5Padding mentioned for use
	 * with AES, despite it requiring at least PKCS7Padding. This mathod is basically PKCS7Padding with an extra count
	 * block to make clear and unambiguous padding.
	 *
	 * @param unencryptedData Raw unencrypted binary to encrypt, of no particular size.
	 * @return Unencrypted binary padded for AES block size. (and Blowfish)
	 */
	private static byte[] pad4AES(byte[] unencryptedData) {
		int bytesToPad = AES_BLOCK_SIZE - (unencryptedData.length % AES_BLOCK_SIZE);
		byte bytesToPadValue = Integer.valueOf(bytesToPad).byteValue();
		int totalBytes = unencryptedData.length + bytesToPad + AES_BLOCK_SIZE;
		byte[] paddedUnencryptedData = new byte[totalBytes];
		System.arraycopy(unencryptedData, 0, paddedUnencryptedData, 0, unencryptedData.length);
		Arrays.fill(paddedUnencryptedData, unencryptedData.length, totalBytes, bytesToPadValue);
		return paddedUnencryptedData;
	}

	/**
	 * Generates independent cipher initialization vectors.
	 *
	 * @param passwordBytes The password bytes.
	 * @param level Encryption level (of 3-AES); [1,2,3]
	 * @return An encryption initialization vector.
	 * @throws PassphraseExpiredException
	 */
	private static IvParameterSpec ivParameterSpec16(byte[] salt, int level) throws PassphraseExpiredException {
		byte[] iv = {1, 1, 30, 1, 99, 2, 90, 1, 0, 2, 13, 32, 20, 3, 1, 70};
		iv[level] = salt[0]; // swizzleness...
		iv[level + 1] = salt[10 * level];
		iv[level + 3] = salt[16 * level];
		iv[level + 5] = (byte) (0xff & (salt[1 + level] ^ salt[7 * level]));
		iv[level + 7] = salt[17 * level];
		iv[level + 10] = salt[20 * level];
		iv[level + 11] = (byte) (0xff & (salt[13 * level] ^ salt[7 - level]));
		return new IvParameterSpec(iv);
	}

	/**
	 * A list of three independent AES ciphers.
	 *
	 * @param passphraseBytes The password bytes.
	 * @param mode Cipher.DECRYPT_MODE | Cipher.UNENCRYPT_MODE
	 * @return A list of three independent AES ciphers.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws PassphraseExpiredException
	 */
	private static List<Cipher> cipherList(byte[] passphraseBytes, int mode) throws NoSuchAlgorithmException, NoSuchPaddingException,
		InvalidKeyException, InvalidAlgorithmParameterException, PassphraseExpiredException {
		Cipher aes1 = Cipher.getInstance(AES);
		Cipher aes2 = Cipher.getInstance(AES);
		Cipher aes3 = Cipher.getInstance(AES);
		aes1.init(mode, keyGenAES(passphraseBytes, "saltyN3SS&Whate", 18913), ivParameterSpec16(passphraseBytes, 1));
		aes2.init(mode, keyGenAES(passphraseBytes, "saltyN74G@337q8", 23944), ivParameterSpec16(passphraseBytes, 2));
		aes3.init(mode, keyGenAES(passphraseBytes, "saltyN99!14Ra12", 19781), ivParameterSpec16(passphraseBytes, 3));
		return Arrays.asList(aes1, aes2, aes3);
	}

	/**
	 * Generates various independent AES compatible encryption keys based on password.
	 *
	 * @param passwordBytes The password bytes.
	 * @param saltString Give the algorithms some salty mc entropy biscuits.
	 * @param iterations Expand effective key space with iterative frizzle dizzling.
	 * @return Can't tell you, it's a secret.
	 * @throws PassphraseExpiredException
	 */
	private static SecretKey keyGenAES(byte[] passphraseBytes, String saltString, int iterations) throws PassphraseExpiredException {
		SecretKey key = null;
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
			byte[] salt = saltString.getBytes();
			salt[1] = passphraseBytes[2]; // salting the salt.
			salt[2] = passphraseBytes[10];
			salt[3] = (byte) (0xff & (passphraseBytes[60] ^ passphraseBytes[50]));
			PBEKeySpec pbeKeySpec = new PBEKeySpec(ByteUtility.toHexString(passphraseBytes).toCharArray(), salt, iterations, AES_KEY_LENGTH);
			key = keyFactory.generateSecret(pbeKeySpec);
			key = new SecretKeySpec(key.getEncoded(), "AES");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return key;
	}
}
