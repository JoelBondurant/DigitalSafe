package com.analyticobjects.digitalsafe;

import com.analyticobjects.digitalsafe.exceptions.PasswordExpiredException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
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
 * A class to wrap up all cryptography into one place in effort to make 
 * future portability easier. Some day I want this running on my phone.
 *
 * @author Joel Bondurant
 * @version 2013.09
 */
public final class Crypto {
    
    private static final String KEYGEN = "PBKDF2WithHmacSHA1";
    // I do my own padding, becasue javax.crypto has padding related errors.
    private static final String AES = "AES/CBC/NoPadding";
    private static final int AES_BLOCK_SIZE = 16; // 16 byte (128 bit) blocks.
    private static final int AES_KEY_LENGTH = 128;
    private static final String SHA_256 = "SHA-256";
    private static final String UTF8 = "UTF-8";
    private static final String STATIC_SALT = "{[Me Some SALT! X?~BxVzzJ14Ry]}";
    
    
    /**
     * Deterministically mix a string up.
     * @param str A string to mix up.
     * @return One mixed up string.
     */
    public static String mix(String str) {
        String result =  str;
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
     * @param str A string to derive a longer string from. A password for example.
     * @return A longer string with characters all mixed up.
     */
    public static String deriveLongerString(String str) {
        StringBuilder sb = new StringBuilder(str);
        StringBuilder builder = new StringBuilder();
        builder.append(sb);
        builder.append(sb.toString().toLowerCase());
        builder.append(sb.toString().toUpperCase());
        StringBuilder result = new StringBuilder();
        result.append(builder);
        result.append(builder.reverse());
        return mix(result.toString());
    }
    
    /**
     * Generate a one-way 256 bit password hash from a string.
     * The hash is derived from iterative application of SHA-256, mixing in 
     * the salt and input repeatedly.
     * @param stringToMangle The password or string to hash.
     * @param salt Entropy ain't what it used to be.
     * @param iterations Tune to make computation as difficult as needed.
     * @return A 256 bit hash of the input.
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public static byte[] oneWayHash(String stringToMangle, String salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha256 = MessageDigest.getInstance(SHA_256);
        StringBuilder sb = new StringBuilder();
        sb.append(deriveLongerString(stringToMangle));
        sb.append(STATIC_SALT);
        sb.append(deriveLongerString(salt));
        byte[] rawInput = sb.toString().getBytes(UTF8);
        byte[] byteHolder1 = rawInput;
        byte[] byteHolder2;
        byte[] byteHolder3;
        int numWalls = 1000;
        int wallThickness = 10;
        int wallInterval = iterations / numWalls;
        int minimumIterations = 10007;
        for (int i = 0; i < Math.max(minimumIterations, iterations); i++) {
            byteHolder2 = sha256.digest(byteHolder1);
            if ((i % wallInterval) < wallThickness) { 
                // make things much more difficult to invert at each wall interval for wall thickness steps.
                // combining the full raw input in each wall iteration to make it extra uninvertible.
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
     * Decrypts DigitalSafe's custom 3-AES encrypted data.
     * @param passwordBytes
     * @param encryptedData
     * @return
     * @throws PasswordExpiredException 
     */
    public static byte[] decrypt(byte[] passwordBytes, byte[] encryptedData) throws PasswordExpiredException {
        byte[] byteHolder1;
        byte[] byteHolder2 = null;
        try {
            List<Cipher> ciphers = cipherList(passwordBytes, Cipher.DECRYPT_MODE);
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
     * DigitalSafe applies three independent layers of AES (3-AES), with keys
     * derived somewhat arguably independently from a single password. The 
     * effective key space size is huge.
     * @param passwordBytes The password bytes.
     * @param unencryptedData Raw unpadded data to encrypt.
     * @return Heavily encrypted data.
     * @throws PasswordExpiredException 
     */
    public static byte[] encrypt(byte[] passwordBytes, byte[] unencryptedData) throws PasswordExpiredException {
        byte[] byteHolder1;
        byte[] byteHolder2 = null;
        try {
            List<Cipher> ciphers = cipherList(passwordBytes, Cipher.ENCRYPT_MODE);
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
     * Pad unencrypted data for 16 byte block ciphers. For some reason javax.crypto
     * has PKCS5Padding mentioned for use with AES, despite it requiring at least
     * PKCS7Padding. This mathod is basically PKCS7Padding with an extra count block
     * to make clear and unambiguous padding.
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
     * @param passwordBytes The password bytes.
     * @param level Encryption level (of 3-AES); [1,2,3]
     * @return An encryption initialization vector.
     * @throws PasswordExpiredException 
     */
    private static IvParameterSpec ivParameterSpec16(byte[] salt, int level) throws PasswordExpiredException {
        byte[] iv = { 1, 1, 30, 1, 99, 2, 90, 1, 0, 2, 13, 32, 20, 3, 1, 70 };
        iv[level] = salt[0]; // swizzleness...
        iv[level + 1] = salt[1];
        iv[level + 3] = salt[2];
        iv[level + 5] = (byte)(0xff & (salt[1 + level] ^ salt[3]));
        iv[level + 7] = salt[4];
        iv[level + 10] = salt[5];
        iv[level + 11] = (byte)(0xff & (salt[3] ^ salt[7 - level]));
        return new IvParameterSpec(iv);
    }
    
    /**
     * A list of three independent AES ciphers.
     * @param passwordBytes The password bytes.
     * @param mode Cipher.DECRYPT_MODE | Cipher.UNENCRYPT_MODE
     * @return A list of three independent AES ciphers.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws PasswordExpiredException 
     */
    private static List<Cipher> cipherList(byte[] passwordBytes, int mode) throws NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException, InvalidAlgorithmParameterException, PasswordExpiredException {
        Cipher aes1 = Cipher.getInstance(AES);
        Cipher aes2 = Cipher.getInstance(AES);
        Cipher aes3 = Cipher.getInstance(AES);
        aes1.init(mode, keyGenAES(passwordBytes, "saltyN3SS&Whate", 189213), ivParameterSpec16(passwordBytes, 1));
        aes2.init(mode, keyGenAES(passwordBytes, "saltyN74G@337q8", 239404), ivParameterSpec16(passwordBytes, 2));
        aes3.init(mode, keyGenAES(passwordBytes, "saltyN99!14Ra12", 197781), ivParameterSpec16(passwordBytes, 3));
        return Arrays.asList(aes1, aes2, aes3);
    }
    
    /**
     * Generates various independent AES compatible encryption keys based on password.
     * @param passwordBytes The password bytes.
     * @param saltString Give the algorithms some salty mc entropy biscuits.
     * @param iterations Expand effective key space with iterative frizzle dizzling.
     * @return Can't tell you, it's a secret.
     * @throws PasswordExpiredException 
     */
    private static SecretKey keyGenAES(byte[] passwordBytes, String saltString, int iterations) throws PasswordExpiredException {
        SecretKey key = null;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEYGEN);
            byte[] salt = saltString.getBytes();
            salt[1] = passwordBytes[2]; // salting the salt.
            salt[2] = passwordBytes[1];
            salt[3] = (byte)(0xff & (passwordBytes[3] ^ passwordBytes[4]));
            PBEKeySpec pbeKeySpec = new PBEKeySpec(ByteUtility.toHexString(passwordBytes).toCharArray(), salt, iterations, AES_KEY_LENGTH);
            key = keyFactory.generateSecret(pbeKeySpec);
            key = new SecretKeySpec(key.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(SecureDatabase.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return key;
    }
    
}
