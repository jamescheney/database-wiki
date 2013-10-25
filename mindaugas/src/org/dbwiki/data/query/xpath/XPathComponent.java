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
	private SchemaNode _entity;
	
	
	/*
	 * Constructors
	 */
	
	public XPathComponent(SchemaNode entity, XPathCondition condition) {
		
		_entity = entity;
		_condition = condition;
	}
	
	public XPathComponent(SchemaNode entity) {
		
		this(entity, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public XPathCondition condition() {
		
		return _condition;
	}
	
	public SchemaNode entity() {
		
		return _entity;
	}
	
	public boolean hasCondition() {
		
		return (_condition != null);
	}
	
	public boolean matches(DatabaseElementNode node) {
		
		if (node.schema().equals(_entity)) {
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
			return _entity.label() + _condition.toString();
		} else {
			return _entity.label();
		}
	}
}
