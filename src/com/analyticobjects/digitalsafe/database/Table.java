package com.analyticobjects.digitalsafe.database;

import java.io.Serializable;
import java.util.List;

/**
 * The base interface for collections stored in SecureDatabase.
 *
 * @author Joel Bondurant
 * @param <T> A type parameter for the table collection.
 * @since 2013.10
 */
public interface Table<T extends TableEntry> extends Serializable {

	/**
	 * Table entry getter.
	 *
	 * @param id The unique primary identifier for the entry to get.
	 * @return The matching table entry.
	 */
	public T getEntry(Long id);

	/**
	 * Put an entry in the table.
	 *
	 * @param entry The entry to add.
	 */
	public void putEntry(T entry);
	
	/**
	 * @return A list of all entries in the table.
	 */
	public List<T> getAll();
	
	/**
	 * @return The table name.
	 */
	public String getName();

}
