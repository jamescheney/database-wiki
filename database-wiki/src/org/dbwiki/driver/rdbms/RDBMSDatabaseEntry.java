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
package org.dbwiki.driver.rdbms;

import org.dbwiki.data.index.DatabaseEntry;

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.time.sequence.TimeSequence;

public class RDBMSDatabaseEntry implements DatabaseEntry {
	/*
	 * Private Variables
	 */
	
	private NID _identifier;
	private String _label = "(null)";
	private int _lastChange = 1;
	private TimeSequence _timestamp;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseEntry(NID identifier, TimeSequence timestamp) {
		_identifier = identifier;
		_timestamp = timestamp;
	}
	
	
	/*
	 * Public Methods
	 */
	

	public int compareTo(DatabaseEntry entry) {
		int comp = this.label().compareTo(entry.label());
		if (comp != 0) {
			return comp;
		} else {
			return _identifier.nodeID() - ((NID)entry.identifier()).nodeID();
		}
	}


	public NID identifier() {
		return _identifier;
	}

	public String label() {
		return _label;
	}

	public String label(String value) {
		return (_label = value);
	}
	
	public int lastChange() {
		return _lastChange;
	}
	
	public int lastChange(int value) {
		return (_lastChange = value);
	}

	public TimeSequence timestamp() {
		return _timestamp;
	}
}
