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
package org.dbwiki.data.query;

import org.dbwiki.data.schema.SchemaNode;

/** Struct holding components of a wiki path, including a schema and optional condition
 * 
 * 
 * @author jcheney
 *
 */
public class WikiPathComponent {
	/*
	 * Private Variables
	 */
	
	private WikiPathCondition _condition;
	private SchemaNode _schema;
	
	
	/*
	 * Constructors 
	 */
	
	public WikiPathComponent(SchemaNode schema, WikiPathCondition condition) {
		_schema = schema;
		_condition = condition;
	}
	
	public WikiPathComponent(SchemaNode schema) {
		this(schema, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public WikiPathCondition condition() {
		return _condition;
	}
	
	public SchemaNode schema() {
		return _schema;
	}
	
	public boolean hasCondition() {
		return (_condition != null);
	}
}
