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

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiSchemaException;

/** 
 * A schema is a collection of entities, each of which is a group or attribute.
 * Attributes are string-valued key-value pairs
 * Groups are named collections of entities, allowing nesting
 * @author jcheney
 *
 */
public class DatabaseSchema {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, SchemaNode> _pathMap;
	private Hashtable<Integer, SchemaNode> _idMap;
	private GroupSchemaNode _root;
	
	/*
	 * Constructors
	 */
	public DatabaseSchema() {
		_pathMap = new Hashtable<String, SchemaNode>();
		_idMap = new Hashtable<Integer, SchemaNode>();
		_root = null;
	}
	
	/*
	 * Public Methods
	 */
	
	
	
	/** Adds a schema node.  First schema node to be added is expected to be root.
	 * 
	 * @param schema
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void add(SchemaNode schema) throws org.dbwiki.exception.WikiException {
// The constraint that consecutive entities must
// have consecutive ids seems overly restrictive.
//
// We now use a hash table for the id map rather than
// a list, which should give us more flexibility.
//
//		if (schema.id() != this.size()) {
//			throw new WikiFatalException("Schema node with invalid id(" + schema.id() + ") added to database schema. Expected id is " + this.size());
//		}

		if(_idMap.size() == 0) {
			_root = (GroupSchemaNode)schema;
		}
		
		assert(_idMap.size() > 0);
		if (!isValidName(schema.label())) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid entry name " + _root.label());
		}
		String key = schema.path();
		if (_pathMap.containsKey(key)) {
			throw new WikiFatalException("Schema node with duplicate path(" + key + ") added to database schema.");
		}
		
		_pathMap.put(key, schema);		
		_idMap.put(schema.id(), schema);
	}
	

	public SchemaNode get(int index) {
		return _idMap.get(index);
	}
	
	public SchemaNode get(String path) throws org.dbwiki.exception.WikiException {
		String key = path;
		if ((path.length() > 1) && (path.endsWith(SchemaNode.SchemaPathSeparator))) {
			key = key.substring(0, key.length() - SchemaNode.SchemaPathSeparator.length());
		}
		SchemaNode schema = _pathMap.get(key);
		if (schema == null) {
			throw new WikiSchemaException(WikiSchemaException.UnknownSchemaNode, path);
		} else {
			return schema;
		}
	}
	
	public GroupSchemaNode root() {
		return _root;
	}
	
	public int size() {
		return _idMap.size();
	}
	
	
	/*
	 * Static methods for names
	 * 
	 */
	public static String getSchemaNodeName(String text) {
		int pos = text.lastIndexOf('/');
		if (pos == -1) {
			return text;
		} else {
			return text.substring(pos + 1);
		}
	}
	
	public static boolean isValidName(String text) {
		if (text.length() > 0) {
			for (int iChar = 0; iChar < text.length(); iChar++) {
				char c = text.charAt(iChar);
				if ((!Character.isDigit(c)) && (!Character.isLetter(c)) && (c != '_') && (c != '-')) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	
	public String printSchema() {
		StringBuffer buf = new StringBuffer();

		_root.printToBuf(buf,"");
		
		return buf.toString().trim();
	}

	
}
