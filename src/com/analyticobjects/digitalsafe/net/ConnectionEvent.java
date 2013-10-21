package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.utility.ByteUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A place to jot down ideas about what to do on connections.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionEvent implements Comparable, Runnable {
	
	private final Socket socket;
	private OutputStream socketOut;
	private InputStream socketIn;
	
	public ConnectionEvent(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		Logger.getLogger(ConnectionEvent.class.getName()).log(Level.INFO, "{0}", toString());
		try {
			socketIn = this.socket.getInputStream();
			socketOut= this.socket.getOutputStream();
			byte[] readFully = ByteUtility.readFully(socketIn);
		} catch (IOException ex) {
			Logger.getLogger(ConnectionEvent.class.getName()).log(Level.SEVERE, "Connection dropped.");
			Logger.getLogger(ConnectionEvent.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			return;
		}
		try {
			this.socket.close();
		} catch (IOException ex) {
			Logger.getLogger(ConnectionEvent.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	
	@Override
	public String toString() {
		return "Connection: " + this.socket.getInetAddress().getCanonicalHostName();
	}

	@Override
	public int compareTo(Object obj) {
		if (obj == null || !(obj instanceof ConnectionEvent)) {
			return 0;
		}
		ConnectionEvent other = (ConnectionEvent) obj;
		return this.toString().compareTo(other.toString());
	}
	
}
