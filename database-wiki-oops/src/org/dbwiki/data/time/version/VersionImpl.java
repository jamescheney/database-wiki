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
package org.dbwiki.data.time.version;

import org.dbwiki.data.provenance.Provenance;

import org.dbwiki.data.time.TimestampPrinter;
import org.dbwiki.data.time.Version;

public class VersionImpl implements Version {
	/*
	 * Private Variables
	 */
	
	private String _name;
	private int _number;
	private TimestampPrinter _printer;
	private Provenance _provenance;
	private long _time;
	
	
	/*
	 * Constructors
	 */
	
	public VersionImpl(int number, String name, long time, Provenance provenance, TimestampPrinter printer) {
		_name = name;
		_number = number;
		_provenance = provenance;
		_time = time;
		_printer = printer;
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

	public TimestampPrinter printer() {
		return _printer;
	}
	
	public Provenance provenance() {
		return _provenance;
	}

	public long time() {
		return _time;
	}
}
