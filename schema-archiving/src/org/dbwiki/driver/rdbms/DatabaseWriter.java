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

import org.dbwiki.data.resource.EntityIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;


import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

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
 * 
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

	public void insertEntity(Entity entity, Version version) throws org.dbwiki.exception.WikiException {
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
			insert.setInt(1, entity.id());
			if (entity.isAttribute()) {
				insert.setInt(2, RelSchemaColTypeValAttribute);
			} else {
				insert.setInt(2, RelSchemaColTypeValGroup);
			}
			insert.setString(3, entity.label());
			if (entity.parent() != null) {
				insert.setInt(4, entity.parent().id());
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
				else if (identifier instanceof EntityIdentifier)
					recordNewSchemaTimestamp(((EntityIdentifier)identifier).nodeID(),
															getGeneratedKey(insert));
			}
			insert.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	// yuck... we should really share the code of 
	// insertTimestamp and insertSchemaTimestamp somehow
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
	
	public ResourceIdentifier insertRootNode(DocumentGroupNode node, Version version) throws org.dbwiki.exception.WikiException {
		try {
			RDBMSDatabaseGroupNode root = this.insertGroupNode((GroupEntity)node.entity(), null, -1, new TimeSequence(version.number(), _database.versionIndex()));
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

	public ResourceIdentifier insertNode(NodeIdentifier identifier, DocumentNode node, Version version) throws org.dbwiki.exception.WikiException {
		RDBMSDatabaseGroupNode parent = (RDBMSDatabaseGroupNode)DatabaseReader.get(_con, _database, identifier);
		
		DatabaseNode entry = parent;
		while (entry.parent() != null) {
			entry = entry.parent();
		}

		ResourceIdentifier nodeIdentifier = null;
		
		try {
			if (node.isAttribute()) {
				nodeIdentifier = insertAttributeNode((AttributeEntity)node.entity(), parent, ((NodeIdentifier)entry.identifier()).nodeID(), new TimeSequence(version), ((DocumentAttributeNode)node).value()).identifier();
			} else {
				RDBMSDatabaseGroupNode group = insertGroupNode((GroupEntity)node.entity(), parent, ((NodeIdentifier)entry.identifier()).nodeID(), new TimeSequence(version));
				insertGroupChildren((DocumentGroupNode)node, group, ((NodeIdentifier)entry.identifier()).nodeID());
				nodeIdentifier = group.identifier();
			}
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
		return nodeIdentifier;
	}

	public void updateTimestamp(ResourceIdentifier identifier, TimeInterval interval) throws org.dbwiki.exception.WikiException {
		try {
			int timestamp = getTimestamp(identifier);
			PreparedStatement pStmtUpdateTimestamp = _con.prepareStatement(
				"UPDATE " + _database.name() + RelationTimestamp + " " +
					"SET " + RelTimestampColEnd + " = ? " +
					"WHERE " + RelTimestampColID + " = ? AND " + RelTimestampColStart + " = ?");
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
		
// APPEARS TO BE DEAD CODE
//	public void updatePage(int id, String content, long timestamp) throws WikiFatalException {
//		
//		try {
//			PreparedStatement pStmtUpdatePage = con.prepareStatement(
//				"UPDATE " + database.name() + RelationPages + " " +
//				    "SET " +
//				    RelPagesColContent + " = ?, " +
//				    RelPagesColTimestamp + " = ? " +
//				    "WHERE " + RelPagesColID + " = ?");
//			pStmtUpdatePage.setString(1, content);
//			pStmtUpdatePage.setLong(2, timestamp);
//			pStmtUpdatePage.setInt(3, id);
//			pStmtUpdatePage.execute();
//		} catch (java.sql.SQLException sqlException) {
//			throw new WikiFatalException(sqlException);
//		}
//	}

	
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
		return getTimestamp(RelationSchema, RelSchemaColID, RelSchemaColTimestamp, id);
	}
	
	private int getNodeTimestamp(int id) throws SQLException, WikiFatalException {
		return getTimestamp(RelationData, RelDataColID, RelDataColTimestamp, id);		
	}
	
	private int getTimestamp(ResourceIdentifier identifier) throws SQLException, WikiFatalException {
		int timestamp = -1;
		
		if (identifier instanceof NodeIdentifier)
			timestamp = getNodeTimestamp(((NodeIdentifier)identifier).nodeID());
		else if (identifier instanceof EntityIdentifier)
			timestamp = getSchemaTimestamp(((EntityIdentifier)identifier).nodeID());
		
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
		recordNewTimestamp(RelationSchema, RelSchemaColID, RelSchemaColTimestamp, id, timestamp);
	}
	
	private void recordNewNodeTimestamp(int id, int timestamp) throws SQLException {
		recordNewTimestamp(RelationData, RelDataColID, RelDataColTimestamp, id, timestamp);
	}
		
	private RDBMSDatabaseAttributeNode 
			insertAttributeNode(
				AttributeEntity entity, 
				RDBMSDatabaseGroupNode parent, 
				int entry, TimeSequence timestamp, 
				String value)
			throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		RDBMSDatabaseAttributeNode attribute = new RDBMSDatabaseAttributeNode(this.insertNode(entity, parent, entry, timestamp, null), entity, parent, timestamp);
		this.insertNode(null, attribute, entry, null, value);
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
				insertAttributeNode((AttributeEntity)attributeChild.entity(), parent, entry, null, attributeChild.value());
			} else {
				DocumentGroupNode groupChild = (DocumentGroupNode)element;
				RDBMSDatabaseGroupNode node = insertGroupNode((GroupEntity)groupChild.entity(), parent, entry, null);
				insertGroupChildren(groupChild, node, entry);
			}
		}
	}

	private RDBMSDatabaseGroupNode 
			insertGroupNode(
				GroupEntity entity, 
				RDBMSDatabaseGroupNode parent, 
				int entry, 
				TimeSequence timestamp)
	 		throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		return new RDBMSDatabaseGroupNode(insertNode(entity, parent, entry, timestamp, null), entity, parent, timestamp);
	}
	
	private int insertNode(Entity entity, DatabaseNode parent, int entry, TimeSequence timestamp, String value) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		PreparedStatement insert = prepareInsertNode();
		if (entity != null) {
			insert.setInt(1, entity.id());
		} else {
			insert.setInt(1, RelDataColEntityValUnknown);
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
	
	private RDBMSDatabaseTextNode insertTextNode(RDBMSDatabaseAttributeNode parent, int entry, TimeSequence timestamp, String value) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		return new RDBMSDatabaseTextNode(this.insertNode(null, parent, entry, timestamp, value), parent, timestamp, value);
	}

	private PreparedStatement prepareInsertNode() throws java.sql.SQLException {		
		return _con.prepareStatement(
			"INSERT INTO " + _database.name() + RelationData + "(" +
				RelDataColEntity + ", " +
				RelDataColParent + ", " +
				RelDataColEntry + ", " +
				RelDataColValue + ") VALUES(? ,? , ?, ?)", Statement.RETURN_GENERATED_KEYS);
	}

	private PreparedStatement prepareInsertTimestamp(boolean fresh) throws java.sql.SQLException {		
		if(fresh) {
			return _con.prepareStatement(
			"INSERT INTO " + _database.name() + RelationTimestamp + "(" +
			RelTimestampColStart + ", " +
			RelTimestampColEnd + ") VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
		} else {
			return _con.prepareStatement(
					"INSERT INTO " + _database.name() + RelationTimestamp + "(" +
					RelTimestampColID + ", " +
					RelTimestampColStart + ", " +
					RelTimestampColEnd + ") VALUES(?, ?, ?)");
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
				this.insertTextNode(attribute, entry, timestamp, node.value());
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
