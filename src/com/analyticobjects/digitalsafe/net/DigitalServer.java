package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.digitalsafe.res.ResourceLoader;
import com.analyticobjects.utility.ThreadUtility;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server for distributed storage.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class DigitalServer implements Runnable {
	
	private static DigitalServer singleton;
	private final Executor connectionProcessors;
	private ServerSocket serverSocket;
	private final Queue<Peer> peers;
	private Thread daemon;
	private boolean onState;
	private int port;
	
	public static void main(String[] args) {
		DigitalServer ds = getInstance();
		ds.start();
	}

	
	private DigitalServer() {
		this.onState = false;
		this.peers = new LinkedList<>();
		this.connectionProcessors = ThreadUtility.allAvailableProcessors();
		try {
			this.port = ResourceLoader.getPropertyAsInt("net", "DEFAULT_PORT");
		} catch (IOException ex) {
			this.port = 0;
			Logger.getLogger(DigitalServer.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	
	public static synchronized DigitalServer getInstance() {
		if (singleton == null) {
			singleton = new DigitalServer();
		}
		return singleton;
	}
	
	public void start() {
		this.onState = true;
		this.daemon = (new Thread(new DigitalServer()));
		this.daemon.setDaemon(true);
		this.daemon.start();
		Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, "Server started.");
	}
	
	public void stop() {
		this.onState = false;
		Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, "Server stopped.");
	}
	
	@Override
	public void run() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException ex) {
			Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
		}
		while (this.onState) {
			try {
				Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, "Server waiting for connection.");
				Socket clientSocket = this.serverSocket.accept();
				Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, "Server processing connection.");
				this.connectionProcessors.execute(new ConnectionTask(clientSocket));
			} catch (IOException ex) {
				Logger.getLogger(DigitalServer.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
			}
		}
	}
	
	private static InetAddress getDefaultSeed() throws IOException {
		return ResourceLoader.getPropertyAsInetAddress("net", "DEFAULT_SEED_IP");
	}
	
}
