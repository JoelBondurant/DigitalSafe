package com.analyticobjects.digitalsafe.net;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * To keep the protocol simple, events will be handled serially.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionEventQueue extends PriorityBlockingQueue<ConnectionEvent> {
	
	public static final int MAX_LENGTH = 30;
	
	public boolean isFull() {
		return (this.size() >= MAX_LENGTH);
	}
	
}
