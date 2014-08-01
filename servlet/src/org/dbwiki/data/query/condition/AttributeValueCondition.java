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

/** Attribute condition with a single value (opposed to AttributeInCondition
 * which may have multiple values). 
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.schema.AttributeSchemaNode;

public class AttributeValueCondition extends AttributeCondition {

	/*
	 * Private Variable
	 */
	
	private String _value;
	
	
	/*
	 * Constructors
	 */
	
	public AttributeValueCondition(AttributeSchemaNode entity, byte type, String value, boolean isNegated) {
		super(entity, type, isNegated);
		
		_value = value;
	}
	
	
	/*
	 * Public Methods 
	 */
	
	public void listValues(Vector<String> parameters) {
		
		parameters.add(_value);
	}
	
	public String sqlPreparedStatement() {
		
		if (this.isNegated()) {
			if (this.isEQ()) {
				return "<> ?";
			} else if (this.isGEQ()) {
				return "< ?";
			} else if (this.isGT()) {
				return "<= ?";
			} else if (this.isLEQ()) {
				return "> ?";
			} else if (this.isLIKE()) {
				return "NOT LIKE ?";
			} else if (this.isLT()) {
				return ">= ?";
			} else if (this.isNEQ()) {
				return "= ?";
			} else {
				return null;
			}
		} else {
			if (this.isEQ()) {
				return "= ?";
			} else if (this.isGEQ()) {
				return ">= ?";
			} else if (this.isGT()) {
				return "> ?";
			} else if (this.isLEQ()) {
				return "<= ?";
			} else if (this.isLIKE()) {
				return "LIKE ?";
			} else if (this.isLT()) {
				return "< ?";
			} else if (this.isNEQ()) {
				return "<> ?";
			} else {
				return null;
			}
		}
	}
	
	public String value() {
		return _value;
	}
}
