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
	
	private Hashtable<String, Entity> _pathMap;
	private Hashtable<Integer, Entity> _idMap;
	private GroupEntity _root;
	
	/*
	 * Constructors
	 */
	public DatabaseSchema() {
		_pathMap = new Hashtable<String, Entity>();
		_idMap = new Hashtable<Integer, Entity>();
		_root = null;
	}
	
	/*
	 * Public Methods
	 */
	
	/** Adds the root node.  Should be called only once when the schema is created 
	 * 
	 */
	public void addRoot(GroupEntity entity, String label) throws org.dbwiki.exception.WikiException {
		assert(_idMap.size() == 0);
		// root entity
		_root = entity;
		add(entity);
			
	}
	
	/** Adds the root node, using label as root label
	 * 
	 * @param entity
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void addRoot(GroupEntity entity) throws org.dbwiki.exception.WikiException {
		addRoot(entity,entity.label());
	}
	
	/** Adds a non-root entity.  Should only be called after addRoot. 
	 * 
	 * @param entity
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void add(Entity entity) throws org.dbwiki.exception.WikiException {
// The constraint that consecutive entities must
// have consecutive ids seems overly restrictive.
//
// We now use a hash table for the id map rather than
// a list, which should give us more flexibility.
//
//		if (entity.id() != this.size()) {
//			throw new WikiFatalException("Entity with invalid id(" + entity.id() + ") added to database schema. Expected id is " + this.size());
//		}

		assert(_idMap.size() > 0);
		if (!isValidName(entity.label())) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid entry name " + _root.label());
		}
		String key = entity.path();
		if (_pathMap.containsKey(key)) {
			throw new WikiFatalException("Entity with duplicate path(" + key + ") added to database schema.");
		}
		
		_pathMap.put(key, entity);		
		_idMap.put(entity.id(), entity);
	}
	

	public Entity get(int index) {
		return _idMap.get(index);
	}
	
	public Entity get(String path) throws org.dbwiki.exception.WikiException {
		String key = path;
		if ((path.length() > 1) && (path.endsWith(Entity.EntityPathSeparator))) {
			key = key.substring(0, key.length() - Entity.EntityPathSeparator.length());
		}
		Entity entity = _pathMap.get(key);
		if (entity == null) {
			throw new WikiSchemaException(WikiSchemaException.UnknownEntity, path);
		} else {
			return entity;
		}
	}
	
	public GroupEntity root() {
		return _root;
	}
	
	public int size() {
		return _idMap.size();
	}
	
	
	/*
	 * Static methods for names
	 * 
	 */
	public static String getEntityName(String text) {
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
