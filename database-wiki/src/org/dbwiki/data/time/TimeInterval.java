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

/** A struct containing a start and end point, with some methods for testing containment
 * and dealing with intervals ending at "now"
 * 
 * Question: do we interpret intervals as [start,end] or [start,end)?  Half-open intervals are nicer...
 * 
 * @author jcheney
 *
 */

public class TimeInterval {
	/*
	 * Private Variables
	 */
	
	private int _end;
	private int _start;
	
	/*
	 * Public Variables
	 */
	
	public static final String OpenIntervalChar = "*";
	/*
	 * Constructors
	 */
	
	/** Create new interval with given start and end points
	 * 
	 */
	public TimeInterval(int start, int end) {
		_start = start;
		_end = end;
	}
	
	/**
	 *  Create an open interval
	 * @param start
	 */
	public TimeInterval(int start) {
		this(start, -1);
	}
	
	
	/*
	 * Public Methods
	 */
	
	/**
	 * Test whether a time point is in a given interval.
	 */
	public boolean contains(int time) {
		return ((_start <= time) && (((_end >= time) && (_end >= _start)) || (_end == -1)));
	}

	/** The end point of the interval
	 * 
	 */
	public int end() {
		return _end;
	}
	
	/** 
	 * Boolean flag indicating whether the end of the interval is "now" 
	 */
	public boolean isOpen() {
		return (_end == -1);
	}
	
	/** The beginning of the interval
	 * 
	 */
	public int start() {
		return _start;
	}
	
	/** Converts interval to a string of the form start-end
	 * 
	 * @return
	 */
	public String toIntString() {
		if (_start == _end) {
			return String.valueOf(_start);
		} else if (_end == -1) {
			return _start + "-" + TimeSequence.OpenIntervalChar;
		} else {
			return _start + "-" + _end;
		}
	}
	
	/** Parses an interval from a  string of the form start-end
	 * Factory method
	 */
	public static TimeInterval parseInterval(String text) {
		int pos  = text.indexOf('-');
		if (pos != -1) {
			int start = Integer.parseInt(text.substring(0, pos));
			String end = text.substring(pos + 1);
			if (end.equals(OpenIntervalChar)) {
				return new TimeInterval(start);
			} else {
				return new TimeInterval(start, Integer.parseInt(end));
			}
		} else {
			int time = Integer.parseInt(text);
			return new TimeInterval(time, time);
		}
	}

	
	
}
