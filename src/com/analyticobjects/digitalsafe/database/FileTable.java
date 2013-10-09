package com.analyticobjects.digitalsafe.database;

import java.util.UUID;

/**
 * A table to hold files.
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public class FileTable extends IndexedMapTable<FileTableEntry>  {
	
	private final String crypticName;

	/**
	 * Create a new file table.
	 * 
	 * @param name The name identifier for the table.
	 */
	public FileTable(String name) {
		super(new MapTable<FileTableEntry>(name));
		this.crypticName = UUID.randomUUID().toString();
	}

	public String getCrypticName() {
		return this.crypticName;
	}
	
}
