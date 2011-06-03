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

import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.resource.EntityIdentifier;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.TimestampedObject;

/** A timestamped schema entity.  Entity is synonymous with "type" or "node of the schema".
 * An entity has an id, label, optional parent group entity, and timestamp.
 * 
 * @author jcheney
 *
 */

public abstract class Entity extends TimestampedObject {
	/*
	 * Public Constants
	 */
	
	public static final String EntityPathSeparator = "/";

	public static final int RootEntityID = -1;
	/*
	 * Private Variables
	 */
	
	private int _id;
	private String _label;
	private GroupEntity _parent;
	// FIXME #schemaversioning: Delete this?  This is never read locally - and it shadows
	// the parent class's _timestamp field too!
	// But maybe this is intended.
	@SuppressWarnings("unused")
	private TimeSequence _timestamp;
	
	/*
	 * Constructors
	 */
	
	public Entity(int id, String label, GroupEntity parent, TimeSequence timestamp) throws org.dbwiki.exception.WikiException {
		super(parent, timestamp);
		_id = id;
		_label = label;
		_parent = parent;
		_timestamp = timestamp;

		if (_parent != null) {
			_parent.children().add(this);
		}
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isAttribute();
	

	/*
	 * Public Methods
	 */
	public EntityIdentifier identifier() {
		return new EntityIdentifier(_id);
	}
	
	public boolean equals(Entity entity) {
		return this.id() == entity.id();
	}
	
	public int id() {
		return _id;
	}
	
	public boolean isGroup() {
		return !this.isAttribute();
	}


	public String label() {
		return _label;
	}
	
	public GroupEntity parent() {
		return _parent;
	}
	
	public String path() {
		if (_parent != null) {
			return _parent.path() + EntityPathSeparator + _label;
		} else {
			return EntityPathSeparator + _label;
		}
	}
	
	public String toString() {
		return label();
	}
	
	public abstract void printToBuf(StringBuffer buf, String indentation);
	
	/** 
	 * 
	 * @param entity
	 * @param groupIndex
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public static DocumentGroupNode createGroupNode(GroupEntity entity, Hashtable<Integer, DocumentGroupNode> groupIndex) throws org.dbwiki.exception.WikiException {
		DocumentGroupNode root = new DocumentGroupNode(entity);
		
		groupIndex.put(new Integer(entity.id()), root);
		
		for (int iChild = 0; iChild < entity.children().size(); iChild++) {
			// TODO: we probably need to be careful about which bits of the
			// schema are current
			Entity child = entity.children().get(iChild);
			if (child.isGroup()) {
				root.children().add(createGroupNode((GroupEntity)child, groupIndex));
			}
		}
		return root;
	}


}

