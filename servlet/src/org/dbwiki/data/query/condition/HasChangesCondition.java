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

/** Implements the HAS CHANGES predicate. Evaluates to true if a given
 * node has a timestamp that is different from it's parents timestamp.
 * If an additional ProvenanceCondition is given then the timestamp
 * has to satisfy the versions in the provenance condition, i.e., has
 * to have an interval that starts at one of the versions in the
 * provenance condition.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.database.DatabaseElementNode;

import org.dbwiki.data.query.xpath.XPath;

public class HasChangesCondition extends ProvenancePathCondition {

	/*
	 * Constructors
	 */
	
	public HasChangesCondition(XPath targetPath, int quantifier, boolean negated, ProvenanceCondition provenanceCondition) {
		
		super(targetPath, quantifier, negated, provenanceCondition);
	}
	
	public HasChangesCondition(XPath targetPath, int quantifier, boolean negated) {
		
		this(targetPath, quantifier, negated, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void eval(DatabaseElementNode node, EvaluationResult result) {
		
		if (node.hasTimestamp()) {
			result.evaluationResult((!this.isRootTimestamp(node.getTimestamp())) && (this.matchesProvenanceCondition(node.getTimestamp())));
		} else {
			result.evaluationResult(false);
		}
	}
	
	public void evalTimestamp(DatabaseElementNode node, TimestampEvaluationResult evalResult) {

	}
}
