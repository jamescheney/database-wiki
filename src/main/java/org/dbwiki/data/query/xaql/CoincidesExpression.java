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

/** A COINCIDES expression in a XAQL WHERE clause.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.query.condition.TimestampEvaluationResult;
import org.dbwiki.data.query.handler.QueryNodeSet;
import org.dbwiki.data.time.TimeSequence;

public class CoincidesExpression implements WhereExpression {

	/*
	 * Private Variables
	 */
	
	private Vector<WhereCondition> _conditions;
	private boolean _negated;
	
	/*
	 * Constructors
	 */
	
	public CoincidesExpression(boolean negated) {
		
		_negated = negated;
		
		_conditions = new Vector<WhereCondition>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(WhereCondition condition) {
		
		_conditions.add(condition);
	}
	
	public boolean eval(QueryNodeSet nodeSet) {
		
		TimestampEvaluationResult evalResult = this.getEvalResult(nodeSet);
		if (evalResult != null) {
			if (evalResult.timestamp() != null) {
				boolean emptyResult = evalResult.timestamp().isEmpty();
				if (_negated) {
					return emptyResult;
				} else 
					return !emptyResult;
			} else {
				return _negated;
			}
		} else {
			return _negated;
		}
	}
	
	public TimeSequence evalTimestamp(QueryNodeSet nodeSet) {
		
		TimestampEvaluationResult evalResult = this.getEvalResult(nodeSet);
		if (evalResult != null) {
			if (evalResult.timestamp() != null) {
				return evalResult.timestamp();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void listConditions(AttributeConditionListing listing) {
		for (WhereCondition condition : _conditions) {
			condition.listConditions(listing);
		}
	}
	
	/*
	 * Private Methods
	 */
	
	private TimestampEvaluationResult getEvalResult(QueryNodeSet nodeSet) {
		
		TimestampEvaluationResult evalResult = new TimestampEvaluationResult();
		
		for (WhereCondition condition : _conditions) {
			TimeSequence timestamp = condition.evalTimestamp(nodeSet);
			if (timestamp != null) {
				evalResult.intersect(timestamp);
				if (evalResult.timestamp().isEmpty()) {
					return null;
				}
			} else {
				return null;
			}
		}
		return evalResult;
	}
}
