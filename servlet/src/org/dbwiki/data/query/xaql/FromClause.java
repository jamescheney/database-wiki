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

/** FROM clause in a XAQL query.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.query.condition.AttributeConditionListing;

public class FromClause {

	/*
	 * Private Variables
	 */
	
	private QueryVariable _rootVariable;
	QueryVariableListing _variableListing;
	
	
	/*
	 * Constructors
	 */
	
	public FromClause(QueryVariable rootVariable, QueryVariableListing variableListing) {
		
		_rootVariable = rootVariable;
		_variableListing = variableListing;
	}

	
	/*
	 * Public Methods
	 */
	
	public void listConditions(AttributeConditionListing listing) {
		for (int iVariable = 0; iVariable < _variableListing.size(); iVariable++) {
			_variableListing.get(iVariable).targetPath().listConditions(listing);
		}
	}

	public QueryVariable rootVariable() {
		
		return _rootVariable;
	}
	
	public QueryVariableListing variables() {
		
		return _variableListing;
	}
}
