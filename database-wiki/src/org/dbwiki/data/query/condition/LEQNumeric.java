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

/** Value operator <= for numeric values.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.schema.AttributeSchemaNode;

public class LEQNumeric extends ValueOp {

	/*
	 * Private Variables
	 */
	
	private double _value;
	
	
	/*
	 * Constructors
	 */
	
	public LEQNumeric(double value) {
		
		_value = value;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public AttributeCondition getQueryCondition(AttributeSchemaNode entity, boolean isNegated) {
		
		return null;
	}

	public boolean eval(String value) {

		try {
			return (_value >= Double.parseDouble(value));
		} catch (java.lang.NumberFormatException nfe) {
			return false;
		}
	}
	
	public String toString() {
		
		return "<= " + _value;
	}
	
	public String value() {
		
		return String.valueOf(_value);
	}
}
