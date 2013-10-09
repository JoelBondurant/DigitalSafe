package com.analyticobjects.digitalsafe.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores a map of serializable objects.
 *
 * @author Joel Bondurant
 * @param <T> Type parameter for the object collection table.
 * @since 2013.10
 */
public class MapTable<T extends TableEntry> implements Table<T> {
	
	private static final long serialVersionUID = 1L;
	private final Map<Long, T> tableEntryMap;
	private final String name;

	public MapTable(String name) {
		this.tableEntryMap = new ConcurrentHashMap<>();
		this.name = name;
	}

	@Override
	public T getEntry(Long id) {
		if (this.tableEntryMap.containsKey(id)) {
			return this.tableEntryMap.get(id);
		}
		return null;
	}

	@Override
	public synchronized void putEntry(T entry) {
		Long id = entry.getId();
		if (id == null || id == 0L) { // apparently Longs are never null, but jic.
			id = this.tableEntryMap.size() + 1L;
			entry.setId(id);
		}
		this.tableEntryMap.put(id, entry);
	}

	@Override
	public List<T> getAll() {
		return new ArrayList<>(this.tableEntryMap.values());
	}

	@Override
	public String getName() {
		return this.name;
	}

	
}