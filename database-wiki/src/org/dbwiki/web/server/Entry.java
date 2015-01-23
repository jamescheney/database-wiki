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
 * Entity class for database entries
 * 
 * @author mingjun
 * TODO: Move to security module
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
