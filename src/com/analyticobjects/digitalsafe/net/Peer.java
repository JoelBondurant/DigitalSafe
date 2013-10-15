package com.analyticobjects.digitalsafe.net;

import java.net.InetAddress;
import java.security.PublicKey;

/**
 * Peer specific data and actions.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class Peer implements Node {

	private PublicKey publicKey;
	private InetAddress iNetAddress;
	
	public Peer() {
		
	}
	
}
