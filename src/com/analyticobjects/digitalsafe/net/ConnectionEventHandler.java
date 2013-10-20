package com.analyticobjects.digitalsafe.net;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * To handle connection handling in a separate thread.
 * 
 * TODO: This class begs for Java8 closures.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionEventHandler implements Runnable {

	private static final Logger logger = Logger.getLogger(ConnectionEventHandler.class.getName());

	@Override
	public void run() {
		logger.log(Level.INFO, "ConnectionEventHandler:");
		while (DigitalServer.getInstance().onState()) {
			ConnectionEvent connectionEvent = DigitalServer.getInstance().poll();
			if (connectionEvent != null) {
				logger.log(Level.INFO, "Running connection event task.");
				connectionEvent.run();
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				}
			}
		}
	}

}
