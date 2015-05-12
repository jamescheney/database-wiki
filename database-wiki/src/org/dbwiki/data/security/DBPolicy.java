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

package org.dbwiki.data.security;

/**
 * Entity class for entry-level access permission
 * 
 * @author mingjun
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
