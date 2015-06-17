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
package org.dbwiki.data.time;

import org.dbwiki.data.resource.ResourceIdentifier;


/** Abstract implementation of timestamped objects.
 * 
 * @author jcheney
 *
 */
public abstract class TimestampedObject {
	/*
	 * Private Variables
	 */
	
	private TimestampedObject _parent;
	private TimeSequence _timestamp;
	
	/*
	 * Constructors
	 */
	
	public TimestampedObject(TimestampedObject parent, TimeSequence timestamp) {
		_parent = parent;
		_timestamp = timestamp;
	}
	
	public TimestampedObject(TimestampedObject parent) {
		this(parent, null);
	}
		
	/*
	 * Public Methods
	 */
	/** The identifier of the object
	 * 
	 * @return ResourceIdentifier
	 */
	public abstract ResourceIdentifier identifier();
	
	/** Boolean indicating whether object has a timestamp
	 * 
	 * @return
	 */
	public boolean hasTimestamp() {
		return (_timestamp != null);
	}
	
	/** 
	 * The TimeSequence associated with the object
	 * @return
	 */
	public TimeSequence getTimestamp() {
		if (_timestamp != null) {
			return _timestamp;
		} else if (_parent != null) {
			return _parent.getTimestamp();
		} else {
			return null;
		}
	}
	
	/** Sets the TimeSequence associated with the object.
	 * 
	 * @param timestamp
	 */
	public void setTimestamp(TimeSequence timestamp) {
		_timestamp = timestamp;
	}
}
