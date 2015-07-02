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

/** Keep track of statistics during query evaluation. For 'FOR ALL' quantifiers
 * we need to keep track how many times a condition was evaluated and how many times
 * it evaluated to true.
 * 
 * @author hmueller
 *
 */
public class EvaluationResult {

	/*
	 * Private Variables
	 */
	
	private int _negativeEvaluations = 0;
	private int _positiveEvaluations = 0;
	
	
	/*
	 * Public Methods
	 */

	public boolean hasAllPositiveEvaluations() {
		
		return ((_positiveEvaluations > 0) && (_negativeEvaluations == 0));
	}
	public boolean hasAnyPositiveEvaluation() {
		
		return (_positiveEvaluations > 0);
	}
	
	public void evaluationResult(boolean positiveEvaluation) {
		
		if (positiveEvaluation) {
			_positiveEvaluations++;
		} else {
			_negativeEvaluations++;
		}
	}
}
