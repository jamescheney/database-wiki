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

public class ChangesSinceContentIterator extends ContentIterator {
	/*
	 * Private Variables
	 */
	
	private int _time;
	
	
	/*
	 * Constructors
	 */
	
	public ChangesSinceContentIterator(DatabaseContent content, int time) {
		super(content);
		
		_time = time;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean matches(DatabaseEntry entry) {
		return (entry.lastChange() > _time);
	}
}
