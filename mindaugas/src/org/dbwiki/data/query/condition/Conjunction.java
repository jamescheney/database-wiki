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

/** Conjunction of conditions.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.time.TimeSequence;

public class Conjunction implements Condition {

	/*
	 * Private Variables
	 */
	
	private Vector<Condition> _conditions;
	
	
	/*
	 * Constructors
	 */
	
	public Conjunction() {
		
		_conditions = new Vector<Condition>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(Condition condition) {
		
		_conditions.add(condition);
	}
	
	public boolean eval(DatabaseElementNode node) {

		for (Condition condition : _conditions) {
			if (!condition.eval(node)) {
				return false;
			}
		}
		return true;
	}
	
	public TimeSequence evalTimestamp(DatabaseElementNode node) {

		TimeSequence timestamp = null;
		for (Condition condition : _conditions) {
			TimeSequence t = condition.evalTimestamp(node);
			if (t != null) {
				if (timestamp != null) {
					timestamp = timestamp.intersect(t);
					if (timestamp.isEmpty()) {
						return null;
					}
				} else {
					timestamp = t;
				}
			}
		}
		return timestamp;
	}
	
	public void listConditions(AttributeConditionListing listing) {
		for (Condition condition : _conditions) {
			condition.listConditions(listing);
		}
	}

}
