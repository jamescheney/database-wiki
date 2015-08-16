package org.dbwiki.data.security;

public class Capability {
	private boolean _read;
	private boolean _insert;
	private boolean _update;
	private boolean _delete;
	
	public Capability() {
		_read = false;
		_insert = false;
		_update = false;
		_delete = false;
	}
	
	public Capability (boolean read, boolean insert, boolean update, boolean delete) {
		_read = read;
		_insert = insert;
		_update = update;
		_delete = delete;
	}
	
	public boolean getRead() {
		return _read;
	}

	public boolean getInsert() {
		return _insert;
	}

	public boolean getUpdate() {
		return _update;
	}

	public boolean getDelete() {
		return _delete;
	}
	
	public void setRead(boolean flag) {
		_read = flag;
	}
	
	public void setInsert(boolean flag) {
		_insert = flag;
	}
	
	public void setUpdate(boolean flag) {
		_update = flag;
	}
	
	public void setDelete(boolean flag) {
		_delete = flag;
	}
	
	public void set(boolean read, boolean insert, boolean update, boolean delete) {
		_read = read;
		_insert = insert;
		_update = update;
		_delete = delete;
	}
	
	public boolean isFullAccess() {
		return (_read && _insert && _update && _delete);
	}
	
	public boolean isNoneAccess() {
		return (!_read && !_insert && !_update && !_delete);
	}
}
