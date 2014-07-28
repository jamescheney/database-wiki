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
package org.dbwiki.data.index;

/** Doesn't seem to be used
 * 
 * @author jcheney
 *
 */
public class ArrayDatabaseListing implements DatabaseContent {
	/*
	 * Private Variables
	 */
	
	private DatabaseEntry[] _entries;
	

	/*
	 * Constructors
	 */
	
	public ArrayDatabaseListing(DatabaseEntry[] list) {
		_entries = list;
		
	}
	/*
	 * Public Methods
	 */
	
	@Override
	public DatabaseEntry get(int index) {
		return _entries[index];
	}

	@Override
	public DatabaseEntry get(String key) {
		for (DatabaseEntry entry : _entries) {
			if (entry.label().equals(key)) {
				return entry;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return _entries.length;
	}
}
