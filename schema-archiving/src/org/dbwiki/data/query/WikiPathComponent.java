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

import org.dbwiki.data.schema.Entity;

/** Struct holding components of a wiki path, including an entity and optional condition
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
	private Entity _entity;
	
	
	/*
	 * Constructors 
	 */
	
	public WikiPathComponent(Entity entity, WikiPathCondition condition) {
		_entity = entity;
		_condition = condition;
	}
	
	public WikiPathComponent(Entity entity) {
		this(entity, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public WikiPathCondition condition() {
		return _condition;
	}
	
	public Entity entity() {
		return _entity;
	}
	
	public boolean hasCondition() {
		return (_condition != null);
	}
}
