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


import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiSchemaException;

public class DatabaseSchema {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, Entity> _entityIndex;
	private Vector<Entity> _entityList;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseSchema() {
		_entityIndex = new Hashtable<String, Entity>();
		_entityList = new Vector<Entity>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(Entity entity) throws org.dbwiki.exception.WikiException {
		if (entity.id() != this.size()) {
			throw new WikiFatalException("Entity with invalid id(" + entity.id() + ") added to database schema. Expected id is " + this.size());
		}
		
		String key = entity.path();
		if (_entityIndex.containsKey(key)) {
			throw new WikiFatalException("Entity with duplicate path(" + key + ") added to database schema.");
		}
		
		_entityIndex.put(key, entity);
		
		_entityList.add(entity);
	}
	
	public Entity get(int index) {
		return _entityList.get(index);
	}
	
	public Entity get(String path) throws org.dbwiki.exception.WikiException {
		String key = path;
		if ((path.length() > 1) && (path.endsWith(Entity.EntityPathSeparator))) {
			key = key.substring(0, key.length() - Entity.EntityPathSeparator.length());
		}
		Entity entity = _entityIndex.get(key);
		if (entity == null) {
			throw new WikiSchemaException(WikiSchemaException.UnknownEntity, path);
		} else {
			return entity;
		}
	}
	
	public GroupEntity root() {
		if (_entityList.size() > 0) {
			return (GroupEntity)_entityList.get(0);
		} else {
			return null;
		}
	}
	
	public int size() {
		return _entityList.size();
	}
}
