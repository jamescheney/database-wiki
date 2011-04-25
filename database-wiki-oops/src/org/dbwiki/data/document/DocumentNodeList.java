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
