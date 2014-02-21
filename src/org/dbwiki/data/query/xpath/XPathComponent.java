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

/** Path component in a XPath expression. Has a schema node and
* an optional condition.
*
* @author hmueller
*
*/
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.schema.SchemaNode;

public class XPathComponent {

	/*
	 * Private Variables
	 */
	
	private XPathCondition _condition;
	private SchemaNode _schema;
	private String _axis;
	
	/*
	 * Constructors
	 */
	
	public XPathComponent(SchemaNode entity, XPathCondition condition, String axis) {
		
		_schema = entity;
		_condition = condition;
		_axis = axis;
	}
	
public XPathComponent(SchemaNode entity, XPathCondition condition) {
		
		_schema = entity;
		_condition = condition;
		_axis = null;
	}
	public XPathComponent(SchemaNode entity) {
		
		this(entity, null, null);
	}
	
	
	public XPathComponent(SchemaNode schema, String axis) {
		this(schema, null, axis);
	}
	/*
	 * Public Methods
	 */
	
	
	public SchemaNode schema() {
		return _schema;
	}
	public XPathCondition condition() {
		
		return _condition;
	}
	
	public SchemaNode entity() {
		
		return _schema;
	}
	
	public boolean hasCondition() {
		
		return (_condition != null);
	}
	
	public boolean matches(DatabaseElementNode node) {
		
		if (node.schema().equals(_schema)) {
			if (_condition != null) {
				return _condition.matches(node);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		
		if (_condition != null) {
			return _schema.label() + _condition.toString();
		} else {
			return _schema.label();
		}
	}

	public String axis() {
		return _axis;
	}
	
	public boolean hasAxis() {
		return (_axis != null);
	}
}
