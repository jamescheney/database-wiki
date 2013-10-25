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

/** Value operator MATCHES (uses Java regular expressions).
 * 
 * @author hmueller
 *
 */
import java.util.regex.Pattern;

import org.dbwiki.data.schema.AttributeSchemaNode;

public class MATCHESString extends ValueOp {

	/*
	 * Private Variables
	 */
	
	private Pattern _pattern;
	
	
	/*
	 * Constructors
	 */
	
	public MATCHESString(String value) {
		
		_pattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE );
	}
	
	
	/*
	 * Public Methods
	 */
	
	public AttributeCondition getQueryCondition(AttributeSchemaNode entity, boolean isNegated) {
		
		return null;
	}

	public boolean eval(String value) {

        return _pattern.matcher(value).find();
	}
	
	public String toString() {
		
		return "MATCHES '" + _pattern.toString() + "'";
	}
	
	public String value() {
		
		return null;
	}
}
