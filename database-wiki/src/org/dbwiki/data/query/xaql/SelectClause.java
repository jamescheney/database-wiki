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
package org.dbwiki.data.query.xaql;

/** SELECT clause in a XAQL query.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;

import org.dbwiki.data.query.handler.QueryNodeHandler;
import org.dbwiki.data.query.handler.QueryNodeSet;
import org.dbwiki.data.query.handler.QueryOutputNodeCollector;
import org.dbwiki.data.query.handler.ResultGroupNode;

import org.dbwiki.data.query.xpath.RelativeXPathConsumer;

import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeSequence;

public class SelectClause {

	/*
	 * Private Variables
	 */
	
	private GroupSchemaNode _resultEntity;
	private Vector<SubTreeSelectStatement> _statements;
	
	
	/*
	 * Constructors
	 */
	
	public SelectClause() throws org.dbwiki.exception.WikiException {
		
		_statements = new Vector<SubTreeSelectStatement>();
		
		_resultEntity = new GroupSchemaNode(-1, "result", null, new TimeSequence(1));
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(SubTreeSelectStatement statement) {
		
		_statements.add(statement);
	}
	
	public void consume(QueryNodeSet nodeSet, QueryNodeHandler consumer) {

		Vector<DatabaseElementNode> outputNodes = new Vector<DatabaseElementNode>();
		for (SubTreeSelectStatement stmt : _statements) {
			DatabaseElementNode node = nodeSet.get(stmt.targetPath().variableName());
			QueryOutputNodeCollector nodeMarker = new QueryOutputNodeCollector(outputNodes);
			new RelativeXPathConsumer().consume(node, stmt.targetPath(), nodeMarker);
		}
		if (outputNodes.size() > 0) {
			TimeSequence timestamp = null;
			for (int iNode = 0; iNode < outputNodes.size(); iNode++) {
				DatabaseElementNode node = outputNodes.get(iNode);
				if (timestamp != null) {
					timestamp = timestamp.union(node.getTimestamp());
				} else {
					timestamp = node.getTimestamp();
				}
				if (_resultEntity.children().get(node.schema().label()) == null) {
					try {
						_resultEntity.children().add(node.schema());
					} catch (org.dbwiki.exception.WikiException wikiException) {
						wikiException.printStackTrace();
					}
				}
			}
			DatabaseGroupNode result = new ResultGroupNode(_resultEntity, timestamp);
			for (DatabaseElementNode node : outputNodes) {
				result.children().add(node);
			}
			consumer.handle(result);
		}
	}
}
