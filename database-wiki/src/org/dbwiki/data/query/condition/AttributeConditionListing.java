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

/** Lists the attribute conditions within a XAQL query statement. These conditions
 * are used to generate the SQL statement which retireves the candidate entries 
 * during query evaluation. 
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

public class AttributeConditionListing {

	/*
	 * Private Variables
	 */
	
	private Vector<AttributeCondition> _conditions;
	
	
	/*
	 * Constructors
	 */
	
	public AttributeConditionListing() {
		_conditions = new Vector<AttributeCondition>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(AttributeCondition condition) {
		// The MATCHES condition currently cannot be translated into a SQL query due
		// to the general lack of support for regular expressions in SQL queries. Thus,
		// queries with MATCHES conditions add null conditions.
		if (condition != null) {
			_conditions.add(condition);
		}
	}
	
	public AttributeCondition get(int index) {
		return _conditions.get(index);
	}
	
	public int size() {
		return _conditions.size();
	}
}
