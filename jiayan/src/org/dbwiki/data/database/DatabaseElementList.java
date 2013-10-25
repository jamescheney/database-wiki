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

import org.dbwiki.data.schema.SchemaNode;

/** A vector of database element nodes used for the list of children of a group node.
 * 
 * Invariant: this data structure (very inefficiently) tries to enforce the invariant
 * that the elements are ordered by schema id. Why?
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
		
		// FIXME: use an ordered set data structure to do this!
		// Do we actually need this data structure to be ordered?
		// If so, then this class shouldn't be called DatabaseElementList
		// but rather something like DatabaseElementSet. In fact, why not just use
		// a standard Java collection class instead?
		for (int iElement = 0; iElement < _elements.size(); iElement++) {
			if (_elements.get(iElement).schema().id() > node.schema().id()) {
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
	
	public DatabaseElementList get(SchemaNode schema) {
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < this.size(); iElement++) {
			DatabaseElementNode element = this.get(iElement);
			if (element.schema().equals(schema)) {
				matches.add(element);
			}
		}
		return matches;
	}
	
	public DatabaseElementList get(String label) {
		DatabaseElementList matches = new DatabaseElementList();
		for (int iElement = 0; iElement < this.size(); iElement++) {
			DatabaseElementNode element = this.get(iElement);
			if (element.schema().label().equals(label)) {
				matches.add(element);
			}
		}
		return matches;
	}

	public void remove(int index) {
		_elements.remove(index);
	}
	
	public int size() {
		return _elements.size();
	}
	
	public Vector<DatabaseElementNode> getElement(){
			return _elements;
		
	}
	
}
