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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;

import org.dbwiki.data.query.handler.QueryNodeHandler;
import org.dbwiki.data.query.handler.QueryNodeSet;
import org.dbwiki.data.query.handler.QueryOutputNodeCollector;
import org.dbwiki.data.query.handler.ResultGroupNode;

import org.dbwiki.data.query.xpath.RelativeXPathConsumer;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.WikiException;

import org.dbwiki.lib.Counter;

public class SelectClause {

	/*
	 * Private Variables
	 */
	
	private GroupSchemaNode _resultSchema = null;
	private Vector<SubTreeSelectStatement> _statements;
	
	
	/*
	 * Constructors
	 */
	
	public SelectClause() {
		
		_statements = new Vector<SubTreeSelectStatement>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(SubTreeSelectStatement statement) {
		
		_statements.add(statement);
	}
	
	public void consume(QueryNodeSet nodeSet, QueryNodeHandler consumer) {

		if (_resultSchema == null) {
			// Create the schema node of the result. Make sure to rename schema nodes
			// having the same name.
			Hashtable<String, Counter> labelIndex = new Hashtable<String, Counter>();
			// Hash table for labels that occur more than once in the output schema
			Hashtable<String, Counter> labelCounter = new Hashtable<String, Counter>();
			for (SubTreeSelectStatement stmt : _statements) {
				String label = null;
				if (stmt.label() != null) {
					label = stmt.label();
				} else {
					label = stmt.targetPath().lastElement().entity().label();
				}
				if (labelIndex.containsKey(label)) {
					if (!labelCounter.containsKey(label)) {
						labelCounter.put(label,  labelIndex.get(label));
					}
					labelIndex.get(label).inc();
				} else {
					labelIndex.put(label,  new Counter());
				}
			}
			// Reset the counter for labels that occur more than once
			Iterator<Counter> elements = labelCounter.values().iterator();
			while (elements.hasNext()) {
				elements.next().reset(1);
			}
			try {
				_resultSchema = new GroupSchemaNode(-1, "result", null, new TimeSequence(1));
				for (SubTreeSelectStatement stmt : _statements) {
					SchemaNode schema = stmt.targetPath().lastElement().entity();
					String label = null;
					if (stmt.label() != null) {
						label = stmt.label();
					} else {
						label = schema.label();
					}
					if (labelCounter.containsKey(label)) {
						Counter counter = labelCounter.get(label);
						label = label + Integer.toString(counter.value());
						stmt.setLabel(label);
						counter.inc();
					}
					SchemaNode renamedSchema = null;
					if (schema.isAttribute()) {
						renamedSchema = new AttributeSchemaNode(-1, label, _resultSchema, schema.getTimestamp());
						//renamedSchema = new AttributeSchemaNode(schema.id(), label, _resultSchema, schema.getTimestamp());
					} else {
						renamedSchema = new GroupSchemaNode(-1, label, _resultSchema, schema.getTimestamp());
						//renamedSchema = new GroupSchemaNode(schema.id(), label, _resultSchema, schema.getTimestamp());
						for (int iChild = 0; iChild < ((GroupSchemaNode)schema).children().size(); iChild++) {
							((GroupSchemaNode)renamedSchema).children().add(((GroupSchemaNode)schema).children().get(iChild));
						}
					}
				}
			} catch (org.dbwiki.exception.WikiException wikiException) {
				wikiException.printStackTrace();
				return;
			}
		}

		Vector<DatabaseElementNode> outputNodes = new Vector<DatabaseElementNode>();
		for (SubTreeSelectStatement stmt : _statements) {
			DatabaseElementNode node = nodeSet.get(stmt.targetPath().variableName());
			QueryOutputNodeCollector nodeMarker = null;
			if(stmt.label() == null)
				nodeMarker = new QueryOutputNodeCollector(outputNodes);
			else
				try {
					nodeMarker = new QueryOutputNodeCollector(outputNodes, _resultSchema.find(stmt.label()));
				} catch (WikiException e) {
					e.printStackTrace();
				}
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
			}
			DatabaseGroupNode result = new ResultGroupNode(_resultSchema, timestamp);
			for (DatabaseElementNode node : outputNodes) {
				result.children().add(node);
			}
			consumer.handle(result);
		}
	}
}
