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
package org.dbwiki.data.wiki;

import org.dbwiki.data.index.DatabaseEntry;

import org.dbwiki.data.resource.PageIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;

import org.dbwiki.data.time.TimeSequence;

/** Page description.  Implements DatabaseEntry so that it can be indexed, etc.
 * FIXME #wiki Make page descriptions implement full database entry interface
 * @author jcheney
 *
 */
public class WikiPageDescription implements DatabaseEntry {
	/*
	 * Private Variables
	 */
	
	private PageIdentifier _identifier;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public WikiPageDescription(String title, PageIdentifier identifier) {
		_identifier = identifier;
		_title = title;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public int compareTo(DatabaseEntry entry) {
		return this.label().compareTo(entry.label());
	}

	public ResourceIdentifier identifier() {
		return _identifier;
	}

	public String label() {
		return _title;
	}

	public int lastChange() {
		return -1;
	}

	public TimeSequence timestamp() {
		return new TimeSequence(1, null);
	}
}
