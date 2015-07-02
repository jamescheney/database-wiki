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

/** A WHERE lause is a collection of WHERE expressions.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.query.handler.QueryNodeSet;

public class WhereClause {

	/*
	 * Private Methods
	 */
	
	private Vector<WhereExpression> _elements;
	
	
	/*
	 * Constructors
	 */
	
	public WhereClause() {
		
		_elements = new Vector<WhereExpression>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(WhereExpression expression) {
		
		_elements.add(expression);
	}
	
	public boolean eval(QueryNodeSet nodeSet) {
		
		for (WhereExpression expression : _elements) {
			if (expression.eval(nodeSet) == false) {
				return false;
			}
		}
		return true;
	}
	
	public void listConditions(AttributeConditionListing listing) {
		for (WhereExpression expression : _elements) {
			expression.listConditions(listing);
		}
	}
}
