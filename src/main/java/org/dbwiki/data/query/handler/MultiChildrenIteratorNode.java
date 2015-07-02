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
package org.dbwiki.data.query.handler;

/** The basic interface for consuming nodes during query evaluation.
 *
 * @author hmueller
 *
 */
import java.util.Vector;

/** IteratorNode with multiple variables as children.
*
* @author hmueller
*
*/
import org.dbwiki.data.database.DatabaseElementNode;

public class MultiChildrenIteratorNode extends IteratorNode {

	/*
	 * Private Variables
	 */
	
	private Vector<NodeSetIterator> _iterators;
	
	
	/*
	 * Constructors
	 */
	
	public MultiChildrenIteratorNode(DatabaseElementNode node, Vector<NodeSetIterator> iterators) {
		
		super(node);
		
		_iterators = iterators;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void addCurrentChildren(QueryNodeSet nodeSet) {

		for (NodeSetIterator iterator : _iterators) {
			iterator.addCurrentNode(nodeSet);
		}
	}

	public boolean advance() {

		for (NodeSetIterator iterator: _iterators) {
			if (iterator.advance()) {
				return true;
			}
		}
		return false;
	}
}
