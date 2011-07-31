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
import org.dbwiki.data.resource.SchemaNodeIdentifier;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.TimestampedObject;

/** A timestamped schema node. 
 * A schema node has an id, label, optional parent, and timestamp.
 * 
 * @author jcheney
 *
 */

public abstract class SchemaNode extends TimestampedObject {
	/*
	 * Public Constants
	 */
	
	public static final String SchemaPathSeparator = "/";

	public static final int RootID = -1;
	/*
	 * Private Variables
	 */
	
	private int _id;
	private String _label;
	private GroupSchemaNode _parent;
	// FIXME #schemaversioning: Delete this?  This is never read locally - and it shadows
	// the parent class's _timestamp field too!
	// But maybe this is intended.
	@SuppressWarnings("unused")
	private TimeSequence _timestamp;
	
	/*
	 * Constructors
	 */
	
	public SchemaNode(int id, String label, GroupSchemaNode parent, TimeSequence timestamp) throws org.dbwiki.exception.WikiException {
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
	public SchemaNodeIdentifier identifier() {
		return new SchemaNodeIdentifier(_id);
	}
	
	public boolean equals(SchemaNode schema) {
		return this.id() == schema.id();
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
	
	public GroupSchemaNode parent() {
		return _parent;
	}
	
	public String path() {
		if (_parent != null) {
			return _parent.path() + SchemaPathSeparator + _label;
		} else {
			return SchemaPathSeparator + _label;
		}
	}
	
	public String toString() {
		return label();
	}
	
	public abstract void printToBuf(StringBuffer buf, String indentation, String extend, String cr);
	
	/** 
	 * 
	 * @param schema
	 * @param groupIndex
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public static DocumentGroupNode createGroupNode(GroupSchemaNode schema, Hashtable<Integer, DocumentGroupNode> groupIndex) throws org.dbwiki.exception.WikiException {
		DocumentGroupNode root = new DocumentGroupNode(schema);
		
		groupIndex.put(new Integer(schema.id()), root);
		
		for (int iChild = 0; iChild < schema.children().size(); iChild++) {
			// TODO: we probably need to be careful about which bits of the
			// schema are current
			SchemaNode child = schema.children().get(iChild);
			if (child.isGroup()) {
				root.children().add(createGroupNode((GroupSchemaNode)child, groupIndex));
			}
		}
		return root;
	}


}

