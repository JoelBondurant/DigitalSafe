package com.analyticobjects.digitalsafe.database;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A decorator for adding String indexes to a MapTable.
 * 
 * TODO: Make sure to do whatever is needed to support multiple named indexes.
 *
 * @author Joel Bondurant
 * @param <T> A type parameter for an IndexedTableEntry.
 * @since 2013.10
 */
public class IndexedMapTable<T extends IndexedTableEntry> implements Table<T> {
	
	private static final long serialVersionUID = 1L;
	private final Map<String, Long> tableEntryIndexMap;
	private final MapTable<T> backingMapTable;

	public IndexedMapTable(MapTable<T> backingMapTable) {
		this.backingMapTable = backingMapTable;
		this.tableEntryIndexMap = new ConcurrentHashMap<>();
	}
	
	public T getEntry(String strId) {
		if (this.tableEntryIndexMap.containsKey(strId)) {
			Long id = this.tableEntryIndexMap.get(strId);
			if (id != null) {
				return this.backingMapTable.getEntry(id);
			}
		}
		return null;
	}

	@Override
	public synchronized void putEntry(T entry) {
		String strId = entry.getIndexId().toLowerCase();
		if (strId == null) {
			throw new NullPointerException("Entry must have a non-null index id.");
		}
		this.backingMapTable.putEntry(entry);
		this.tableEntryIndexMap.put(entry.getIndexId(), entry.getId());
	}

	@Override
	public T getEntry(Long id) {
		return this.backingMapTable.getEntry(id);
	}

	@Override
	public List<T> getAll() {
		return this.backingMapTable.getAll();
	}

	@Override
	public String getName() {
		return this.backingMapTable.getName();
	}

	
}
