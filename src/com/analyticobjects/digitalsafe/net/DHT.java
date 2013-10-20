package com.analyticobjects.digitalsafe.net;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Feature creep.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DHT {
	
	public DHTKey put(byte[] data) {
		DHTKey key = null;
		try {
			key = DHTKey.gen(data);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			Logger.getLogger(DHT.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
		return key;
	}
	
	
	
}
