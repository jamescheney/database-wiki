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
	private int _entry;
	private int _user_id;
	private Capability _capability;
	
	public DBPolicy(int user_id, int entry, boolean read,
			boolean insert, boolean update, boolean delete) {
		super();
		this._entry = entry;
		this._user_id = user_id;
		this._capability = new Capability(read,insert,update,delete);
	}

	public int entry() {
		return _entry;
	}


	public int user_id() {
		return _user_id;
	}

	
	public Capability capability() {return _capability;}

}
