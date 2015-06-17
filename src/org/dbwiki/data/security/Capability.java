package org.dbwiki.data.security;

public class Capability {
	private boolean _read;
	private boolean _insert;
	private boolean _delete;
	private boolean _update;
	
	public Capability (boolean read, boolean insert, boolean delete, boolean update) {
		_read = read;
		_insert = insert;
		_delete = delete;
		_update = update;
	}
	
	public boolean isRead() {
		return _read;
	}

	public boolean isInsert() {
		return _insert;
	}

	public boolean isUpdate() {
		return _update;
	}

	public boolean isDelete() {
		return _delete;
	}
}
