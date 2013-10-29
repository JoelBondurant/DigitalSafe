package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.utility.KeyPairUtility;
import com.analyticobjects.utility.ByteUtility;
import com.analyticobjects.utility.SerializationUtility;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A TCP message wrapper.
 * 
 * TODO: Move off Java serialization.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class Message implements Serializable {
	
	private final PublicKey publicKey;
	private final byte[] signature;
	private final byte[] data;
	
	public Message(PublicKey publicKey, PrivateKey privateKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		this.publicKey = publicKey;
		this.signature = KeyPairUtility.sign(privateKey, data);
		this.data = data;
	}
	
	public boolean validate() {
		if (KeyPairUtility.validate(publicKey)) {
			try {
				return KeyPairUtility.verify(publicKey, data, signature);
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException ex) {
				Logger.getLogger(Message.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
		return false;
	}
	
	public byte[] rawData() {
		return ByteUtility.copy(data);
	}
	
	public <T> T decodeObject() throws IOException, ClassNotFoundException {
		return SerializationUtility.<T>inflate(data);
	}
	
}
