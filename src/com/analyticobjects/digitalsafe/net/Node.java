package com.analyticobjects.digitalsafe.net;

import java.security.PublicKey;
import java.util.UUID;

/**
 * A network node.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public interface Node {
	
	public UUID getGuid();
	public DHTKey getDHTKey();
	public PublicKey getPublicKey();
	public Boolean isSelf();
	public Boolean isSyncNode();
	public Boolean isBackupNode();
	public ConnectionStatistics getConnectionStatistics();
	
}
