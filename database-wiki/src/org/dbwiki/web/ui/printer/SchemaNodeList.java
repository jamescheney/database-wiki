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
package org.dbwiki.web.ui.printer;

import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;

import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.exception.WikiFatalException;

/** A struct containing a list of schema nodes.
 * Used in printing data nodes and query results.
 * @author jcheney
 *
 */
public class SchemaNodeList {
	/*
	 * Private Variables
	 */
	
	private Vector<DatabaseElementNode> _elements;
	private SchemaNode _schemaNode;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaNodeList(DatabaseElementNode node) {
		_elements = new Vector<DatabaseElementNode>();
		_elements.add(node);
		
		_schemaNode = node.schema();
	}
	
	public SchemaNodeList(QueryResultSet rs) throws org.dbwiki.exception.WikiException {
		_elements = new Vector<DatabaseElementNode>();
		_elements.add((DatabaseElementNode)rs.get(0));
		
		_schemaNode = rs.schema();
		
		for (int iNode = 1; iNode < rs.size(); iNode++) {
			this.add((DatabaseElementNode)rs.get(iNode));
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(DatabaseElementNode node) throws org.dbwiki.exception.WikiException {
		if (!node.schema().equals(_schemaNode)) {
			throw new WikiFatalException("Incomparable entities in SchemaNodeList");
		}
		_elements.add(node);
	}
	
	public SchemaNode schema() {
		return _schemaNode;
	}
	
	public DatabaseElementNode get(int index) {
		return _elements.get(index);
	}
	
	public boolean isActive() {
		for (int iNode = 0; iNode < _elements.size(); iNode++) {
			if (_elements.get(iNode).getTimestamp().isCurrent()) {
				return true;
			}
		}
		return false;
	}
	
	public int size() {
		return _elements.size();
	}
}
