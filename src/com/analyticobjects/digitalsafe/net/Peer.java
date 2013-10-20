package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.digitalsafe.database.IndexedTableEntry;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.UUID;

/**
 * Peer specific data and actions.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class Peer implements Node, IndexedTableEntry {

	private Long id;
	private UUID uniqueId;
	private DHTKey dhtKey;
	private PublicKey publicKey;
	private Boolean self;
	private Boolean syncBuddy;
	private ConnectionStatistics connStats;
	
	public Peer() {
		
	}

	@Override
	public String getIndexId() {
		return this.dhtKey.toString();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
}
