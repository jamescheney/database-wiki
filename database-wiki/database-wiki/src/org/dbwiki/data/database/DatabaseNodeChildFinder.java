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

import org.dbwiki.data.resource.ResourceIdentifier;

/*
 * jcheney: [TODO] Maybe this should be folded into the DatabaseNode classes...
 */
public class DatabaseNodeChildFinder {
	/*
	 * Public Methods
	 */
	
	public DatabaseNode find(DatabaseNode parent, ResourceIdentifier identifier) {
		if (parent.isElement()) {
			DatabaseElementNode element = (DatabaseElementNode)parent;
			if (element.isAttribute()) {
				return this.find((DatabaseAttributeNode)element, identifier);
			} else {
				DatabaseGroupNode group = (DatabaseGroupNode)element;
				for (int iChild = 0; iChild < group.children().size(); iChild++) {
					DatabaseElementNode child = group.children().get(iChild);
					if (child.identifier().equals(identifier)) {
						return child;
					} else {
						DatabaseNode node = null;
						if (child.isAttribute()) {
							node = this.find((DatabaseAttributeNode)child, identifier);
						} else {
							node = this.find(child, identifier);
						}
						if (node != null) {
							return node;
						}
					}
				}
			}
		}
		return null;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private DatabaseNode find(DatabaseAttributeNode attribute, ResourceIdentifier identifier) {
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			DatabaseTextNode text = attribute.value().get(iValue);
			if (text.identifier().equals(identifier)) {
				return text;
			}
		}
		return null;
	}
}
