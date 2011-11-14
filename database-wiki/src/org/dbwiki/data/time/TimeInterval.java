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

	/** Returns true if the time interval contains (subsumes) the given time interval
	 * 
	 * @param interval
	 * @return
	 */
	public boolean contains(TimeInterval interval) {
		
		if (this.start() <= interval.start()) {
			if (this.isOpen()) {
				return true;
			} else if (interval.isOpen()) {
				return false;
			} else if (this.end() >= interval.end()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/** Returns a copy of this time interval
	 * 
	 * @return
	 */
	public TimeInterval copy() {
		
		return new TimeInterval(_start, _end);
	}

	/** The end point of the interval
	 * 
	 */
	public int end() {
		return _end;
	}
	
	/** Extends the time interval by setting it's end value to
	 * the given time
	 * 
	 * @param time
	 */
	public void extend(int time) {
		
		_end = time;
	}

	/** Returns a new time interval that is the intersection of this time interval
	 * and a given time interval
	 * 
	 * @param interval
	 * @return
	 * @throws au.csiro.svs.SVSException
	 */
	public TimeInterval intersect(TimeInterval interval) {
		
		int start = Math.max(this.start(), interval.start());
		if ((this.isOpen()) && (interval.isOpen())) {
			return new TimeInterval(start);
		} else {
			if (this.isOpen()) {
				return new TimeInterval(start, interval.end());
			} else if (interval.isOpen()) {
				return new TimeInterval(start, this.end());
			} else {
				return new TimeInterval(start, Math.min(this.end(), interval.end()));
			}
		}
	}

	/** 
	 * Boolean flag indicating whether the end of the interval is "now" 
	 */
	public boolean isOpen() {
		return (_end == -1);
	}
	
	/** Returns true if this time interval and the given time interval overlap
	 * 
	 * @param interval
	 * @return
	 */
	public boolean overlap(TimeInterval interval) {
		
		if ((this.start() == interval.start()) || (this.start() == interval.end()) || (this.end() == interval.start()) || (this.end() == interval.end())) {
			return true;
		}
		
		if (this.start() <= interval.start()) {
			if (this.isOpen()) {
				return true;
			} else if (this.end() >= interval.start()) {
				return true;
			} else {
				return false;
			}
		} else if (this.start() >= interval.start()) {
			if (interval.isOpen()) {
				return true;
			} else if (interval.end() >= this.start()) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
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
