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
package org.dbwiki.data.database;

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.schema.Entity;

import org.dbwiki.data.time.TimeSequence;

/** A subclass of DatabaseNode providing common functionality for "group" and "attribute" nodes.
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseElementNode extends DatabaseNode {
	/*
	 * Private Variables
	 */
	
	private Entity _entity;
	private String _label;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseElementNode(Entity entity, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation) {
		super(parent, timestamp, annotation);
		
		_entity = entity;
		
		_label = entity.label();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public Entity entity() {
		return _entity;
	}
	
	public boolean isAttribute() {
		return _entity.isAttribute();
	}

	public boolean isElement() {
		return true;
	}

	public boolean isGroup() {
		return _entity.isGroup();
	}

	public String label() {
		return _label;
	}
	
}
