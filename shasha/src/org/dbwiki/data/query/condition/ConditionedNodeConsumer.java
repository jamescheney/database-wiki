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

/** Implements a QueryNodeHandler for conditions, i.e., the given
 * condition is evaluated against a query node and if the condition
 * is satisfied evaluation is continued for the query node.
 * 
 * @author hmueller
 *
 */
// TODO: Rename this to ConditionedNodeHandler; move to handler package

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.query.handler.QueryNodeHandler;

public class ConditionedNodeConsumer implements QueryNodeHandler {

	/*
	 * Private Variables
	 */
	
	private Condition _condition;
	private QueryNodeHandler _nodeHandler;
	
	
	/*
	 * Constructors
	 */
	
	public ConditionedNodeConsumer(QueryNodeHandler nodeHandler, Condition condition) {
		
		_nodeHandler = nodeHandler;
		_condition = condition;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void handle(DatabaseElementNode node) {

		if (_condition.eval(node)) {
			_nodeHandler.handle(node);
		}
	}
}
