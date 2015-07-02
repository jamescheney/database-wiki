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

/** QueryNodeHandler for queries with a single variable. In this case the
 * root of the query variable tree is the only variable and the where clause
 * and select clause cam be evaluated immediately on the given database node.
 *
 * @author hmueller
 *
 */
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.query.xaql.QueryVariable;
import org.dbwiki.data.query.xaql.SelectClause;
import org.dbwiki.data.query.xaql.WhereClause;

public class SingleVariableQueryNodeHandler extends QueryRootNodeHandler {

	/*
	 * Private Variables
	 */
	
	private QueryVariable _variable;
	
	
	/*
	 * Constructors
	 */
	
	public SingleVariableQueryNodeHandler(SelectClause selectClause, WhereClause whereClause, QueryVariable variable, QueryNodeHandler consumer) {
		
		super(selectClause, whereClause, consumer);
		
		_variable = variable;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void handle(DatabaseElementNode node) {

		this.handle(new QueryNodeSet(_variable.name(), node));
	}
}
