/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
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
package org.dbwiki.data.document;

public abstract class PasteNode {
	/*
	 * Private Variables
	 */
	
	private PasteDatabaseInfo _database;
	private String _key;
	
	
	/*
	 * Constructors
	 */
	
	public PasteNode(PasteDatabaseInfo database, String key) {
		_database = database;
		_key = key;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isElement();
	
	
	/*
	 * Public Methods
	 */
	
	public PasteDatabaseInfo database() {
		return _database;
	}
	
	public boolean isText() {
		return !this.isElement();
	}
	
	public String key() {
		return _key;
	}
}
