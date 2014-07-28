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

/** Transform timestamps into strings
 * 
 * @author hmueller
 *
 */

public class TimestampPrinter {

	/*
	 * Private Variables
	 */
	
	private VersionIndex _versionIndex;
	
	
	/*
	 * Constructors
	 */
	
	public TimestampPrinter(VersionIndex versionIndex) {
		
		_versionIndex = versionIndex;
	}
	
	
	/*
	 * Public Methods
	 */
	
	/** Builds a string representing the interval, using a VersionIndex
	 * 
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized String toPrintString(TimeSequence timestamp) throws org.dbwiki.exception.WikiException {
		TimeInterval[] intervals = timestamp.intervals();
		String text = getTextString(intervals[0]);
		for (int iInterval = 1; iInterval < intervals.length; iInterval++) {
			text = text + ", " + getTextString(intervals[iInterval]);
		}
		return text;
	}
	
	
	/*
	 * Private Methods
	 */
	
	/** Transforms a time interval into a string
	 * 
	 * @param interval
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	private String getTextString(TimeInterval interval) throws org.dbwiki.exception.WikiException {
		String text = _versionIndex.getByNumber(interval.start()).name();
		if (interval.isOpen()) {
			return text + "-now";
		} else if (interval.start() != interval.end()) {
			return text + "-" + _versionIndex.getByNumber(interval.end()).name();
		} else {
			return text;
		}
	}
}
