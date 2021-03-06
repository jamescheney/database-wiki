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
package org.dbwiki.data.query.handler;

/** Provides the method to evaluate the where clause and select clause on a set of
 * nodes that represent a valid binding for all variables in the query.
*
* @author hmueller
*
*/
import org.dbwiki.data.query.xaql.SelectClause;
import org.dbwiki.data.query.xaql.WhereClause;

public abstract class QueryRootNodeHandler implements QueryNodeHandler {

	/*
	 * Private Variables
	 */
	
	private QueryNodeHandler _consumer;
	private SelectClause _selectClause;
	private WhereClause _whereClause;

	
	/*
	 * Constructors
	 */
	
	public QueryRootNodeHandler(SelectClause selectClause, WhereClause whereClause, QueryNodeHandler consumer) {
		
		_selectClause = selectClause;
		_whereClause = whereClause;
		_consumer = consumer;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void handle(QueryNodeSet nodeSet) {
		
		boolean output = true;
		if (_whereClause != null) {
			output = _whereClause.eval(nodeSet);
		}
		if (output) {
			_selectClause.consume(nodeSet, _consumer);
		}
	}
}
