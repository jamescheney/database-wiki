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
package org.dbwiki.data.query.xpath;

/** SubPathCondition in a XPath expression, e.g., .../NAME[SUBNAME = '...']
*
* @author hmueller
*
*/
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.query.condition.Condition;

public class SubPathCondition extends XPathCondition {

	/*
	 * Private Variables
	 */
	
	private Condition _condition;
	
	
	/*
	 * Constructors
	 */
	
	public SubPathCondition(Condition condition) {
	
		_condition = condition;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean isIndexCondition() {
		
		return false;
	}
	
	public void listConditions(AttributeConditionListing listing) {
		_condition.listConditions(listing);
	}
	
	public boolean matches(DatabaseElementNode node) {
		
		return _condition.eval(node);
	}
	
	public String toString() {
		
		return _condition.toString();
	}
}
