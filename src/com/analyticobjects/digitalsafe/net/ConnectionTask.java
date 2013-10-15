package com.analyticobjects.digitalsafe.net;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A place to jot down ideas about what to do on connections.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionTask implements Runnable {
	
	private final Socket socket;
	
	public ConnectionTask(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		Logger.getLogger(ConnectionTask.class.getName()).log(Level.FINEST, "Connection: {0}", this.socket.getInetAddress().toString());
	}
	
}
