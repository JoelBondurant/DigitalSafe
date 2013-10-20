package com.analyticobjects.digitalsafe.net;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Stats for a single connection event.
 * 
 * @author Joel Bondurant
 * @since 2013.10
 */
public class ConnectionEventStatistics implements Serializable, Comparable {
	
	private final InetAddress iNetAddress;
	private long startTime;
	private long endTime;
	private long bytesSent;
	private long bytesReceived;
	
	public ConnectionEventStatistics(InetAddress iNetAddress) {
		this.iNetAddress = iNetAddress;
		this.startTime = 0L;
		this.endTime = 0L;
		this.bytesSent = 0L;
		this.bytesReceived = 0L;
	}
	
	public void startTime() {
		this.startTime = System.nanoTime();
	}
	
	public void endTime() {
		this.endTime = System.nanoTime();
	}
	
	public long connectionTimeNS() {
		return (this.endTime - this.startTime);
	}
	
	public long connectionTimeMS() {
		return connectionTimeNS() / 1_000_000;
	}
	
	public long connectionTimeS() {
		return connectionTimeMS() / 1_000;
	}
	
	public long connectionTimeMIN() {
		return connectionTimeS() / 60;
	}
	
	public long totalBytesTranserred() {
		return bytesReceived + bytesSent;
	}
	
	public long bytesSent() {
		return this.bytesSent;
	}
	
	public long bytesReceived() {
		return this.bytesReceived;
	}
	
	public void setBytesSent(long bytesSent) {
		this.bytesSent = bytesSent;
	}
	
	public void setBytesReceived(long bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
	
	public long score() {
		long connectionTimeNS = connectionTimeNS();
		long totalBytesTranserred = totalBytesTranserred();
		if (connectionTimeNS == 0L || totalBytesTranserred == 0L) {
			return 0L;
		}
		return totalBytesTranserred / connectionTimeNS;
	}

	@Override
	public int compareTo(Object obj) {
		if (obj == null || !(obj instanceof ConnectionEventStatistics)) {
			return 0;
		}
		ConnectionEventStatistics other = (ConnectionEventStatistics) obj;
		Long thisScore = this.score();
		Long otherScore = other.score();
		return thisScore.compareTo(otherScore);
	}
			
			
	
	
}
