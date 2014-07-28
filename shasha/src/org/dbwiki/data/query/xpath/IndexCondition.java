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
package org.dbwiki.data.query.xpath;

/** Index condition in a XPath expression, e.g., .../NAME:i/...
*
* @author hmueller
*
*/
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;

public class IndexCondition extends XPathCondition {

	/*
	 * Private Variables
	 */
	
	private int _index;
	
	
	/*
	 * Constructors
	 */
	
	public IndexCondition(int index) {
		
		_index = index;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public boolean isIndexCondition() {

		return true;
	}

	@Override
	public boolean matches(DatabaseElementNode node) {

		int nodeIndex = 1;
		
		// Counts the nodes of the same schema as the given node in the
		// list of children of the node's parent that have a node ID
		// below the ID of the given node
		if (node.parent() != null) {
			if (node.parent().isGroup()) {
				DatabaseGroupNode parent = (DatabaseGroupNode)node.parent();
				for (int iChild = 0; iChild < parent.children().size(); iChild++) {
					DatabaseElementNode child = parent.children().get(iChild);
					if (child.schema().equals(node.schema())) {
						if (child.identifier().compareTo(node.identifier()) < 0) {
							nodeIndex++;
						}
					}
				}
			}
		} else {
			// TODO Need a way to determine the index of an entry in the database.
			nodeIndex = -1;
		}
		
		return (nodeIndex == _index);
	}
	
	@Override
	public String toString() {
		
		return ":" + _index;
	}
	
	public int index() {
		return _index;
	}
	
}