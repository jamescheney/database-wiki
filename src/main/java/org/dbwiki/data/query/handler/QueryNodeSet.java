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

/** Collection of database nodes for each of the variables in the query.
 * Represents a valid binding for all variables within the query, which is
 * passed on to the SELECT clause to generate output.
 *
 * @author hmueller
 *
 */
import java.util.Hashtable;
import java.util.Iterator;

import org.dbwiki.data.database.DatabaseElementNode;

public class QueryNodeSet {

	/*
	 * Private Variables
	 */
	
	private Hashtable<String, DatabaseElementNode> _nodes;
	
	
	/*
	 * Constructors
	 */
	
	public QueryNodeSet() {
		
		_nodes = new Hashtable<String, DatabaseElementNode>();
	}
	
	public QueryNodeSet(String name, DatabaseElementNode node) {
		
		this();
		this.add(name, node);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(String name, DatabaseElementNode node) {
		
		_nodes.put(name,  node);
	}
	
	public DatabaseElementNode get(String name) {
		
		return _nodes.get(name);
	}
	
	public Iterator<DatabaseElementNode> iterator() {
		
		return _nodes.values().iterator();
	}
	
	public int size() {
		
		return _nodes.size();
	}
}
