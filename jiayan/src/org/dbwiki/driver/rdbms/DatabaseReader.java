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
package org.dbwiki.driver.rdbms;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;

import org.dbwiki.data.annotation.Annotation;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.resource.SchemaNodeIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeSequence;

import org.dbwiki.exception.WikiFatalException;

/** Provides static methods to get the nodes associated with a schema node or all
 *  of the descendants of a given data node.
 * FIXME #static Fold into RDBMSDatabase?
 * @author jcheney
 *
 */

public class DatabaseReader implements DatabaseConstants {
	/*
	 * Public Methods
	 */
	
	/**
	 * Load in the list of IDs of a given schema node.
	 */
	public static ArrayList<NodeIdentifier> getNodesOfSchemaNode(Connection con, RDBMSDatabase database, SchemaNodeIdentifier identifier)
		throws SQLException {
		
		ArrayList<NodeIdentifier> nodes = new ArrayList<NodeIdentifier>();
		
		Statement statement = con.createStatement();
		ResultSet rs = statement.executeQuery(
				"SELECT " + RelDataColID +  " FROM " + database.name() + RelationData + " " +
				"WHERE " + RelDataColSchema + " = " + identifier.nodeID());
		
		while (rs.next()) {
			int id = rs.getInt(RelDataColID);
			nodes.add(new NodeIdentifier(id));
		}
		
		return nodes;
	}
	
	
	/**
	 * Load in the node specified by @identifier (including its descendants)
	 * 
	 * This method loads all versions of the node and its children,
	 * all time intervals for which the node and its children were active,
	 * and all annotations for the node and its children.
	 */
	public static DatabaseNode get(Connection con, RDBMSDatabase database, NodeIdentifier identifier) throws org.dbwiki.exception.WikiException {
		Hashtable<Integer, DatabaseNode> nodeIndex = new Hashtable<Integer, DatabaseNode>();
		
		try {
			Statement stmt = con.createStatement();
			
			// This query returns a row for every
			// node that is in the same entry as the requested node.
			//
			// Using a more sophisticated indexing system
			// we might only return those rows that pertain to
			// the node and its descendants.
			//
			// We might use a join in place of the view for extracting
			// the time interval data. What about the annotations?
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM " + database.name() + ViewData + " " +
					"WHERE " + ViewDataColNodeEntry + " = (SELECT " + RelDataColEntry + " FROM " + database.name() + RelationData + " WHERE " + RelDataColID + " = " + identifier.nodeID() + ") " +
					"ORDER BY " + ViewDataColNodeID + ", " + ViewDataColTimestampStart + ", " + ViewDataColAnnotationID);
			DatabaseNode node = null;
			
			while (rs.next()) {
				int id = rs.getInt(ViewDataColNodeID);
				
				// The following condition allows information about nodes to be built up
				// incrementally when spread across multiple rows.
				//
				// It relies on the results being ordered first by node id.
				//
				// A single node id may appear in consecutive rows if the
				// node has existed in multiple time intervals, or if it has
				// multiple annotations.
				if (node == null || ((NodeIdentifier)node.identifier()).nodeID() != id)  {
					int schema = rs.getInt(ViewDataColNodeSchema);
					int parent = rs.getInt(ViewDataColNodeParent);
					if (schema != RelDataColSchemaValUnknown) {
						// FIXME #database: This seems to assume that the nodes are in parent-child order.
						// The following logic seems rather fragile.
						// It isn't at all clear that things will
						// work properly if a child node is encountered
						// in the result set before its parent.
						//
						// There doesn't appear to be any code for
						// connecting up parents to children later on.
						//
						// It may be that the problematic situation
						// never occurs. This seems quite likely, as
						// child nodes are always going to be created
						// after their parents, and ids are assigned
						// in sequence. If this is the case, then
						// the constraint should be explicitly stated
						// in the data model, we should implement code
						// for checking that the constraint hasn't been
						// violated, and the following null-checking code
						// should be removed.
						RDBMSDatabaseGroupNode parentNode = null;
						if (parent != RelDataColParentValUnknown) {
							parentNode = (RDBMSDatabaseGroupNode)nodeIndex.get(new Integer(parent));
						}
						SchemaNode schemaNode = database.schema().get(schema);
						if (schemaNode.isAttribute()) {
							node = new RDBMSDatabaseAttributeNode(id, (AttributeSchemaNode)schemaNode, parentNode);
						} else {
							node = new RDBMSDatabaseGroupNode(id, (GroupSchemaNode)schemaNode, parentNode);
						}
						if (parentNode != null) {
							parentNode.children().add((DatabaseElementNode)node);
						}
					} else {
						RDBMSDatabaseAttributeNode parentNode = (RDBMSDatabaseAttributeNode)nodeIndex.get(new Integer(parent));
						node =  new RDBMSDatabaseTextNode(id, parentNode, rs.getString(ViewDataColNodeValue));
						parentNode.value().add((DatabaseTextNode)node);
					}
					nodeIndex.put(new Integer(id), node);
				}
				int end = RelTimestampColEndValOpen;
				int start = rs.getInt(ViewDataColTimestampStart);
				if (!rs.wasNull()) {
					end = rs.getInt(ViewDataColTimestampEnd);
					if (!node.hasTimestamp()) {
						node.setTimestamp(new TimeSequence(start, end));
					} else {
						node.getTimestamp().elongate(start, end);
					}
				}
				int annotationID = rs.getInt(ViewDataColAnnotationID);
				if (!rs.wasNull()) {
					if (!node.annotation().contains(annotationID)) {
						node.annotation().add(new Annotation(annotationID, rs.getString(ViewDataColAnnotationText), rs.getString(ViewDataColAnnotationDate), database.users().get(rs.getInt(ViewDataColAnnotationUser))));
					}
				}
			}
			rs.close();
			stmt.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		return nodeIndex.get(new Integer(identifier.nodeID()));
	}
}
