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

/** Implements a condition with given target path.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;

import org.dbwiki.data.query.xpath.XPath;
import org.dbwiki.data.query.xpath.XPathEvaluator;
import org.dbwiki.data.query.xpath.XPathTimestampEvaluator;

import org.dbwiki.data.time.TimeSequence;

public abstract class PathCondition implements Condition, ConditionEvaluator {

	/*
	 * Private Variables
	 */
	
	private boolean _negated;
	private int _quantifier;
	private XPath _targetPath;
	
	
	/*
	 * Constructors
	 */
	
	public PathCondition(XPath targetPath, int quantifier, boolean negated) {
	
		_targetPath = targetPath;
		_quantifier = quantifier;
		_negated = negated;
	}

	
	/*
	 * Public Methods
	 */
	
	@Override
	public boolean eval(DatabaseElementNode node) {

		EvaluationResult result = new EvaluationResult();
		
		new XPathEvaluator(result).eval((DatabaseGroupNode)node, _targetPath, this);
		
		if (_quantifier == Quantifier.FOR_ALL) {
			if (_negated) {
				return !result.hasAllPositiveEvaluations();
			} else {
				return result.hasAllPositiveEvaluations();
			}
		} else {
			if (_negated) {
				return !result.hasAnyPositiveEvaluation();
			} else {
				return result.hasAnyPositiveEvaluation();
			}
		}
	}
	
	@Override
	public TimeSequence evalTimestamp(DatabaseElementNode node) {

		TimestampEvaluationResult result = new TimestampEvaluationResult();
		
		new XPathTimestampEvaluator(result).evalTimestamp((DatabaseGroupNode)node, _targetPath, this);
		
		return result.timestamp();
	}
		
	public boolean isNegated() {
		
		return _negated;
	}
	
	@Override
	public void listConditions(AttributeConditionListing listing) {
		_targetPath.listConditions(listing);
	}

	public XPath targetPath() {
		
		return _targetPath;
	}
	
	@Override
	public String toString() {
		
		String text = "[";
		if (_negated) {
			text = text + " NOT";
		}
		if (_quantifier == Quantifier.FOR_ALL) {
			text = text + " FOR ALL";
		}
		text = text + " " + _targetPath.toString();
		return text + "]";
	}
}
