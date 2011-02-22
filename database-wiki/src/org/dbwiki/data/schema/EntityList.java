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

public class EntityList {
	/*
	 * Private Variables
	 */
	
	private Vector<Entity> _entityList;
	private Hashtable<String, Entity> _entityIndex;
	private boolean _hasAttributes;
	private boolean _hasElements;
	
	
	/*
	 * Constructors
	 */
	
	public EntityList() {
		_entityList = new Vector<Entity>();
		_entityIndex = new Hashtable<String, Entity>();
		_hasAttributes = false;
		_hasElements = false;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(Entity entity) throws org.dbwiki.exception.WikiException {
		String key = entity.label();
		if (!_entityIndex.containsKey(key)) {
			_entityList.add(entity);
			_entityIndex.put(key, entity);
			if (entity.isAttribute()) {
				_hasAttributes = true;
			} else {
				_hasElements = true;
			}
		} else {
			throw new WikiSchemaException(WikiSchemaException.DuplicateEntity, key);
		}
	}
	
	public Entity get(int index) {
		return _entityList.get(index);
	}
	
	public Entity get(String name) {
		if (_entityIndex.containsKey(name)) {
			return _entityIndex.get(name);
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
		return _entityList.size();
	}
}
