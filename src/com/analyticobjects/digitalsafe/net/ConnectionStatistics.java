package com.analyticobjects.digitalsafe.net;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Deque;


/**
 * Aggregate stats on ConnectionEventStatistics.
 * 
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionStatistics implements Serializable {
	
	private static final int MAX_RECORDS = 1000;
	private final Deque<ConnectionEventStatistics> connectionEventStats;
	
	public ConnectionStatistics() {
		this.connectionEventStats = new LinkedList<>();
	}
	
	public void add(ConnectionEventStatistics ces) {
		if (this.connectionEventStats.size() >= MAX_RECORDS) {
			this.connectionEventStats.pollFirst();
		}
		this.connectionEventStats.add(ces);
	}
	
	
}
