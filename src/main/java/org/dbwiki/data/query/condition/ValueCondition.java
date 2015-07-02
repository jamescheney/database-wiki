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

/** A path condition that has a associated ValueOperator to be
 * evaluated on the target node(s).
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.query.xpath.XPath;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.data.WikiQueryException;

public class ValueCondition extends PathCondition {

	/*
	 * Private Variables
	 */
	
	private int _evalQuantifier;
	private ValueOp _operator;
	
	
	/*
	 * Constructors
	 */
	
	public ValueCondition(XPath targetPath, int quantifier, boolean negated, ValueOp operator, int evalQuantifier) throws org.dbwiki.exception.WikiException {
		
		super(targetPath, quantifier, negated);
		
		if (!targetPath.lastElement().entity().isAttribute()) {
			throw new WikiQueryException(WikiQueryException.InvalidPathExpression, "Cannot have value condition for non-attribute node " + targetPath.lastElement().entity());
		}
		_evalQuantifier = evalQuantifier;
		_operator = operator;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void eval(DatabaseElementNode node, EvaluationResult result) {

		DatabaseAttributeNode attribute = (DatabaseAttributeNode)node;
		
		EvaluationResult evalResult = new EvaluationResult();
		
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			boolean opResult = _operator.eval(attribute.value().get(iValue).value());
			evalResult.evaluationResult(opResult);
			if ((opResult) && (_evalQuantifier == Quantifier.FOR_ANY)) {
				break;
			} else if ((!opResult) && (_evalQuantifier == Quantifier.FOR_ALL)) {
				break;
			}
		}
		if (_evalQuantifier == Quantifier.FOR_ALL) {
			result.evaluationResult(evalResult.hasAllPositiveEvaluations());
		} else {
			result.evaluationResult(evalResult.hasAnyPositiveEvaluation());
		}
	}
	
	public void evalTimestamp(DatabaseElementNode node, TimestampEvaluationResult evalResult) {
		
		DatabaseAttributeNode attribute = (DatabaseAttributeNode)node;
		
		TimeSequence timestamp = null;
		
		boolean hasAllPositiveEvaluations = true;
		
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			boolean opResult = _operator.eval(attribute.value().get(iValue).value());
			if (opResult) {
				if (timestamp != null) {
					timestamp = timestamp.union(attribute.value().get(iValue).getTimestamp());
				} else {
					timestamp = attribute.value().get(iValue).getTimestamp();
				}
			} else {
				hasAllPositiveEvaluations = false;
				if (_evalQuantifier == Quantifier.FOR_ALL) {
					break;
				}
			}
		}
		if (_evalQuantifier == Quantifier.FOR_ALL) {
			if (hasAllPositiveEvaluations) {
				evalResult.union(timestamp);
			}
		} else {
			evalResult.union(timestamp);
		}
	}
	
	public void listConditions(AttributeConditionListing listing) {
		super.listConditions(listing);
		listing.add(_operator.getQueryCondition((AttributeSchemaNode)this.targetPath().lastElement().entity(), this.isNegated()));
	}
}
