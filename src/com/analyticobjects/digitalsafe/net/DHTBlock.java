package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.utility.ByteUtility;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A block of data
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DHTBlock implements Serializable {
	
	private final byte[] data;
	private final DHTKey dhtKey;
	
	public DHTBlock(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
		this.data = ByteUtility.copy(data);
		this.dhtKey = DHTKey.gen(data);
	}
	
	public byte[] copyData() {
		return ByteUtility.copy(data);
	}
	
	public boolean isValid() {
		try {
			return DHTKey.gen(data).equals(this.dhtKey);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			Logger.getLogger(DHTBlock.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return false;
	}
}
