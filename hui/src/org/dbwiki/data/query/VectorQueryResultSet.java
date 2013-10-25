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
package org.dbwiki.data.query;

import java.util.Vector;

import org.dbwiki.data.database.DatabaseNode;

/** Implementation of QueryResultSet, which wraps a Vector of DatabaseNode
 * 
 * @author jcheney
 *
 */
public class VectorQueryResultSet implements QueryResultSet {
	/*
	 * Private Variables
	 */
	
	private Vector<DatabaseNode> _nodes;
	
	
	/*
	 * Constructors
	 */
	
	public VectorQueryResultSet() {
		_nodes = new Vector<DatabaseNode>();
	}
	
	public VectorQueryResultSet(DatabaseNode node) {
		this();
		
		this.add(node);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(DatabaseNode node) {
		_nodes.add(node);
	}
	
	public DatabaseNode get(int index) {
		return _nodes.get(index);
	}

	public boolean isElement() {
		if (_nodes.size() > 0) {
			return _nodes.get(0).isElement();
		} else {
			return false;
		}
	}
	public boolean isEmpty() {
		return _nodes.isEmpty();
	}

	public int size() {
		return _nodes.size();
	}
	
	public String toString() {
		int n = _nodes.size();
		
		if (n == 0) {
			return "{}";
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append("{");
			for(int i = 0; i < n-1; i++) {
				buf.append(_nodes.get(i).toString());
				buf.append(",");
			}
			buf.append(_nodes.get(n-1).toString());
			buf.append("}");
			return buf.toString();
		}
	}
}
