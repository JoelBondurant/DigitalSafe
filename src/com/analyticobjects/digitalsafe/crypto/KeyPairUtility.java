package com.analyticobjects.digitalsafe.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Class for helping with key pairs.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class KeyPairUtility {
	
	private static final String KEY_ALGO = "RSA";
	private static final int KEY_SIZE = 2048;
	private static final String SIGN_ALGO = "SHA256withRSA";
	
	
	private static KeyPairGenerator keyPairGenerator() throws NoSuchAlgorithmException {
		return KeyPairGenerator.getInstance(KEY_ALGO);
	}
	
	public static KeyPair keyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keygen = keyPairGenerator();
		keygen.initialize(KEY_SIZE, new SecureRandom());
		return keygen.generateKeyPair();
	}
	
	public static byte[] sign(PrivateKey privateKey, byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(SIGN_ALGO);
		signature.initSign(privateKey, new SecureRandom());
		signature.update(msg);
		return signature.sign();
	}
	
	public static boolean verify(PublicKey publicKey, byte[] msg, byte[] sign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(SIGN_ALGO);
		signature.initVerify(publicKey);
		signature.update(msg);
		return signature.verify(sign);
	}
	
	public static boolean validate(PublicKey publicKey) {
		return (8 * publicKey.getEncoded().length == KEY_SIZE);
	}
}
