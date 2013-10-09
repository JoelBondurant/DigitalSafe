package com.analyticobjects.digitalsafe.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main serializable container for the root index into all data contained within SecureDatabase.
 *
 * @author Joel Bondurant
 * @since 2013.08
 */
public class MasterIndex implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Map<String, FileTable> fileTables;
	private final Map<String, IndexedMapTable> indexedMapTables;
	private final Map<String, MapTable> mapTables;


	public MasterIndex() {
		this.fileTables = new ConcurrentHashMap<>();
		this.indexedMapTables = new ConcurrentHashMap<>();
		this.mapTables = new ConcurrentHashMap<>();
	}

	public void putFileTable(FileTable fileTable) {
		this.fileTables.put(fileTable.getName(), fileTable);
	}
	
	public FileTable getFileTable(String name) {
		if (this.fileTables.containsKey(name)) {
			return this.fileTables.get(name);
		}
		return null;
	}
	
	public List<FileTable> getFileTables() {
		return new ArrayList<>(this.fileTables.values());
	}
	
	public void putIndexedMapTable(IndexedMapTable indexedMapTable) {
		this.indexedMapTables.put(indexedMapTable.getName(), indexedMapTable);
	}
	
	public IndexedMapTable getIndexedMapTable(String name) {
		if (this.indexedMapTables.containsKey(name)) {
			return this.indexedMapTables.get(name);
		}
		return null;
	}
	
	public void putMapTable(MapTable mapTable) {
		this.mapTables.put(mapTable.getName(), mapTable);
	}
	
	public MapTable getMapTable(String name) {
		if (this.mapTables.containsKey(name)) {
			return this.mapTables.get(name);
		}
		return null;
	}

}
