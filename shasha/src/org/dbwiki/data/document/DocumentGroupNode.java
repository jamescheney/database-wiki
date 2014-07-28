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

import java.util.Collections;
import java.util.Vector;

import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

public class DocumentGroupNode extends DocumentNode {
	/*
	 * Private Variables
	 */
	
	private DocumentNodeList _children;
	
	

	
	/*
	 * Constructors
	 */
	
	public DocumentGroupNode(GroupSchemaNode schema) {
		super(schema);
		
		_children = new DocumentNodeList();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public DocumentNodeList children() {
		return _children;
	}
	
	public Vector<DocumentNode> find(SchemaNode schema) throws org.dbwiki.exception.WikiException {
		Vector<DocumentNode> elementList = new Vector<DocumentNode>();
		
		if (this.schema().equals(schema)) {
			elementList.add(this);
		} else {
			this.find(this, schema, elementList);
		}
		
		return elementList;
	}

	@Override
	public boolean isAttribute() {
		return false;
	}
	
	public boolean isElement() {
		return true;
	}
	
	public void doNumberingRoot() {
        doNumbering(1);

    }

    @Override
	public int doNumbering(int startingFrom) {
        _pre = startingFrom;
        int next = _pre+1;
        for(int i = 0; i < children().size(); i++) {
                DocumentNode child = children().get(i);
                next = child.doNumbering(next);
        }
        _post = next;
        return next+1;
    }


	
	/* Static methods
	 * 
	 */
	/** Traverses document tree, removing empty nodes.
	 * @param root
	 */
	public static void removeEmptyNodes(DocumentGroupNode root) {
		int iNode = 0;
		while (iNode < root.children().size()) {
			DocumentNode child = root.children().get(iNode);
			if (child.isGroup()) {
				DocumentGroupNode groupChild = (DocumentGroupNode)child;
				if (groupChild.children().size() > 0) {
					removeEmptyNodes(groupChild);
				}
				if (groupChild.children().size() == 0) {
					root.children().remove(iNode);
				} else {
					iNode++;
				}
			} else {
				iNode++;
			}
		}
	}
	
	/*
	 * Private Variables
	 */
	
	private void find(DocumentGroupNode group, SchemaNode schema, Vector<DocumentNode> elementList) throws org.dbwiki.exception.WikiException {
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			DocumentNode element = group.children().get(iChild);
			if (element.schema().equals(schema)) {
				elementList.add(element);
			} else if (element.isGroup()) {
				this.find((DocumentGroupNode)element, schema, elementList);
			}
		}
	}
	
	public class DocumentNodeList {
		/*
		 * Private Variables
		 */
		
		private Vector<DocumentNode> _elements;
		
		
		/*
		 * Constructors
		 */
		
		public DocumentNodeList() {
			_elements = new Vector<DocumentNode>();
		}
		
		
		/*
		 * Public Methods
		 */
		
		public void add(DocumentNode node) {
			_elements.add(node);
		}
		
		public DocumentNode get(int index) {
			return _elements.get(index);
		}
		
		public void remove(int index) {
			_elements.remove(index);
		}
		
		public int size() {
			return _elements.size();
		}
		
		public void sort() {
			Collections.sort(_elements);
		}
	}

}
