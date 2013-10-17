package com.analyticobjects.digitalsafe.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Feature creep.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DHT {
	
	public DHTKey put(byte[] data) {
			
		return null;
	}
	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		System.out.println(InetAddress.getLocalHost().getHostAddress());
		try (Socket outsideWorld = new Socket("example.com", 80)) {
			System.out.println(outsideWorld.getInetAddress());
			System.out.println(outsideWorld.getLocalAddress());
		}
	}
	
}
