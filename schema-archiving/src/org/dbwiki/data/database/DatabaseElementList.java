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

import java.util.Vector;

import org.dbwiki.data.schema.Entity;

/** A vector of database element nodes used for the list of children of a group node.
 * 
 * @author jcheney
 *
 */
public class DatabaseElementList {
	/*
	 * Private Variables
	 */
	
	private Vector<DatabaseElementNode> _elements;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseElementList() {
		_elements = new Vector<DatabaseElementNode>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(DatabaseElementNode node) {
		boolean added = false;
		for (int iElement = 0; iElement < _elements.size(); iElement++) {
			if (_elements.get(iElement).entity().id() > node.entity().id()) {
				_elements.add(iElement, node);
				added = true;
				break;
			}
		}
		if (!added) {
			_elements.add(node);
		}
	}
	
	public DatabaseElementNode get(int index) {
		return _elements.get(index);
	}
	
	public DatabaseElementList get(Entity entity) {
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < this.size(); iElement++) {
			DatabaseElementNode element = this.get(iElement);
			if (element.entity().equals(entity)) {
				matches.add(element);
			}
		}
		return matches;
	}
	
	public DatabaseElementList get(String label) {
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < this.size(); iElement++) {
			DatabaseElementNode element = this.get(iElement);
			if (element.entity().label().equals(label)) {
				matches.add(element);
			}
		}
		return matches;
	}

	public int size() {
		return _elements.size();
	}
}
