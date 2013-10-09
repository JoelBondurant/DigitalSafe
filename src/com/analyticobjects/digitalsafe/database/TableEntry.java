package com.analyticobjects.digitalsafe.database;

import java.io.Serializable;

/**
 * Stores a collection of serializable
 *
 * @author Joel Bondurant
 * @since 2013.10
 */
public interface TableEntry extends Serializable {
	
	public Long getId();
	public void setId(Long id);
	
}
