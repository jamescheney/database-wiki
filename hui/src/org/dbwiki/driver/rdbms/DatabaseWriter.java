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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbwiki.data.annotation.Annotation;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.resource.SchemaNodeIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;


import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.Version;


import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;


/** 
 * Provides capabilities to insert annotations, entities, timestamps, schema timestamps, and nodes.
 * Also to update timestamps and nodes.
 * FIXME #static: Fold into RDBMSDatabase?  
 * This class is basically always used 
 * in RDBMSDatabase (and once in DatabaseImportHandler) by creating a new instance and 
 * calling a single method. That is, its methods are morally just being used statically.
 * FIXME #document_this
 * @author jcheney
 *
 */
public class DatabaseWriter implements DatabaseConstants {
	private Connection _con;
	private RDBMSDatabase _database;
	
	public DatabaseWriter(Connection con, RDBMSDatabase database) {
		this._con = con;
		this._database = database;
	}
	
	/*
	 * Public Methods
	 */
	/** Using a query, insert an annotation to identified node.
	 * 
	 */
	public void insertAnnotation(NodeIdentifier identifier, Annotation annotation) throws org.dbwiki.exception.WikiException {
		try {
			PreparedStatement pStmtInsertAnnotation = _con.prepareStatement(
				"INSERT INTO " + _database.name() + RelationAnnotation + "(" +
					RelAnnotationColNode + ", " +
					RelAnnotationColDate + ", " +
					RelAnnotationColUser + ", " + 
					RelAnnotationColText + ") VALUES(?, ?, ?, ?)");
			pStmtInsertAnnotation.setInt(1, identifier.nodeID());
			pStmtInsertAnnotation.setString(2, annotation.date());
			if (annotation.user() != null) {
				pStmtInsertAnnotation.setInt(3, annotation.user().id());
			} else {
				pStmtInsertAnnotation.setInt(3, User.UnknownUserID);
			}
			pStmtInsertAnnotation.setString(4, annotation.text());
			pStmtInsertAnnotation.execute();
			pStmtInsertAnnotation.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	/** Via query, insert a new schema node with a given version.
	 * 
	 * @param schemaNode
	 * @param version
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void insertSchemaNode(SchemaNode schemaNode, Version version) throws org.dbwiki.exception.WikiException {
		User user = version.provenance().user();
		TimeSequence timestamp = new TimeSequence(version);
		try {
			PreparedStatement insert = _con.prepareStatement(
				"INSERT INTO " + _database.name() + RelationSchema + " (" +
					RelSchemaColID + ", " +
					RelSchemaColType + ", " +
					RelSchemaColLabel + ", " +
					RelSchemaColParent + ", " +
					RelSchemaColUser + ")" +
					" VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			insert.setInt(1, schemaNode.id());
			if (schemaNode.isAttribute()) {
				insert.setInt(2, RelSchemaColTypeValAttribute);
			} else {
				insert.setInt(2, RelSchemaColTypeValGroup);
			}
			insert.setString(3, schemaNode.label());
			if (schemaNode.parent() != null) {
				insert.setInt(4, schemaNode.parent().id());
			} else {
				insert.setInt(4, RelSchemaColParentValUnknown);
			}
			if (user != null) {
				insert.setInt(5, user.id());
			} else {
				insert.setInt(5, User.UnknownUserID);
			}

			insert.execute();
			int id = getGeneratedKey(insert);
			insert.close();
			
			PreparedStatement insertTimestamp = prepareInsertTimestamp(true);

			insertTimestamp.setInt(1, timestamp.firstValue());
			insertTimestamp.setInt(2, RelTimestampColEndValOpen);
			insertTimestamp.execute();
				
			recordNewSchemaTimestamp(id, getGeneratedKey(insertTimestamp));
			
			
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	/**
	 * Adds a new time interval to an identified resource. 
	 * TODO: Check for interval overlap, complain if violated.
	 * @param identifier
	 * @param interval
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void insertTimestamp(ResourceIdentifier identifier, TimeInterval interval)
	throws org.dbwiki.exception.WikiException {
		try {
			int timestamp = getTimestamp(identifier);
			PreparedStatement insert = prepareInsertTimestamp(timestamp == -1);
			
			// if the node doesn't yet have a timestamp id, then
			// create a fresh one, otherwise use the existing one
			if(timestamp == -1) {
				insert.setInt(1, interval.start());
				insert.setInt(2, interval.end());
			} else {
				insert.setInt(1, timestamp);
				insert.setInt(2, interval.start());
				insert.setInt(3, interval.end());
			}
			insert.execute();
			
			// we only need to record the timestamp the first time an interval
			// is explicitly associated with this node
			if(timestamp == -1) {
				if (identifier instanceof NodeIdentifier)
					recordNewNodeTimestamp(((NodeIdentifier)identifier).nodeID(),
															getGeneratedKey(insert));
				else if (identifier instanceof SchemaNodeIdentifier)
					recordNewSchemaTimestamp(((SchemaNodeIdentifier)identifier).nodeID(),
															getGeneratedKey(insert));
			}
			insert.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	/** 
	 * Inserts a time interval to be associated with a schema node.
	 * @param id
	 * @param interval
	 * @throws org.dbwiki.exception.WikiException
	 */
	// yuck... we should really share the code of 
	// insertTimestamp and insertSchemaTimestamp somehow
	// FIXME: Make schema nodes identifiable via ResourceIdentifiers?
	public void insertSchemaTimestamp(int id, TimeInterval interval)
	throws org.dbwiki.exception.WikiException {
		try {
			int timestamp = getSchemaTimestamp(id);
			PreparedStatement insert = prepareInsertTimestamp(timestamp == -1);
			
			// if the node doesn't yet have a timestamp id, then
			// create a fresh one, otherwise use the existing one
			if(timestamp == -1) {
				insert.setInt(1, interval.start());
				insert.setInt(2, interval.end());
			} else {
				insert.setInt(1, timestamp);
				insert.setInt(2, interval.start());
				insert.setInt(3, interval.end());
			}
			insert.execute();
			
			// we only need to record the timestamp the first time an interval
			// is explicitly associated with this node
			if(timestamp == -1) {
				recordNewSchemaTimestamp(id, getGeneratedKey(insert));
			}
			insert.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	/** Inserts a database wiki root node.
	 * 
	 * @param node
	 * @param version
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public ResourceIdentifier insertRootNode(DocumentGroupNode node, Version version) throws org.dbwiki.exception.WikiException {
		try {
			node.doNumberingRoot();
			RDBMSDatabaseGroupNode root = this.insertGroupNode((GroupSchemaNode)node.schema(), null, -1, new TimeSequence(version.number(), _database.versionIndex()), node.getpre(), node.getpost());
			this.insertGroupChildren(node, root, root.identifier().nodeID());
			PreparedStatement pStmtUpdateNode = _con.prepareStatement(
				"UPDATE " + _database.name() + RelationData + " " +
					"SET " + RelDataColEntry + " = ? WHERE " + RelDataColID + " = ?");
			pStmtUpdateNode.setInt(1, root.identifier().nodeID());
			pStmtUpdateNode.setInt(2, root.identifier().nodeID());
			pStmtUpdateNode.execute();
			pStmtUpdateNode.close();
    		return root.identifier();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	/** Inserts a database wiki node with given version and id
	 * 
	 * @param identifier
	 * @param node
	 * @param version
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 * @throws IOException 
	 */
	public ResourceIdentifier insertNode(NodeIdentifier identifier, DocumentNode node, Version version) throws org.dbwiki.exception.WikiException {
		RDBMSDatabaseGroupNode parent = (RDBMSDatabaseGroupNode)DatabaseReader.get(_con, _database, identifier);
		
		DatabaseNode entry = parent;
		while (entry.parent() != null) {
			entry = entry.parent();
		}

		ResourceIdentifier nodeIdentifier = null;
		
		try {
			if (node.isAttribute()) {
				nodeIdentifier = insertAttributeNode((AttributeSchemaNode)node.schema(), parent, ((NodeIdentifier)entry.identifier()).nodeID(), new TimeSequence(version), ((DocumentAttributeNode)node).value(), node.getpre(), node.getpost()).identifier();
			} else {
				RDBMSDatabaseGroupNode group = insertGroupNode((GroupSchemaNode)node.schema(), parent, ((NodeIdentifier)entry.identifier()).nodeID(), new TimeSequence(version),node.getpre(), node.getpost());
				insertGroupChildren((DocumentGroupNode)node, group, ((NodeIdentifier)entry.identifier()).nodeID());
				nodeIdentifier = group.identifier();
			}
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
		
		 
	
		
		return nodeIdentifier;
	}

	/** Update the time interval starting at interval.start() associated with an identified resource with a new end.
	 * FIXME: Check here and complain if there are multiple intervals starting at "start" 
	 * @param identifier
	 * @param interval
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void updateTimestamp(ResourceIdentifier identifier, TimeInterval interval) throws org.dbwiki.exception.WikiException {
		try {
			int timestamp = getTimestamp(identifier);
			PreparedStatement pStmtUpdateTimestamp = _con.prepareStatement(
				"UPDATE " + _database.name() + RelationTimesequence + " " +
					"SET " + RelTimesequenceColStop + " = ? " +
					"WHERE " + RelTimesequenceColID + " = ? AND " + RelTimesequenceColStart + " = ?");
			pStmtUpdateTimestamp.setInt(1, interval.end());
			pStmtUpdateTimestamp.setInt(2, timestamp);
			pStmtUpdateTimestamp.setInt(3, interval.start());
			pStmtUpdateTimestamp.execute();
			pStmtUpdateTimestamp.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void updateNode(DatabaseNode node) throws org.dbwiki.exception.WikiException {
		DatabaseNode entry = node;
		while (entry.parent() != null) {
			entry = entry.parent();
		}

		try {
			int entryID = ((NodeIdentifier)entry.identifier()).nodeID();
			
			// ugly casts - perhaps a visitor
			// would be appropriate here?
			if (node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)node;
				if (element.isAttribute()) {
					writeTextNodes((RDBMSDatabaseAttributeNode)element, entryID);
				} else {
					writeTextNodes((RDBMSDatabaseGroupNode)element, entryID);
				}
			} else {
				writeTextNodes((RDBMSDatabaseAttributeNode)node.parent(), entryID);
			}
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	/*
	 * Private Methods
	 */
	private int getTimestamp(String relation, String idColumn, String timestampColumn, int id) throws SQLException, WikiFatalException {
		PreparedStatement q = _con.prepareStatement("" +
				"SELECT " + timestampColumn +
				" FROM " + _database.name() + relation + " " +
				" WHERE " + RelSchemaColID + " = " + id
				);
		ResultSet rs = q.executeQuery();
		if (rs.next()) {
			int timestamp = rs.getInt(1);
			rs.close();
			return timestamp;
		} else {
	        throw new WikiFatalException("Unknown id: " + id);
	    }	
		
	}
	
	private int getSchemaTimestamp(int id) throws SQLException, WikiFatalException {
		return getTimestamp(RelationSchema, RelSchemaColID, RelSchemaColTimesequence, id);
	}
	
	private int getNodeTimestamp(int id) throws SQLException, WikiFatalException {
		return getTimestamp(RelationData, RelDataColID, RelDataColTimesequence, id);		
	}
	
	private int getTimestamp(ResourceIdentifier identifier) throws SQLException, WikiFatalException {
		int timestamp = -1;
		
		if (identifier instanceof NodeIdentifier)
			timestamp = getNodeTimestamp(((NodeIdentifier)identifier).nodeID());
		else if (identifier instanceof SchemaNodeIdentifier)
			timestamp = getSchemaTimestamp(((SchemaNodeIdentifier)identifier).nodeID());
		
		return timestamp;
	}
	
	private int getGeneratedKey(PreparedStatement insertStatement) throws WikiFatalException, SQLException {
		ResultSet rs = insertStatement.getGeneratedKeys();
		if (rs.next()) {
			int key = rs.getInt(1);
			rs.close();
			return key;
	    } else {
	        throw new WikiFatalException("There are no generated keys.");
	    }	
	}
	
	/**
	 * Point a row at its newly generated timestamp.
	 */
	private void recordNewTimestamp(String relation, String idColumn, String timestampColumn, int id, int timestamp)
		throws SQLException {		
		PreparedStatement updateNode = _con.prepareStatement(
				"UPDATE " + _database.name() + relation + " " +
					"SET " + timestampColumn + " = ? WHERE " + idColumn + " = ?");
		updateNode.setInt(1, timestamp);
		updateNode.setInt(2, id);
		updateNode.execute();
		updateNode.close();
	}
	
	private void recordNewSchemaTimestamp(int id, int timestamp) throws SQLException {
		recordNewTimestamp(RelationSchema, RelSchemaColID, RelSchemaColTimesequence, id, timestamp);
	}
	
	private void recordNewNodeTimestamp(int id, int timestamp) throws SQLException {
		recordNewTimestamp(RelationData, RelDataColID, RelDataColTimesequence, id, timestamp);
	}
		
	private RDBMSDatabaseAttributeNode 
			insertAttributeNode(
				AttributeSchemaNode schema, 
				RDBMSDatabaseGroupNode parent, 
				int entry, TimeSequence timestamp, 
				String value,
				int pre, 
				int post) 

			throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		RDBMSDatabaseAttributeNode attribute = new RDBMSDatabaseAttributeNode(this.insertNode(schema, parent, entry, timestamp, null, pre, post), schema, parent, timestamp, pre, post);
		this.insertNode(null, attribute, entry, null, value, pre, post);
		return attribute;
	}

	private void 
			insertGroupChildren(
				DocumentGroupNode group, 
				RDBMSDatabaseGroupNode parent, 
				int entry) 
			throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			DocumentNode element = group.children().get(iChild);
			if (element.isAttribute()) {
				DocumentAttributeNode attributeChild = (DocumentAttributeNode)element;
				insertAttributeNode((AttributeSchemaNode)attributeChild.schema(), parent, entry, null, attributeChild.value(), attributeChild.getpre(), attributeChild.getpost());

			} else {
				DocumentGroupNode groupChild = (DocumentGroupNode)element;
				RDBMSDatabaseGroupNode node = insertGroupNode((GroupSchemaNode)groupChild.schema(), parent, entry, null, groupChild.getpre(), groupChild.getpost());

				insertGroupChildren(groupChild, node, entry);
			}
		}
	}

	private RDBMSDatabaseGroupNode 
			insertGroupNode(
				GroupSchemaNode schema, 
				RDBMSDatabaseGroupNode parent, 
				int entry, 
				TimeSequence timestamp,
				int pre,
				int post) 
	 		throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		return new RDBMSDatabaseGroupNode(insertNode(schema, parent, entry, timestamp, null, pre, post), schema, parent, timestamp, pre, post);
	}
	
	private int insertNode(
			SchemaNode schema, 
			DatabaseNode parent, 
			int entry, 
			TimeSequence timestamp, 
			String value,
			int pre, 
			int post) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		PreparedStatement insert = prepareInsertNode();
		if (schema != null) {
			insert.setInt(1, schema.id());
		} else {
			insert.setInt(1, RelDataColSchemaValUnknown);
		}
		if (parent != null) {
			insert.setInt(2, ((NodeIdentifier)parent.identifier()).nodeID());
		} else {
			insert.setInt(2, RelDataColParentValUnknown);
		}
		insert.setInt(3, entry);
		if (value != null) {
			insert.setString(4, value);
		} else {
			insert.setString(4, null);
		}
		insert.setInt(5, pre);
        insert.setInt(6, post);
		insert.execute();
		int nodeID = getGeneratedKey(insert);
		insert.close();

		if (timestamp != null) {
			PreparedStatement insertTimestamp = prepareInsertTimestamp(true);

			insertTimestamp.setInt(1, timestamp.firstValue());
			insertTimestamp.setInt(2, RelTimestampColEndValOpen);
			insertTimestamp.execute();
			
			recordNewNodeTimestamp(nodeID, getGeneratedKey(insertTimestamp));
		}
		
	    return nodeID;
	}
	
