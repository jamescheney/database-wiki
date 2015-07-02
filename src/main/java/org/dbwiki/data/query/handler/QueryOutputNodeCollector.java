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

/** Node handler that collects the nodes which are output by the select clasue
 * of a query.
 *
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNodeValue;
import org.dbwiki.data.database.ResultAttributeNode;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.SchemaNode;

public class QueryOutputNodeCollector implements QueryNodeHandler {
	/*
	 * Private Variables
	 */
	private Vector<DatabaseElementNode> _outputNodes;
	private SchemaNode _schema;
	private boolean _rename;
	
	
	/*
	 * Constructors
	 */
	public QueryOutputNodeCollector(Vector<DatabaseElementNode> outputNodes, SchemaNode schema) {
		_outputNodes = outputNodes;
		_schema = schema;
		_rename = true;
	}
	
	public QueryOutputNodeCollector(Vector<DatabaseElementNode> outputNodes) {
		_outputNodes = outputNodes;
		_schema = null;
		_rename = false;
	}

	
	
	/*
	 * Public Methods
	 */
	public void handle(DatabaseElementNode node) {
		DatabaseElementNode outputNode = null;
				
		if(!_rename)
			outputNode = node;
		else {
			if(node.isAttribute()) {
				ResultAttributeNode result = new ResultAttributeNode((AttributeSchemaNode)_schema, node.getTimestamp());
				DatabaseNodeValue value = ((DatabaseAttributeNode)node).value();
				for(int i = 0; i < value.size(); i++) {
					result.value().add(value.get(i));
				}
				outputNode = result;
			} else if(node.isGroup()) {
				ResultGroupNode result = new ResultGroupNode((GroupSchemaNode)_schema, node.getTimestamp());
				DatabaseElementList children = ((DatabaseGroupNode)node).children();
				for(int i = 0; i < children.size(); i++) {
					result.children().add(children.get(i));
				}
				outputNode = result;
			}
		}
			
		_outputNodes.add(outputNode);
	}
}
