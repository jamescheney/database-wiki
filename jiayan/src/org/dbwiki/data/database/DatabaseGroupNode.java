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

import java.util.Stack;
import java.util.Vector;

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** Subclass of DatabaseElementNode representing group nodes. 
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseGroupNode extends DatabaseElementNode {
	/*
	 * Private Variables
	 */
	
	private DatabaseElementList _children;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseGroupNode(GroupSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation) {
		super(schema, parent, timestamp, annotation);
		
		_children = new DatabaseElementList();
	}

	
	/*
	 * Public Methods
	 */
	
	public DatabaseElementList children() {
		return _children;
	}
	
	public DatabaseElementList find(SchemaNode schema) {
		Stack<SchemaNode> entities = new Stack<SchemaNode>();
		
		SchemaNode parent = schema.parent();
		while (parent != null) {
			if (parent.equals(this.schema())) {
				break;
			}
			entities.push(parent);
			parent = parent.parent();
		}
		
		if (parent == null) {
			return new DatabaseElementList();
		}
		
		Vector<DatabaseGroupNode> elements = new Vector<DatabaseGroupNode>();
		elements.add(this);
		
		while (!entities.isEmpty()) {
			SchemaNode pathSchema = entities.pop();
			Vector<DatabaseGroupNode> candidates = new Vector<DatabaseGroupNode>();
			for (int iElement = 0; iElement < elements.size(); iElement++) {
				DatabaseGroupNode node = elements.get(iElement);
				for (int iChild = 0; iChild < node.children().size(); iChild++) {
					if (node.children().get(iChild).schema().equals(pathSchema)) {
						candidates.add((DatabaseGroupNode)node.children().get(iChild));
					}
				}
			}
			elements = candidates;
		}
		
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < elements.size(); iElement++) {
			DatabaseGroupNode node = elements.get(iElement);
			for (int iChild = 0; iChild < node.children().size(); iChild++) {
				if (node.children().get(iChild).schema().equals(schema)) {
					matches.add(node.children().get(iChild));
				}
			}
		}
		return matches;
	}
	
	public DatabaseElementList find(String path) throws org.dbwiki.exception.WikiException {
		int pos = path.indexOf('/');
		if (pos != -1) {
			DatabaseElementList matches = new DatabaseElementList();
			DatabaseElementList children = this.find(path.substring(0, pos));
			for (int iChild = 0; iChild < children.size(); iChild++) {
				DatabaseElementList nodes = ((DatabaseGroupNode)children.get(iChild)).find(path.substring(pos + 1));
				for (int iNode = 0; iNode < nodes.size(); iNode++) {
					matches.add(nodes.get(iNode));
				}
			}
			return matches;
		} else {
			return this.children().get(path);
		}
	}
	
	public String toString() {
		int n = _children.size();
		if (n == 0) {
			return label();
		} else if (n == 1) {
			return label() + "/" + _children.get(0).toString();
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append(label() + "/{");
			for(int i = 0; i < n-1; i++) {
				buf.append(_children.get(i).toString());
				buf.append(",");
			}
			buf.append(_children.get(n-1).toString());
			buf.append("}");
			return buf.toString();
		}
	}

	public DatabaseNode find(ResourceIdentifier identifier) {

		for (int iChild = 0; iChild < children().size(); iChild++) {
			DatabaseElementNode child = children().get(iChild);
			if (child.identifier().equals(identifier)) {
				return child;
			} else {
				DatabaseNode node = child.find(identifier);
				
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}
	
	public DatabaseElementList getChildren(){
	
		return _children;
	}
}
	
