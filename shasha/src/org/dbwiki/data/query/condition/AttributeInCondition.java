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

/** Represents a IN condition. This condition differs from all other AttributeCondition's
 * in that it may have more that one value associated with it. 
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.schema.AttributeSchemaNode;

public class AttributeInCondition extends AttributeCondition {

	/*
	 * Private Variables
	 */
	
	private Vector<String> _values;
	
	
	/*
	 * Constructors
	 */
	
	public AttributeInCondition(AttributeSchemaNode entity, Vector<String> values, boolean isNegated) {
		super(entity, IN, isNegated);
		
		_values = values;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void listValues(Vector<String> parameters) {
		
		for (int iValue = 0; iValue < _values.size(); iValue++) {
			parameters.add(_values.get(iValue));
		}
	}

	@Override
	public String sqlPreparedStatement() {
	
		String parameters = "(?";
		for (int iValue = 1; iValue < _values.size(); iValue++) {
			parameters = parameters + ", ?";
		}
		parameters = parameters + ")";
		if (this.isNegated()) {
			return "NOT IN " + parameters;
		} else {
			return "IN " + parameters;
		}
	}
	
	public Vector<String> values() {
		return _values;
	}
}