	private  RDBMSDatabaseTextNode insertTextNode(RDBMSDatabaseAttributeNode parent, int entry, TimeSequence timestamp, String value, int pre, int post) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		return new RDBMSDatabaseTextNode(this.insertNode(null, parent, entry, timestamp, value, pre, post), parent, timestamp, value, pre, post);
	}

	private PreparedStatement prepareInsertNode() throws java.sql.SQLException {		
		return _con.prepareStatement(
			"INSERT INTO " + _database.name() + RelationData + "(" +
				RelDataColSchema + ", " +
				RelDataColParent + ", " +
				RelDataColEntry + ", " +
				RelDataColValue + ", " +
				RelDataColPre + ", " +
				RelDataColPost + ") VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
	}

	private PreparedStatement prepareInsertTimestamp(boolean fresh) throws java.sql.SQLException {		
		if(fresh) {
			return _con.prepareStatement(
			"INSERT INTO " + _database.name() + RelationTimesequence + "(" +
			RelTimesequenceColStart + ", " +
			RelTimesequenceColStop + ") VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
		} else {
			return _con.prepareStatement(
					"INSERT INTO " + _database.name() + RelationTimesequence + "(" +
					RelTimesequenceColID + ", " +
					RelTimesequenceColStart + ", " +
					RelTimesequenceColStop + ") VALUES(?, ?, ?)");
		}
	}

	
	private void writeTextNodes(RDBMSDatabaseAttributeNode attribute, int entry) throws java.sql.SQLException, org.dbwiki.exception.WikiException {		
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			DatabaseTextNode node = attribute.value().get(iValue);
			if (((NodeIdentifier)node.identifier()).nodeID() == RelDataColIDValUnknown) {
				TimeSequence timestamp = null;
				if (node.hasTimestamp()) {
					timestamp = node.getTimestamp();
				}
				this.insertTextNode(attribute, entry, timestamp, node.value(), attribute.getpre(), attribute.getpost());

			}
		}
	}
	
	private void writeTextNodes(RDBMSDatabaseGroupNode group, int entry) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			DatabaseElementNode child = group.children().get(iChild);
			if (child.isAttribute()) {
				this.writeTextNodes((RDBMSDatabaseAttributeNode)child, entry);
			} else {
				this.writeTextNodes((RDBMSDatabaseGroupNode)child, entry);
			}
		}
	}
	
	
}
