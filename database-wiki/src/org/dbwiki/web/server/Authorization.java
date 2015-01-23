/*
    BEGIN LICENSE BLOCK
    Copyright 2010-2014, Heiko Mueller, Sam Lindley, James Cheney, 
    Ondrej Cierny, Mingjun Han, and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/

package org.dbwiki.web.server;

/**
 * Entity class for database-level access permission
 * 
 * @author mingjun
 * TODO: Move to security module
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
