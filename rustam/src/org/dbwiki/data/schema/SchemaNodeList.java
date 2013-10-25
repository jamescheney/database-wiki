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
package org.dbwiki.data.schema;

import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.exception.data.WikiSchemaException;

/** A list of entities, with a map from schema node names to their values.
 * Used in GroupSchemaNode for children.
 * @author jcheney
 *
 */

public class SchemaNodeList {
	/*
	 * Private Variables
	 */
	
	private Vector<SchemaNode> _schemaNodeList;
	private Hashtable<String, SchemaNode> _schemaNodeIndex;
	private boolean _hasAttributes;
	private boolean _hasElements;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaNodeList() {
		_schemaNodeList = new Vector<SchemaNode>();
		_schemaNodeIndex = new Hashtable<String, SchemaNode>();
		_hasAttributes = false;
		_hasElements = false;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(SchemaNode schema) throws org.dbwiki.exception.WikiException {
		String key = schema.label();
		if (!_schemaNodeIndex.containsKey(key)) {
			_schemaNodeList.add(schema);
			_schemaNodeIndex.put(key, schema);
			if (schema.isAttribute()) {
				_hasAttributes = true;
			} else {
				_hasElements = true;
			}
		} else {
			throw new WikiSchemaException(WikiSchemaException.DuplicateSchemaNode, key);
		}
	}
	
	public SchemaNode get(int index) {
		return _schemaNodeList.get(index);
	}
	
	public SchemaNode get(String name) {
		if (_schemaNodeIndex.containsKey(name)) {
			return _schemaNodeIndex.get(name);
		} else {
			return null;
		}
	}
	
	public boolean hasAttributes() {
		return _hasAttributes;
	}
	
	public boolean hasElements() {
		return _hasElements;
	}
	
	public int size() {
		return _schemaNodeList.size();
	}
}
