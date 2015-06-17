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
package org.dbwiki.data.query.xaql;

/** Single condition in a XAQL WHERE clause.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.query.condition.Condition;
import org.dbwiki.data.query.handler.QueryNodeSet;
import org.dbwiki.data.time.TimeSequence;

public class WhereCondition implements WhereExpression {

	/*
	 * Private Variables
	 */
	
	private Condition _condition;
	private String _variableName;
	
	
	/*
	 * Constructors
	 */
	
	public WhereCondition(String variableName, Condition condition) {
		
		_variableName = variableName;
		_condition = condition;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean eval(QueryNodeSet nodeSet) {
		
		return _condition.eval(nodeSet.get(_variableName));
	}
	
	public TimeSequence evalTimestamp(QueryNodeSet nodeSet) {
		
		return _condition.evalTimestamp(nodeSet.get(_variableName));
	}
	
	public void listConditions(AttributeConditionListing listing) {
		
		_condition.listConditions(listing);
	}
}
