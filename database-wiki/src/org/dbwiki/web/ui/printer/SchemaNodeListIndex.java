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
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;
/**
 * A struct containing a layouter and a vector of schema node lists.
 * Used in printing out data nodes.
 * @author jcheney
 *
 */
public class SchemaNodeListIndex {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layout;
	private Vector<SchemaNodeList> _lists;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaNodeListIndex(DatabaseGroupNode node, DatabaseLayouter layout) throws org.dbwiki.exception.WikiException {
		_layout = layout;
		
		_lists = new Vector<SchemaNodeList>();
		for (int iChild = 0; iChild < node.children().size(); iChild++) {
			this.add(node.children().get(iChild));
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(DatabaseElementNode node) throws org.dbwiki.exception.WikiException {
		SchemaNodeList container = null;
		for (int iElement = 0; iElement < _lists.size(); iElement++) {
			if (_lists.get(iElement).schema().equals(node.schema())) {
				container = _lists.get(iElement);
				break;
			}
		}
		if (container == null) {
			container = new SchemaNodeList(node);
			boolean added = false;
			SchemaLayout nodeLayout = _layout.get(node.schema());
			for (int iElement = 0; iElement < _lists.size(); iElement++) {
				SchemaNode schema = _lists.get(iElement).schema();
				SchemaLayout schemaLayout = _layout.get(schema);
				if (schemaLayout.getDisplayOrder() > nodeLayout.getDisplayOrder()) {
					_lists.add(iElement, container);
					added = true;
					break;
				} else if ((schemaLayout.getDisplayOrder() == nodeLayout.getDisplayOrder()) && (schema.id() > node.schema().id())) {
					_lists.add(iElement, container);
					added = true;
					break;
				}
			}
			if (!added) {
				_lists.add(container);
			}
		} else {
			container.add(node);
		}
	}
	
	public SchemaNodeList get(int index) {
		return _lists.get(index);
	}
	
	public int size() {
		return _lists.size();
	}
}
