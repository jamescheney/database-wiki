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

/** Interface that represents conditions within a XAQL query.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.time.TimeSequence;

public interface Condition {

	/*
	 * Interface Methods
	 */
	
	/** Evaluate a condition against the given node.
	 * 
	 * @param node against with the condition is evaluated.
	 * @return true, if the node satisfies the condition, false otherwise.
	 */
	public boolean eval(DatabaseElementNode node);
	
	/** Evaluate a condition against the given node and return the timestamp
	 * of versions for which the condition evaluates to true. This method is
	 * used for COINCIDE statements where we need to ensure that all conditions
	 * are true within at least one version of the database.
	 * 
	 * @param node against with the condition is evaluated.
	 * @return Sequence of versions numbers for which the evaluation is true, null
	 * if the condition is not satisfied in any version.
	 */
	public TimeSequence evalTimestamp(DatabaseElementNode node);
	
	/** Lists all attribute conditions (including nested conditions). This
	 * method is needed to retrieve all attribute conditions for a XAQL query
	 * during query evaluation (especially to generate the SQL statement for
	 * the RDBMS implementation that retireves all the candidate entries).
	 * 
	 * @param listing The current listing of attribute conditions to which the
	 * attribute conditions of this class (including nested conditions) are added. 
	 */
	public void listConditions(AttributeConditionListing listing);
}
