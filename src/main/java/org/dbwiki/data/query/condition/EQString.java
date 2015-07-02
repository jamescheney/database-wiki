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

/** Value operator for equality of STRINGs.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.schema.AttributeSchemaNode;

public class EQString extends ValueOp {

	/*
	 * Private Variables
	 */
	
	private String _value;
	
	
	/*
	 * Constructors
	 */
	
	public EQString(String value) {
		
		_value = value;
	}
	
	
	/*
	 * Public Methods
	 */

	public AttributeCondition getQueryCondition(AttributeSchemaNode entity, boolean isNegated) {
		
		return new AttributeValueCondition(entity, AttributeCondition.EQ, _value, isNegated);
	}

	public boolean eval(String value) {

		return _value.equals(value);
	}

	public String toString() {
		
		return "= '" + _value + "'";
	}
	
	public String value() {
		
		return _value;
	}
}
