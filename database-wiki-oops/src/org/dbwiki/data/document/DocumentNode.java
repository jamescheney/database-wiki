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
package org.dbwiki.data.document;

import org.dbwiki.data.schema.Entity;

public abstract class DocumentNode implements Comparable<DocumentNode> {
	/*
	 * Private Variables
	 */
	
	private Entity _entity;
	
	
	/*
	 * Constructors
	 */
	
	public DocumentNode(Entity entity) {
		_entity = entity;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isAttribute();
	
	
	/*
	 * Public Methods
	 */
	
	public int compareTo(DocumentNode element) {
		if (this.entity().id() < element.entity().id()) {
			return -1;
		} else if (this.entity().id() > element.entity().id()) {
			return 1;
		} else {
			return 0;
		}
	}

	public Entity entity() {
		return _entity;
	}
	
	public boolean isGroup() {
		return !this.isAttribute();
	}
	
	public String label() {
		return _entity.label();
	}
}
