package org.dbwiki.web.server;

public class Authorization {
	private String _database_name;
	private String _user_login;
	private boolean _read;
	private boolean _insert;
	private boolean _delete;
	private boolean _update;
	
	

	public Authorization(String _database_name, String _user_login,
			boolean _read, boolean _insert, boolean _delete, boolean _update) {
		this._database_name = _database_name;
		this._user_login = _user_login;
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

	public String user_login() {
		return _user_login;
	}

	public void set_user_login(String _user_login) {
		this._user_login = _user_login;
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
