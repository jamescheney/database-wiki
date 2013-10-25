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

/** Condition for attribute node in a XAQL statement. The list of attribute
 * conditions is currently used to generate the SQL query statement to retrieve the
 * candidate entries in query evaluation. 
 * 
 * FIXME Should be related only to the RDBMS implementation of the Database Wiki.
 * When using XArch to maintain the data the sqlProparedStatement is not needed.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.schema.AttributeSchemaNode;

public abstract class AttributeCondition {

	/*
	 * Public Constants
	 */
	
	public static final byte EQ = 0;
	public static final byte NEQ = 1;
	public static final byte GT = 2;
	public static final byte GEQ = 3;
	public static final byte LT = 4;
	public static final byte LEQ = 5;
	public static final byte IN = 6;
	public static final byte LIKE = 7;
	public static final byte INDEX = 8;
	

	/*
	 * Private Variables
	 */
	
	private AttributeSchemaNode _entity;
	private boolean _isNegated;
	private byte _type;
	
	
	/*
	 * Constructors
	 */
	
	public AttributeCondition(AttributeSchemaNode entity, byte type, boolean isNegated) {
		
		_entity = entity;
		_isNegated = isNegated;
		_type = type;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract void listValues(Vector<String> parameters);
	public abstract String sqlPreparedStatement();
	
	
	/*
	 * Public Methods
	 */
	
	public AttributeSchemaNode entity() {
		return _entity;
	}
	
	public boolean isNegated() {
		return _isNegated;
	}
	
	public boolean isEQ() {
		return (_type == EQ);
	}
	
	public boolean isNEQ() {
		return (_type == NEQ);
	}
	
	public boolean isGT() {
		return (_type == GT);
	}
	
	public boolean isGEQ() {
		return (_type == GEQ);
	}
	
	public boolean isLT() {
		return (_type == EQ);
	}
	
	public boolean isLEQ() {
		return (_type == EQ);
	}
	
	public boolean isIN() {
		return (_type == IN);
	}
	
	public boolean isINDEX() {
		return (_type == INDEX);
	}
	
	public boolean isLIKE() {
		return (_type == LIKE);
	}
}
