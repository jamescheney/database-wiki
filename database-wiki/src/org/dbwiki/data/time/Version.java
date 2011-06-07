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

import org.dbwiki.data.provenance.Provenance;

/** Interface to Version information.
 * A Version is a struct containing a name, number, TimestampPrinter, Provenance, and time
 * The name is basically a date-time string stored in the database.
 * FIXME #time: Eliminate version names, recalculate from _time instead
 * @author jcheney
 *
 */
public class Version {
	private String _name;
	private int _number;
	private VersionIndex _index;
	private Provenance _provenance;
	private long _time;
	
	
	/*
	 * Constructors
	 */
	
	public Version(int number, String name, long time, Provenance provenance, VersionIndex index) {
		_name = name;
		_number = number;
		_provenance = provenance;
		_time = time;
		_index = index;
	}
	
	
	/*
	 * Public Methods
	 */

	public String name() {
		return _name;
	}

	public int number() {
		return _number;
	}

	public VersionIndex index() {
		return _index;
	}
	
	public Provenance provenance() {
		return _provenance;
	}

	public long time() {
		return _time;
	}
	
	public String toString() {
		return "["+_name + ", "+ _number +","+ _time +"]";
	}
}
