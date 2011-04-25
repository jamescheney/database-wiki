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

import java.util.Vector;

import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.Timestamp;
import org.dbwiki.data.time.TimestampPrinter;
import org.dbwiki.data.time.Version;

public class TimeSequence implements Timestamp {
	/*
	 * Private Constants
	 */
	
	public static final String OpenIntervalChar = "*";
	
	
	/*
	 * Private variables
	 */
	
	private Vector<TimeSequenceInterval> _intervals;
	private TimestampPrinter _timestampPrinter;
	
	/*
	 * Constructors
	 */
	
	public TimeSequence(String text, TimestampPrinter timestampPrinter) {
		_timestampPrinter = timestampPrinter;
		
		_intervals = new Vector<TimeSequenceInterval>();
		
		int pos;
		while ((pos = text.indexOf(',')) != -1) {
			_intervals.add(this.parseInterval(text.substring(0, pos).trim()));
			text = text.substring(pos + 1).trim();
		}
		_intervals.add(this.parseInterval(text));
	}

	public TimeSequence(int time, TimestampPrinter timestampPrinter) {
		_timestampPrinter = timestampPrinter;
		_intervals = new Vector<TimeSequenceInterval>();
		_intervals.add(new TimeSequenceInterval(time));
	}

	public TimeSequence(int start, int end, TimestampPrinter timestampPrinter) {
		_timestampPrinter = timestampPrinter;
		_intervals = new Vector<TimeSequenceInterval>();
		_intervals.add(new TimeSequenceInterval(start, end));
	}
	
	public TimeSequence(Version version) {
		this(version.number(), version.printer());
	}

	public TimeSequence(Vector<TimeSequenceInterval> intervals, TimestampPrinter timestampPrinter) {
		_intervals = intervals;
		_timestampPrinter = timestampPrinter;
	}

	
	/*
	 * Public Methods
	 */
	
	public boolean changedSince(int time) {
		TimeSequenceInterval interval = _intervals.lastElement();
		
		if (interval.isOpen()) {
			return (interval.start() > time);
		} else {
			return (interval.end() >= time);
		}
	}
	
	public boolean contains(int time) {
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			if (_intervals.get(iInterval).contains(time)) {
				return true;
			}
		}
		return false;
	}

	public Timestamp continueAt(int time) {
		Vector<TimeSequenceInterval> intervals = new Vector<TimeSequenceInterval>();
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			intervals.add(_intervals.get(iInterval));
		}
		intervals.add(new TimeSequenceInterval(time));
		return new TimeSequence(intervals, _timestampPrinter);
	}
	
	public void elongate(int start, int end) {
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			if (_intervals.get(iInterval).start() == start) {
				return;
			}
		}
		_intervals.add(new TimeSequenceInterval(start, end));
	}
	
	public Timestamp finishAt(int time) {
		Vector<TimeSequenceInterval> intervals = new Vector<TimeSequenceInterval>();
		for (int iInterval = 0; iInterval < _intervals.size() - 1; iInterval++) {
			intervals.add(_intervals.get(iInterval));
		}
		TimeSequenceInterval interval = _intervals.lastElement();
		intervals.add(new TimeSequenceInterval(interval.start(), time));
		return new TimeSequence(intervals, _timestampPrinter);
	}
	
	public int firstValue() {
		return _intervals.get(0).start();
	}

	public TimeInterval[] intervals() {
		TimeInterval[] intervals = new TimeInterval[_intervals.size()];
		for (int iInterval = 0; iInterval < _intervals.size(); iInterval++) {
			intervals[iInterval] = _intervals.get(iInterval);
		}
		return intervals;
	}

	public boolean isCurrent() {
		return _intervals.lastElement().isOpen();
	}

	public TimeSequenceInterval lastInterval() {
		return _intervals.lastElement();
	}
	
	public int lastValue() {
		TimeInterval interval = _intervals.lastElement();
		if (interval.isOpen()) {
			return -1;
		} else {
			return interval.end();
		}
	}
	
	public String toIntString() {
		String text = _intervals.get(0).toIntString();
		if (_intervals.size() > 1) {
			for (int iInterval = 1; iInterval < _intervals.size(); iInterval++) {
				text = text + "," + _intervals.get(iInterval).toIntString();
			}
		}
		return text;
	}
	
	public String toPrintString() throws org.dbwiki.exception.WikiException {
		return _timestampPrinter.toString(this);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private TimeSequenceInterval parseInterval(String text) {
		int pos  = text.indexOf('-');
		if (pos != -1) {
			int start = Integer.parseInt(text.substring(0, pos));
			String end = text.substring(pos + 1);
			if (end.equals(OpenIntervalChar)) {
				return new TimeSequenceInterval(start);
			} else {
				return new TimeSequenceInterval(start, Integer.parseInt(end));
			}
		} else {
			int time = Integer.parseInt(text);
			return new TimeSequenceInterval(time, time);
		}
	}
}
