package org.dbwiki.web.server;

/**
 * Entity class for database entries
 * 
 *
 */

public class Entry {
	private int entry_id;
	private String entry_value;
	public Entry(int entry_id, String entry_value) {
		super();
		this.entry_id = entry_id;
		this.entry_value = entry_value;
	}
	public String entry_value() {
		return entry_value;
	}
	public void setEntry_value(String entry_value) {
		this.entry_value = entry_value;
	}
	public int entry_id() {
		return entry_id;
	}
	public void setEntry_id(int entry_id) {
		this.entry_id = entry_id;
	}
	
}
