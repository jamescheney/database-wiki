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

import java.util.Vector;

/** Represents a set of time points using a sequence of intervals.
 * Invariants we should maintain: 
 * 1.  The intervals are disjoint
 * 2.  The intervals are increasing (start1 < end1 < start2 < end2 ...) 
 * 3.  There are no gaps
 * @author jcheney
 *
 */
public class TimeSequence {
	/*
	 * Private Constants
	 */
	
	public static final String OpenIntervalChar = "*";
	
	
	/*
	 * Private variables
	 */
	
	private Vector<TimeInterval> _intervals;
	//private TimestampPrinter _timestampPrinter;
	private VersionIndex _index;
	
	/*
	 * Constructors
	 */
	

	/** Make set with single interval starting at time and ending at "now"
	 * 
	 * @param time
	 * @param timestampPrinter
	 */
	public TimeSequence(int time, VersionIndex index) {
		_index = index;
		_intervals = new Vector<TimeInterval>();
		_intervals.add(new TimeInterval(time));
	}

	/** Make set with single interval starting at start and ending at end
	 * 
	 * @param start
	 * @param end
	 * @param timestampPrinter
	 */
	public TimeSequence(int start, int end, VersionIndex index) {
		_index = index;
		_intervals = new Vector<TimeInterval>();
		_intervals.add(new TimeInterval(start, end));
	}
	
	/** Make a time sequence from a version
	 * 
	 * @param version
	 */
	public TimeSequence(Version version) {
		this(version.number(), version.index());
	}

	/** Make a time sequence from a vector of intervals
	 * 
	 * @param intervals
	 * @param timestampPrinter
	 */
	public TimeSequence(Vector<TimeInterval> intervals, VersionIndex index) {
		_intervals = intervals;
		_index = index;
	}

	
	/*
	 * Public Methods
	 */
	/** Tests whether there is a change between the given time and now
	 * 
	 */
	public boolean changedSince(int time) {
		TimeInterval interval = _intervals.lastElement();
		
		if (interval.isOpen()) {
			return (interval.start() > time);
		} else {
			return (interval.end() >= time);
		}
	}
	
	/** Tests whether the set contains a given time.
	 * 
	 * @param time
	 * @return
	 */
	public boolean contains(int time) {
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			if (_intervals.get(iInterval).contains(time)) {
				return true;
			}
		}
		return false;
	}

	/** Creates a new time sequence that is a copy of the current one plus an open interval starting at time
	 * 
	 * @param time
	 * @return
	 */
	public TimeSequence continueAt(int time) {
		Vector<TimeInterval> intervals = new Vector<TimeInterval>();
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			intervals.add(_intervals.get(iInterval));
		}
		intervals.add(new TimeInterval(time));
		return new TimeSequence(intervals, _index);
	}
	
	/** 
	 * Extend set with [start,end] provided there isn't already an interval starting at start
	 * @param start
	 * @param end
	 */
	public void elongate(int start, int end) {
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			if (_intervals.get(iInterval).start() == start) {
				return;
			}
		}
		_intervals.add(new TimeInterval(start, end));
	}
	
	/** Create a new TimeSequence that is a copy of this one except that the last 
	 * interval is adjusted to be [start,time], overriding the old end value.
	 * Typically used when the interval is open.
	 * 
	 * @param time
	 * @return
	 */
	public TimeSequence finishAt(int time) {
		Vector<TimeInterval> intervals = new Vector<TimeInterval>();
		for (int iInterval = 0; iInterval < _intervals.size() - 1; iInterval++) {
			intervals.add(_intervals.get(iInterval));
		}
		TimeInterval interval = _intervals.lastElement();
		intervals.add(new TimeInterval(interval.start(), time));
		return new TimeSequence(intervals, _index);
	}
	
	/** The first (smallest?) value 
	 * */
	public int firstValue() {
		return _intervals.get(0).start();
	}

	/** Creates a copy of the list of intervals
	 * FIXME #time: It doesn't seem necessary to make a copy, since the copy isn't used destructively anywhere.
	 * 
	 * @return
	 */
	public TimeInterval[] intervals() {
		TimeInterval[] intervals = new TimeInterval[_intervals.size()];
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			intervals[iInterval] = _intervals.get(iInterval);
		}
		return intervals;
	}

	/** 
	 * Determine whether a set is current, by checking whether the last interval
	 * ends "now"
	 * @return
	 */
	public boolean isCurrent() {
		return _intervals.lastElement().isOpen();
	}

	/** 
	 * Fetch the last interval in the set.
	 * @return
	 */
	public TimeInterval lastInterval() {
		return _intervals.lastElement();
	}
	
	/** 
	 * Fetch the last value, either -1 for "now" or the endpoint of the last interval.
	 * @return
	 */
	public int lastValue() {
		TimeInterval interval = _intervals.lastElement();
		if (interval.isOpen()) {
			return -1;
		} else {
			return interval.end();
		}
	}
	
	/** Builds a string of start-end pairs 
	 * 
	 * @return
	 */
	public String toIntString() {
		String text = _intervals.get(0).toIntString();
		if (_intervals.size() > 1) {
			for (int iInterval = 1; iInterval < _intervals.size(); iInterval++) {
				text = text + "," + _intervals.get(iInterval).toIntString();
			}
		}
		return text;
	}
	
	/** Builds a string representing the interval, using timestampPrinter to convert 
	 * version numbers to datetimes
	 * 
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	/*@Deprecated
	public String toPrintString() throws org.dbwiki.exception.WikiException {
		return _timestampPrinter.toString(this);
	}*/
	
	/** Builds a string representing the interval, using a VersionIndex
	 * 
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized String toPrintString() throws org.dbwiki.exception.WikiException {
		TimeInterval[] intervals = intervals();
		String text = getTextString(intervals[0]);
		for (int iInterval = 1; iInterval < intervals.length; iInterval++) {
			text = text + ", " + getTextString(intervals[iInterval]);
		}
		return text;
	}
	
	
	/** Parses a string of the form s-e,...,s-e to a time sequence
	 * 
	 * @param text
	 * @param timestampPrinter
	 * @return
	 */
	@Deprecated
	public static TimeSequence parseTimeSequence(String text, VersionIndex index) {
		
		Vector<TimeInterval> intervals = new Vector<TimeInterval>();
		
		int pos;
		while ((pos = text.indexOf(',')) != -1) {
			intervals.add(TimeInterval.parseInterval(text.substring(0, pos).trim()));
			text = text.substring(pos + 1).trim();
		}
		intervals.add(TimeInterval.parseInterval(text));
		return new TimeSequence(intervals,index);
	}
	
	/*
	 * Private Methods
	 */
	
	/** 
	 * FIXME: A bit hacky.
	 * 
	 */
	private String getTextString(TimeInterval interval) throws org.dbwiki.exception.WikiException {
		String text = _index.getByNumber(interval.start()).name();
		if (interval.isOpen()) {
			return text + "-now";
		} else if (interval.start() != interval.end()) {
			return text + "-" + _index.getByNumber(interval.end()).name();
		} else {
			return text;
		}
	}
}
