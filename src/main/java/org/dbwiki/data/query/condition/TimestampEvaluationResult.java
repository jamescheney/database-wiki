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
package org.dbwiki.data.query.condition;

/** Evaluation result for CONINCIDE statements. We need to keep track
 * of all the versions for which a condition evaluates to true in case
 * that multiple nodes match the tragte path.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.time.TimeSequence;

public class TimestampEvaluationResult {

	/*
	 * Private Variables
	 */
	
	private TimeSequence _timestamp = null;
	
	
	/*
	 * Public Methods
	 */

	public void intersect(TimeSequence timestamp) {
		
		if (_timestamp != null) {
			_timestamp = _timestamp.intersect(timestamp);
		} else {
			_timestamp = timestamp;
		}
	}
	
	public TimeSequence timestamp() {
		
		return _timestamp;
	}
	
	public void union(TimeSequence timestamp) {
	
		if (_timestamp != null) {
			_timestamp = _timestamp.union(timestamp);
		} else {
			_timestamp = timestamp;
		}
	}
}
