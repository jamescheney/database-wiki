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
package org.dbwiki.web.ui.printer.index;

import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.DatabaseEntry;

/** 
 * 	Class providing a list/vector of tagged listings
 * @author jcheney
 *
 */
public class ContentIndex {
	/*
	 * Private Constants
	 */
	
	private static final String nonalphabeticContentKey = "&nbsp;&nbsp;&nbsp;OTHER";
	private static final String azIndex = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, ContentIndexContainer> _containerIndex;
	private Vector<ContentIndexContainer> _containerList;
	
	
	/*
	 * Constructors
	 */
	
	public ContentIndex(ContentIterator iterator) {
		_containerIndex = new Hashtable<String, ContentIndexContainer>();
		_containerList = new Vector<ContentIndexContainer>();
		for (int iChar = 0; iChar < azIndex.length(); iChar++) {
			String key = azIndex.substring(iChar, iChar + 1);
			ContentIndexContainer container = new ContentIndexContainer(key);
			_containerIndex.put(key, container);
			_containerList.add(container);
		}
		ContentIndexContainer container = new ContentIndexContainer(nonalphabeticContentKey);
		_containerIndex.put(nonalphabeticContentKey, container);
		_containerList.add(container);
		
		DatabaseEntry entry = null;
		
		while ((entry = iterator.next()) != null) {
			String key = entry.label().substring(0, 1);
			container = _containerIndex.get(key);
			if (container == null) {
				container = _containerList.lastElement();
			}
			container.add(entry);
		}
		
		if (_containerList.lastElement().size() == 0) {
			_containerIndex.remove(nonalphabeticContentKey);
			_containerList.remove(_containerIndex.size());
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean containsKey(String key) {
		return _containerIndex.containsKey(key);
	}
	
	public ContentIndexContainer get(int index) {
		return _containerList.get(index);
	}
	
	public ContentIndexContainer get(String key) {
		return _containerIndex.get(key);
	}

	public int size() {
		return _containerList.size();
	}
}
