package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.digitalsafe.res.ResourceLoader;
import com.analyticobjects.utility.LogUtility;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
	private static final Logger logger = Logger.getLogger(DigitalServer.class.getName());
	private final ConnectionEventQueue eventQueue;
	private ServerSocket serverSocket;
	private Thread serverDaemon, eventDaemon;
	private boolean onState;
	private int port;
	
	public static void main(String[] args) throws InterruptedException {
		LogUtility.setLoggingLevelGlobally(Level.ALL);
		DigitalServer ds = getInstance();
		ds.start();
	}

	@Override
	public void finalize() throws Throwable {
		logger.log(Level.INFO, "DigitalServer.finalize();");
		super.finalize();
	}
	
	private DigitalServer() {
		this.onState = false;
		this.eventQueue = new ConnectionEventQueue();
		try {
			this.port = ResourceLoader.getPropertyAsInt("net", "DEFAULT_PORT");
		} catch (IOException ex) {
			this.port = 0;
			logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	
	public static synchronized DigitalServer getInstance() {
		if (singleton == null) {
			singleton = new DigitalServer();
		}
		return singleton;
	}
	
	public void start() throws InterruptedException {
		this.onState = true;
		this.serverDaemon = (new Thread(this));
		this.serverDaemon.setDaemon(true);
		this.serverDaemon.start();
		this.eventDaemon = new Thread(new ConnectionEventHandler());
		this.eventDaemon.setDaemon(true);
		this.eventDaemon.start();
		this.serverDaemon.join();
		this.eventDaemon.join();
	}
	
	public void stop() {
		this.onState = false;
		logger.info("Server stopped.");
	}
	
	@Override
	public void run() {
		try {
			this.serverSocket = new ServerSocket(this.port);
			logger.log(Level.INFO, "Server started.");
			logger.log(Level.INFO, "{0}", this.serverSocket.toString());
		} catch (IOException ex) {
			logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
		}
		while (this.onState) {
			try {
				logger.log(Level.INFO, "Server waiting for connection.");
				Socket clientSocket = this.serverSocket.accept();
				logger.log(Level.INFO, "Server processing connection.");
				logger.log(Level.INFO, "{0}", clientSocket.toString());
				this.eventQueue.add(new ConnectionEvent(clientSocket));
			} catch (IOException ex) {
				logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
	}
	
	public synchronized boolean onState() {
		return this.onState;
	}
	
	public ConnectionEvent poll() {
		return this.eventQueue.poll();
	}
	
	private static InetAddress getDefaultSeed() throws IOException {
		return ResourceLoader.getPropertyAsInetAddress("net", "DEFAULT_SEED_IP");
	}
	
}
