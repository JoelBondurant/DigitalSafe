package com.analyticobjects.digitalsafe.net;

import com.analyticobjects.utility.KeyPairUtility;
import com.analyticobjects.digitalsafe.database.IndexedTableEntry;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
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
	private UUID guid;
	private DHTKey dhtKey;
	private PublicKey publicKey;
	private Boolean self, syncNode, backupNode;
	private ConnectionStatistics connStats;
	
	public Peer() {}
	
	public PrivateKey initSelf() throws NoSuchAlgorithmException, InterruptedException, InvalidKeyException {
		this.guid = UUID.randomUUID();
		this.dhtKey = DHTKey.gen();
		this.self = Boolean.TRUE;
		this.syncNode = Boolean.FALSE;
		this.backupNode = Boolean.FALSE;
		this.connStats = new ConnectionStatistics();
		KeyPair keyPair = KeyPairUtility.keyPair();
		this.publicKey = keyPair.getPublic();
		return keyPair.getPrivate();
	}

	@Override
	public String getIndexId() {
		return this.guid.toString();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public UUID getGuid() {
		return this.guid;
	}

	@Override
	public DHTKey getDHTKey() {
		return this.dhtKey;
	}

	@Override
	public PublicKey getPublicKey() {
		return this.publicKey;
	}
	
	@Override
	public Boolean isSelf() {
		return this.self;
	}
	
	@Override
	public Boolean isSyncNode() {
		return this.syncNode;
	}

	@Override
	public Boolean isBackupNode() {
		return this.backupNode;
	}

	@Override
	public ConnectionStatistics getConnectionStatistics() {
		return this.connStats;
	}


	
}
