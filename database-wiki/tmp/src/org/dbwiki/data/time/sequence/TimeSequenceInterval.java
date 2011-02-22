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
package org.dbwiki.data.time.sequence;

import org.dbwiki.data.time.TimeInterval;

public class TimeSequenceInterval implements TimeInterval {
	/*
	 * Private Variables
	 */
	
	private int _end;
	private int _start;
	
	
	/*
	 * Constructors
	 */
	
	public TimeSequenceInterval(int start, int end) {
		_start = start;
		_end = end;
	}
	
	public TimeSequenceInterval(int start) {
		this(start, -1);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean contains(int time) {
		return ((_start <= time) && (((_end >= time) && (_end >= _start)) || (_end == -1)));
	}

	public int end() {
		return _end;
	}
	
	public boolean isOpen() {
		return (_end == -1);
	}
	
	public int start() {
		return _start;
	}
	
	public String toIntString() {
		if (_start == _end) {
			return String.valueOf(_start);
		} else if (_end == -1) {
			return _start + "-" + TimeSequence.OpenIntervalChar;
		} else {
			return _start + "-" + _end;
		}
	}
}
