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
package org.dbwiki.data.database;

import org.dbwiki.data.time.Timestamp;

public abstract class TimestampedObject {
	/*
	 * Private Variables
	 */
	
	private TimestampedObject _parent;
	private Timestamp _timestamp;
	
	/*
	 * Constructors
	 */
	
	public TimestampedObject(TimestampedObject parent, Timestamp timestamp) {
		_parent = parent;
		_timestamp = timestamp;
	}
	
	public TimestampedObject(TimestampedObject parent) {
		this(parent, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean hasTimestamp() {
		return (_timestamp != null);
	}
	
	public Timestamp getTimestamp() {
		if (_timestamp != null) {
			return _timestamp;
		} else if (_parent != null) {
			return _parent.getTimestamp();
		} else {
			return null;
		}
	}
	
	public void setTimestamp(Timestamp timestamp) {
		_timestamp = timestamp;
	}
}
