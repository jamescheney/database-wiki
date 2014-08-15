package org.dbwiki.web.server;

/**
 * Entity class for database-level access permission
 * 
 *
 */

public class Authorization {
	private String _database_name;
	private int _user_id;
	private boolean _read;
	private boolean _insert;
	private boolean _delete;
	private boolean _update;
	
	

	public Authorization(String _database_name, int _user_id,
			boolean _read, boolean _insert, boolean _delete, boolean _update) {
		this._database_name = _database_name;
		this._user_id = _user_id;
		this._read = _read;
		this._insert = _insert;
		this._delete = _delete;
		this._update = _update;
	}

	public String database_name() {
		return _database_name;
	}

	public void set_database_name(String _database_name) {
		this._database_name = _database_name;
	}

	public int user_id() {
		return _user_id;
	}

	public void set_user_id(int _user_id) {
		this._user_id = _user_id;
	}

	public boolean is_read() {
		return _read;
	}

	public void set_read(boolean _read) {
		this._read = _read;
	}

	public boolean is_insert() {
		return _insert;
	}

	public void set_insert(boolean _insert) {
		this._insert = _insert;
	}

	public boolean is_delete() {
		return _delete;
	}

	public void set_delete(boolean _delete) {
		this._delete = _delete;
	}

	public boolean is_update() {
		return _update;
	}

	public void set_update(boolean _update) {
		this._update = _update;
	}

	
	
}
