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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.data.index.DatabaseContent;

import org.dbwiki.data.resource.NID;

public class RDBMSDatabaseListing implements DatabaseContent {
	/*
	 * Private Variables
	 */
	
	private Hashtable<Integer, RDBMSDatabaseEntry> _entryIndex;
	private Vector<RDBMSDatabaseEntry> _entryList;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseListing() {
		_entryIndex = new Hashtable<Integer, RDBMSDatabaseEntry>();
		_entryList = new Vector<RDBMSDatabaseEntry>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(RDBMSDatabaseEntry entry) {
		_entryIndex.put(new Integer(entry.identifier().nodeID()), entry);
		_entryList.add(entry);
	}
	
	public RDBMSDatabaseEntry get(int index) {
		return _entryList.get(index);
	}

	public RDBMSDatabaseEntry get(NID identifier) {
		return _entryIndex.get(new Integer(identifier.nodeID()));
	}
	
	public int size() {
		return _entryList.size();
	}
	
	public void sort() {
		Collections.sort(_entryList);
	}
}
