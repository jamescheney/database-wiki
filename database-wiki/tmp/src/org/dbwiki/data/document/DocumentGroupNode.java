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

import java.util.Vector;

import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

public class DocumentGroupNode extends DocumentNode {
	/*
	 * Private Variables
	 */
	
	private DocumentNodeList _children;
	
	
	/*
	 * Constructors
	 */
	
	public DocumentGroupNode(GroupEntity entity) {
		super(entity);
		
		_children = new DocumentNodeList();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public DocumentNodeList children() {
		return _children;
	}
	
	public Vector<DocumentNode> find(Entity entity) throws org.dbwiki.exception.WikiException {
		Vector<DocumentNode> elementList = new Vector<DocumentNode>();
		
		if (this.entity().equals(entity)) {
			elementList.add(this);
		} else {
			this.find(this, entity, elementList);
		}
		
		return elementList;
	}

	public boolean isAttribute() {
		return false;
	}
	
	
	
	/*
	 * Private Variables
	 */
	
	private void find(DocumentGroupNode group, Entity entity, Vector<DocumentNode> elementList) throws org.dbwiki.exception.WikiException {
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			DocumentNode element = group.children().get(iChild);
			if (element.entity().equals(entity)) {
				elementList.add(element);
			} else if (element.isGroup()) {
				this.find((DocumentGroupNode)element, entity, elementList);
			}
		}
	}
}
