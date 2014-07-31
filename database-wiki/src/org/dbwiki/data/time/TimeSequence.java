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

import org.dbwiki.exception.data.WikiDataException;

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
	
	/*
	 * Constructors
	 */
	
	/** Make a time sequence with an empty set of time intervals
	 * 
	 */
	public TimeSequence() {
		_intervals = new Vector<TimeInterval>();
	}
	
	/** Generate a time sequence from a text representation of the timestamp
	 * @param text
	 * @throws org.dbwiki.exception.WikiException
	 */
	
	public TimeSequence(String text) throws org.dbwiki.exception.WikiException {

		_intervals = parseTimeSequence(text);
		this.checkForConsistency();
	}
	
	/** Parses a string of the form s-e,...,s-e to a time sequence
	 * TODO: Clean this up to avoid string surgery
	 * @param text
	 * @return
	 */
	public static Vector<TimeInterval> parseTimeSequence(String text) {
		String tmp = text;

		Vector<TimeInterval> intervals = new Vector<TimeInterval>();
		
		int pos;
		while ((pos = tmp.indexOf(',')) != -1) {
			intervals.add(TimeInterval.parseInterval(tmp.substring(0, pos).trim()));
			tmp = tmp.substring(pos + 1).trim();
		}
		intervals.add(TimeInterval.parseInterval(tmp));
		return intervals;
	}
	

	
	
	/** Make set with single interval starting at time and ending at "now"
	 * 
	 * @param time
	 * @param timestampPrinter
	 */
	public TimeSequence(int time) {
		_intervals = new Vector<TimeInterval>();
		_intervals.add(new TimeInterval(time));
	}

	/** Make set with single interval starting at start and ending at end
	 * 
	 * @param start
	 * @param end
	 * @param timestampPrinter
	 */
	public TimeSequence(int start, int end) {
		_intervals = new Vector<TimeInterval>();
		_intervals.add(new TimeInterval(start, end));
	}
	
	/** Make a time sequence from a version
	 * 
	 * @param version
	 */
	public TimeSequence(Version version) {
		this(version.number());
	}

	/** Make a time sequence from a given set of time intervals
	 * 
	 * @param intervals
	 */
	public TimeSequence(Vector<TimeInterval> intervals) {
		_intervals = intervals;
	}

	/** Make a time sequence from a vector of intervals
	 * 
	 * @param intervals
	 * @param timestampPrinter
	 */
	public TimeSequence(Vector<TimeInterval> intervals, VersionIndex index) {
		_intervals = intervals;
	}

	
	/*
	 * Public Methods
	 */

	/** Adds the given time interval to the time sequence
	 * 
	 * @param interval
	 */
	public void add(TimeInterval interval) {
		
		if (_intervals.isEmpty()) {
			_intervals.add(interval);
		} else {
			if (!_intervals.lastElement().isOpen()) {
				if (_intervals.lastElement().end() > interval.start()) {
					if (interval.isOpen()) {
						_intervals.lastElement().extend(interval.end());
					} else {
						_intervals.lastElement().extend(Math.max(_intervals.lastElement().end(), interval.end()));
					}
				} else if (_intervals.lastElement().end() == (interval.start() - 1)) {
					_intervals.lastElement().extend(interval.end());
				} else {
					_intervals.add(interval);
				}
			}
		}
	}

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
		return new TimeSequence(intervals);
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
		return new TimeSequence(intervals);
	}
	
	/** The first (smallest?) value 
	 * */
	public int firstValue() {
		return _intervals.get(0).start();
	}

	/** Returns the time interval at position index
	 * 
	 * @param index
	 * @return
	 */
	public TimeInterval get(int index) {
		return _intervals.get(index);
	}
	
	/** Create a new TimeSequence that is an intersection of all the intervals
	 * of this TimeSequence and a given TimeSequence
	 * @param timestamp
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public TimeSequence intersect(TimeSequence timestamp) {
		
		Vector<TimeInterval> intervals = new Vector<TimeInterval>();
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			TimeInterval intervalI = this.get(iInterval);
			for (int jInterval = 0; jInterval < timestamp.size(); jInterval++) {
				TimeInterval intervalJ = timestamp.get(jInterval);
				if (intervalI.overlap(intervalJ)) {
					intervals.add(intervalI.intersect(intervalJ));
				}
			}
		}
		return new TimeSequence(intervals);
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
	 * Determine whether the timestamp is empty
	 * @return
	 */
	public boolean isEmpty() {
		return _intervals.isEmpty();
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

	public boolean overlap(TimeSequence timestamp) {
		
		if ((this.isCurrent()) && (timestamp.isCurrent())) {
			return true;
		}
		
		int idxI = 0;
		int idxJ = 0;
		
		while ((idxI < this.size()) && (idxJ < timestamp.size())) {
			TimeInterval intervalI = this.get(idxI);
			TimeInterval intervalJ = timestamp.get(idxJ);
			if (intervalI.overlap(intervalJ)) {
				return true;
			} else if (intervalI.start() < intervalJ.start()) {
				idxI++;
			} else {
				idxJ++;
			}
		}
		return false;
	}
	
	/** Returns the number of intervals in this time sequence
	 * 
	 * @return
	 */
	public int size() {
		return _intervals.size();
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
	
	public TimeSequence union(TimeSequence timestamp) {
		
		TimeSequence result = new TimeSequence();
		
		int idxI = 0;
		int idxJ = 0;
		
		while ((idxI < this.size()) && (idxJ < timestamp.size())) {
			TimeInterval intervalI = this.get(idxI);
			TimeInterval intervalJ = timestamp.get(idxJ);
			if (intervalI.contains(intervalJ)) {
				idxJ++;
			} else if (intervalJ.contains(intervalI)) {
				idxI++;
			} else if (intervalI.overlap(intervalJ)) {
				if ((intervalI.isOpen()) || (intervalJ.isOpen())) {
					result.add(new TimeInterval(Math.min(intervalI.start(), intervalJ.start())));
				} else {
					result.add(new TimeInterval(Math.min(intervalI.start(), intervalJ.start()), Math.max(intervalI.end(), intervalJ.end())));
				}
				idxI++;
				idxJ++;
			} else if (intervalI.start() < intervalJ.start()) {
				result.add(intervalI.copy());
				idxI++;
			} else {
				result.add(intervalJ.copy());
				idxJ++;
			}
		}
		while (idxI < this.size()) {
			result.add(this.get(idxI++).copy());
		}
		while (idxJ < timestamp.size()) {
			result.add(timestamp.get(idxJ++).copy());
		}
		
		return result;
	}

	/*
	 * Private Methods
	 */
	
	/** 
	 * FIXME: A bit hacky.
	 * 
	 */
	private void checkForConsistency() throws org.dbwiki.exception.WikiException {
		
		for (int iInterval = 1; iInterval < _intervals.size(); iInterval++) {
			if (_intervals.get(iInterval - 1).isOpen()) {
				throw new WikiDataException(WikiDataException.InvalidTimestamp, this.toIntString());
			}
			if (_intervals.get(iInterval).start() <= _intervals.get(iInterval - 1).end()) {
				throw new WikiDataException(WikiDataException.InvalidTimestamp, this.toIntString());
			}
		}
	}

	
}
