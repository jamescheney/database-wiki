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

import org.dbwiki.exception.WikiFatalException;

public class PasteNodeList {
	/*
	 * Private Variables
	 */
	
	private Vector<PasteNode> _nodes;
	
	
	/*
	 * Constructors
	 */
	
	public PasteNodeList() {
		_nodes = new Vector<PasteNode>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(PasteNode node) throws org.dbwiki.exception.WikiException {
		if (_nodes.size() > 0) {
			if ((_nodes.get(0).isElement()) && (node.isElement())) {
				_nodes.add(node);
			} else {
				System.out.println(_nodes.get(0) + "\t" + node);
				throw new WikiFatalException("Invalid node sequence in document. Trying to add text node to group node");
			}
		} else {
			_nodes.add(node);
		}
	}
	
	public PasteNode get(int index) {
		return _nodes.get(index);
	}
	
	public int size() {
		return _nodes.size();
	}
}
