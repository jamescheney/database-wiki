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

/** Iterates over all valid bindings for the variables in a XAQL query
 * given a database node bound to the root of the query variable tree.
*
* @author hmueller
*
*/
import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.query.xaql.QueryVariable;
import org.dbwiki.data.query.xpath.RelativeXPathConsumer;

public class NodeSetIterator implements QueryNodeHandler {

	/*
	 * Private Variables
	 */
	
	private int _index;
	private Vector<IteratorNode> _nodes;
	private QueryVariable _variable;
	
	
	/*
	 * Constructors
	 */
	
	public NodeSetIterator(QueryVariable variable) {
		
		_variable = variable;
		
		_nodes = new Vector<IteratorNode>();
		_index = 0;
	}
	
	
	/*
	 * Public Methods
	 */

	public void addCurrentNode(QueryNodeSet nodeSet) {
		
		if (_nodes.size() > 0) {
			IteratorNode iteratorNode = _nodes.get(_index);
			nodeSet.add(_variable.name(), iteratorNode.node());
			iteratorNode.addCurrentChildren(nodeSet);
		}
	}
	
	public boolean advance() {
		
		if (_nodes.size() > 0) {
			boolean hasNext = _nodes.get(_index).advance();
			if (!hasNext) {
				_index++;
				if (_index < _nodes.size()) {
					return true;
				} else {
					_index = 0;
					return false;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public void handle(DatabaseElementNode node) {
		
		if (_variable.children().size() > 0) {
			Vector<NodeSetIterator> iterators = new Vector<NodeSetIterator>();
			for (int iVariable = 0; iVariable < _variable.children().size(); iVariable++) {
				QueryVariable variable = _variable.children().get(iVariable);
				NodeSetIterator iterator = new NodeSetIterator(variable);
				new RelativeXPathConsumer().consume(node, variable.targetPath(), iterator);
				iterators.add(iterator);
			}
			if (iterators.size() == 1) {
				_nodes.add(new SingleChildIteratorNode(node, iterators.firstElement()));
			} else {
				_nodes.add(new MultiChildrenIteratorNode(node, iterators));
			}
		} else {
			_nodes.add(new LeafIteratorNode(node));
		}
	}
}
