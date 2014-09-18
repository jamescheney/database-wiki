package org.dbwiki.web.server;

/**
 * Entity class for entry-level access permission
 * 
 *
 */

public class DBPolicy {
	private int entry;
	private int user_id;
	private boolean read;
	private boolean insert;
	private boolean update;
	private boolean delete;
	
	public DBPolicy(int user_id, int entry, boolean read,
			boolean insert, boolean update, boolean delete) {
		super();
		this.entry = entry;
		this.user_id = user_id;
		this.read = read;
		this.insert = insert;
		this.update = update;
		this.delete = delete;
	}

	public int entry() {
		return entry;
	}

	public void setEntry(int entry) {
		this.entry = entry;
	}

	public int user_id() {
		return user_id;
	}

	public void set_user_id(int user_id) {
		this.user_id = user_id;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isInsert() {
		return insert;
	}

	public void setInsert(boolean insert) {
		this.insert = insert;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	
	
}
