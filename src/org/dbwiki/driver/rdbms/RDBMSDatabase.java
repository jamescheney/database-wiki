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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.data.annotation.Annotation;
import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseNodeValue;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.database.NodeUpdate;
import org.dbwiki.data.database.Update;
import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.document.PasteAttributeNode;
import org.dbwiki.data.document.PasteElementNode;
import org.dbwiki.data.document.PasteGroupNode;
import org.dbwiki.data.document.PasteNode;
import org.dbwiki.data.document.PasteTextNode;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.VectorDatabaseListing;
import org.dbwiki.data.io.ImportHandler;
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.provenance.ProvenanceActivate;
import org.dbwiki.data.provenance.ProvenanceCopy;
import org.dbwiki.data.provenance.ProvenanceDelete;
import org.dbwiki.data.provenance.ProvenanceInsert;
import org.dbwiki.data.provenance.ProvenanceUnknown;
import org.dbwiki.data.provenance.ProvenanceUpdate;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.query.QueryStatement;
import org.dbwiki.data.query.condition.AttributeCondition;
import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.resource.SchemaNodeIdentifier;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.TimestampedObject;
import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiDataException;
import org.dbwiki.exception.data.WikiSchemaException;
import org.dbwiki.exception.web.WikiRequestException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.DatabaseWikiProperties;


/** Implementation of the Database interface using a relational database.
 * FIXME #document_this
 */

public class RDBMSDatabase implements Database, DatabaseConstants {
	/*
	 * Public Constants
	 */
	public static final String NAME   = "NAME";
	public static final String TITLE  = "TITLE";

	
	/*
	 * Private Variables
	 */
	private DatabaseConnector _connector;
	private DatabaseIdentifier _identifier;
	private SQLDatabaseSchema _schema;
	private SQLVersionIndex _versionIndex;
	private DatabaseWiki _wiki;
	
	/*
	 * Constructors
	 */
	public RDBMSDatabase(DatabaseWiki wiki, DatabaseConnector connector) throws org.dbwiki.exception.WikiException {
		_connector = connector;
		_wiki = wiki;
		
		_identifier = new DatabaseIdentifier(wiki.name());

		try {
			Connection con = connector.getConnection();
			_versionIndex = new SQLVersionIndex(con, wiki.name(), wiki.users(), false);
			_schema = new SQLDatabaseSchema(con, _versionIndex, wiki.name());
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	// HACK: the last two arguments supply existing session
	// information for initialising the database
	public RDBMSDatabase(DatabaseWiki wiki, DatabaseConnector connector,
						Connection con, SQLVersionIndex versionIndex)
		throws org.dbwiki.exception.WikiException {
		_connector = connector;
		_wiki = wiki;
		
		_identifier = new DatabaseIdentifier(wiki.name());
		try {
			//con = connector.getConnection();
			_versionIndex = versionIndex;
			_schema = new SQLDatabaseSchema(con, _versionIndex, wiki.name());
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public synchronized void activate(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();

		DatabaseNode node = DatabaseReader.get(con, this, (NodeIdentifier)identifier);

		Version version = _versionIndex.getNextVersion(new ProvenanceActivate(user, identifier));

		try {
			con.setAutoCommit(false);
			try {
				activateNode(con, node, version);
				_versionIndex.add(version);
				_versionIndex.store(con);
			} catch (org.dbwiki.exception.WikiException wikiException) {
				con.rollback();
				con.close();
				throw wikiException;
			}
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public synchronized void annotate(ResourceIdentifier identifier, Annotation annotation) throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();
		new DatabaseWriter(con, this).insertAnnotation((NodeIdentifier)identifier, annotation);
		try {
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	public RDBMSDatabaseListing content() throws org.dbwiki.exception.WikiException {
		RDBMSDatabaseListing listing = new RDBMSDatabaseListing(_connector.getConnection(), this);
		return listing;
	}

	public synchronized void delete(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();

		DatabaseNode node = DatabaseReader.get(con, this, (NodeIdentifier)identifier);
		Version version = _versionIndex.getNextVersion(new ProvenanceDelete(user, identifier));
		
		try {
			con.setAutoCommit(false);
			try {
				deleteNode(con, node, version);
				_versionIndex.add(version);
				_versionIndex.store(con);
			} catch (org.dbwiki.exception.WikiException wikiException) {
				con.rollback();
				con.close();
				throw wikiException;
			}
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	public synchronized void deleteSchemaNode(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();

		SchemaNode schemaNode = _schema.get(((SchemaNodeIdentifier)identifier).nodeID());
		Version version = _versionIndex.getNextVersion(new ProvenanceUnknown(user));
		
		try {
			con.setAutoCommit(false);
			try {
				// delete all nodes whose types are schemaNode
				
				ArrayList<NodeIdentifier> deletedNodes =
					DatabaseReader.getNodesOfSchemaNode(con, this, ((SchemaNodeIdentifier)identifier));
				
				for(NodeIdentifier nid : deletedNodes) {
					deleteNode(con, DatabaseReader.get(con, this, nid), version);
				}
				
				// delete the schema node from the schema
				deletetSchemaNode(con, schemaNode, version);
				_versionIndex.add(version);
				_versionIndex.store(con);
			} catch (org.dbwiki.exception.WikiException wikiException) {
				con.rollback();
				con.close();
				throw wikiException;
			}
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	public void export(ResourceIdentifier identifier, int version, NodeWriter out) throws org.dbwiki.exception.WikiException {
		out.startDatabase(this, version);
		if (identifier.isRootIdentifier()) {
			RDBMSDatabaseListing entries = content();
			for (int iEntry = 0; iEntry < entries.size(); iEntry++) {
				exportEntry(get(entries.get(iEntry).identifier()), version, out);
			}
		} else {
			out.startEntry();
			exportNode(get(identifier), version, out, true);
			out.endEntry();
		}
		out.endDatabase(this);
	}

	/** Validates data and generates error report to stderr
	 * 
	 */
	public void validate() throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();
		try {
			DatabaseReader.validateTimestamps(con, this); 
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public DatabaseNode get(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException {
		Connection con = _connector.getConnection();
		DatabaseNode node = DatabaseReader.get(con, this, (NodeIdentifier)identifier);
		try {
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		return node;
	}
	
	public SchemaNode getSchemaNode(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException {
		// the entire schema history is kept in memory
		return _schema.get(((SchemaNodeIdentifier)identifier).nodeID());
	}

	// TODO: This probably belongs somewhere else, like the ui level.
	public AttributeSchemaNode getDisplaySchemaNode() {
		return _wiki.layouter().displaySchemaNode(schema());
	}
	
	public ResourceIdentifier getIdentifierForParameterString(String parameterValue) throws org.dbwiki.exception.WikiException {
		try {
			return new NodeIdentifier(Integer.parseInt(parameterValue));
		} catch (java.lang.NumberFormatException exception) {
			throw new WikiRequestException(WikiRequestException.InvalidUrl, parameterValue);
		}
	}

	public DatabaseContent getMatchingEntries(AttributeConditionListing listing) throws org.dbwiki.exception.WikiException {
		
		Vector<String> parameters = new Vector<String>();
		Vector<String> sqlStatements = new Vector<String>();
		
		for (int iCondition = 0; iCondition < listing.size(); iCondition++) {
			AttributeCondition condition = listing.get(iCondition);
			if (condition.isINDEX()) {
				this.addSchemaIndexStatement(sqlStatements, parameters, condition);
			} else {
				this.addSchemaValueStatement(sqlStatements, parameters, condition);
			}
		}
		
		String sql = _connector.joinMatchSQLStatements(sqlStatements, this.name());
		
		VectorDatabaseListing result = new VectorDatabaseListing();
		
		try {
			Connection con = _connector.getConnection();
			PreparedStatement pStmt = con.prepareStatement(sql);
			for (int iParameter = 0; iParameter < parameters.size(); iParameter++) {
				pStmt.setString(iParameter + 1, parameters.get(iParameter));
			}
			ResultSet rs = pStmt.executeQuery();
			RDBMSDatabaseListing content = this.content();
			while (rs.next()) {
				result.add(content.get(new NodeIdentifier(rs.getInt(1))));
			}
			rs.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
		return result;
	}
	
	public ResourceIdentifier getNodeIdentifierForURL(RequestURL url) throws org.dbwiki.exception.WikiException {
		return new NodeIdentifier(url);
	}

	public ResourceIdentifier getSchemaNodeIdentifierForURL(RequestURL url) throws org.dbwiki.exception.WikiException {
		return new SchemaNodeIdentifier(url);
	}
	
	public DatabaseIdentifier identifier() {
		return _identifier;
	}

	public synchronized ResourceIdentifier insertNode(ResourceIdentifier identifier, DocumentNode node, User user) throws org.dbwiki.exception.WikiException {
		ResourceIdentifier nodeIdentifier = null;

		Version version = _versionIndex.getNextVersion(new ProvenanceInsert(user, identifier));

		try {
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			try {
				if (identifier.isRootIdentifier()) {
					nodeIdentifier = new DatabaseWriter(con, this).insertRootNode((DocumentGroupNode)node, version);			
				} else {
					nodeIdentifier = new DatabaseWriter(con, this).insertNode((NodeIdentifier)identifier, node, version);
				}
				_versionIndex.add(version);
				_versionIndex.store(con);
			} catch (org.dbwiki.exception.WikiException wikiException) {
				con.rollback();
				con.close();
				throw wikiException;
			}
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		return nodeIdentifier;
	}
	
	public synchronized void insertSchemaNode(GroupSchemaNode parent, String name, byte type, User user) throws org.dbwiki.exception.WikiException {
		if (!DatabaseSchema.isValidName(name)) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid element name " + name);
		}
		
		Version version = _versionIndex.getNextVersion(new ProvenanceUnknown(user));
		
		SchemaNode schema = null;
		if (type == SchemaNodeTypeAttribute) {
			if (_schema.size() == 0) {
				throw new WikiSchemaException(WikiSchemaException.InvalidSchemaType, "Schema root cannot be an attribute");
			}
			schema = new AttributeSchemaNode(_schema.size(), name, parent, new TimeSequence(version));
		} else if (type == SchemaNodeTypeGroup) {
			schema = new GroupSchemaNode(_schema.size(), name, parent, new TimeSequence(version));
		} else {
			throw new WikiSchemaException(WikiSchemaException.InvalidSchemaType, String.valueOf(type));
		}
		
		try {
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			new DatabaseWriter(con, this).insertSchemaNode(schema, version);
			_versionIndex.add(version);
			_versionIndex.store(con);
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		_schema.add(schema);
	}

	public String name() {
		return _wiki.name();
	}

	public synchronized void paste(ResourceIdentifier target, PasteNode pasteNode, String sourceURL, User user) throws org.dbwiki.exception.WikiException {
		DatabaseElementNode targetElement = null;
		
		if (!target.isRootIdentifier()) {
			DatabaseNode targetNode = get(target);
			if (targetNode.isText()) {
				targetElement = targetNode.parent();
			} else {
				targetElement = (DatabaseElementNode)targetNode;
			}
		} else if (!pasteNode.isElement()) {
			throw new WikiDataException(WikiDataException.InvalidPasteTarget, target.toParameterString());
		}
		
		if (pasteNode.isElement()) {
			if (targetElement != null) {
				if (!targetElement.isGroup()) {
					throw new WikiDataException(WikiDataException.InvalidPasteTarget, target.toParameterString());
				}
			}
			Connection con = _connector.getConnection();			
			try {
				SQLDatabaseSchema schema = new SQLDatabaseSchema(con, _versionIndex, _wiki.name());
				DocumentNode insertNode = null;
				if (targetElement != null) {
					// FIXME #copypaste: This code looks unnecessarily complicated
					// Isn't:
					//
					//   (GroupSchemaNode)schema.get(targetElement.schema().id())
					//
					// entirely equivalent to:
					//
					//   (GroupSchemaNode)targetElement.schema()
					//
					// ?
					insertNode = getPasteInsertNode((GroupSchemaNode)schema.get(targetElement.schema().id()), (PasteElementNode)pasteNode, schema);
				} else {
					insertNode = getPasteInsertNode(null, (PasteElementNode)pasteNode, schema);
				}
				if (insertNode != null) {
					Version version = _versionIndex.getNextVersion(new ProvenanceCopy(user, target, sourceURL));
					try {
						con.setAutoCommit(false);
						try {
							if (target.isRootIdentifier()) {
								new DatabaseWriter(con, this).insertRootNode((DocumentGroupNode)insertNode, version);			
							} else {
								new DatabaseWriter(con, this).insertNode((NodeIdentifier)target, insertNode, version);
							}
							for (int i = schema().size(); i < schema.size(); i++) {
								new DatabaseWriter(con, this).insertSchemaNode(schema.get(i), version);
							}
							_versionIndex.add(version);
							_versionIndex.store(con);
						} catch (org.dbwiki.exception.WikiException wikiException) {
							con.rollback();
							con.close();
							throw wikiException;
						}
						con.commit();
						con.close();
					} catch (java.sql.SQLException sqlException) {
						throw new WikiFatalException(sqlException);
					}
					_schema = schema;
				}
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
		} else {
			// targetElement should be nonnull
			assert(targetElement != null);
			if (!targetElement.isAttribute()) {
				throw new WikiDataException(WikiDataException.InvalidPasteTarget, target.toParameterString());
			}
			Update update = new Update();
			update.add(new NodeUpdate(targetElement.identifier(), ((PasteTextNode)pasteNode).getValue()));
			updateNodeWrapped(targetElement, update, _versionIndex.getNextVersion(new ProvenanceCopy(user, targetElement.identifier(), sourceURL)));
		}
	}

	/** Evaluates a wiki query with respect to the database */
	public QueryResultSet query(String query) throws org.dbwiki.exception.WikiException {
		return QueryStatement.createStatement(this, query).execute();
	}

	public DatabaseSchema schema() {
		return _schema;
	}

	public DatabaseContent search(String query) throws org.dbwiki.exception.WikiException {
		DatabaseQuery keywords = new DatabaseQuery(query);
		
		RDBMSDatabaseListing entries = content();
		
		VectorDatabaseListing result = new VectorDatabaseListing();
		if (keywords.size() > 0) {
			String union = "SELECT '0' kwid, " + RelDataColEntry + ", COUNT(*) cnt FROM " + name() + RelationData + " WHERE UPPER(" + RelDataColValue + ") LIKE '%" + keywords.get(0).toUpperCase() + "%' GROUP BY kwid, " + RelDataColEntry;
			for (int iKW = 1; iKW < keywords.size(); iKW++) {
				union = union + " UNION SELECT '" + iKW + "' kwid, "+ RelDataColEntry + ", COUNT(*) FROM " + name() + RelationData + " WHERE UPPER(" + RelDataColValue + ") LIKE '%" + keywords.get(iKW).toUpperCase() + "%' GROUP BY kwid, " + RelDataColEntry;
			}
			String sql = "(SELECT " + RelDataColEntry + ", COUNT(kwid), SUM(cnt) FROM (" + union + ") AS u GROUP BY " + RelDataColEntry + " ORDER BY COUNT(kwid) DESC, SUM(cnt) DESC) ";
			try {
				Connection con = _connector.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					NodeIdentifier identifier = new NodeIdentifier(rs.getInt(RelDataColEntry));
					result.add(entries.get(identifier));
				}
				rs.close();
				stmt.close();
				con.close();
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
		}
		return result;
	}

	public synchronized void update(ResourceIdentifier identifier, Update update, User user) throws org.dbwiki.exception.WikiException {
		updateNodeWrapped(get(identifier), update, _versionIndex.getNextVersion(new ProvenanceUpdate(user, identifier)));
	}

	public UserListing users() {
		return _wiki.users();
	}

	public VersionIndex versionIndex() {
		return _versionIndex;
	}
	
	/*
	 * Private Methods
	 */
	
	private void activateNode(Connection con, DatabaseNode node, Version version) throws org.dbwiki.exception.WikiException {
		if (!node.getTimestamp().isCurrent()) {
			boolean activeParent = true;
			DatabaseElementNode parent = node.parent();
			if (parent != null) {
				activeParent = parent.getTimestamp().isCurrent();
			}
			if (activeParent) {
				int deletedAt = node.getTimestamp().lastValue();
				if (node.isElement()) {
					if (node.hasTimestamp()) {
						insertTimestamp(con, node, node.getTimestamp().continueAt(version.number()));
					}
					activateElementNode(con, (DatabaseElementNode)node, deletedAt, version);
				} else {
					// FIXME: Suspicious sideways-cast, is this code reachable?
					assert(parent instanceof DatabaseAttributeNode);
					DatabaseNodeValue values = ((DatabaseAttributeNode)parent).value();
					if (values.size() > 1) {
						for (int iValue = 0; iValue < values.size(); iValue++) {
							DatabaseTextNode value = values.get(iValue);
							if (value.getTimestamp().isCurrent()) {
								updateTimestamp(con, value, value.getTimestamp().finishAt(version.number() - 1));
							}
						}
					}
					insertTimestamp(con, node, node.getTimestamp().continueAt(version.number()));
				}
			}
		}
	}

	private void activateElementNode(Connection con, DatabaseElementNode node, int deletedAt, Version version) throws org.dbwiki.exception.WikiException {
		if (node.isAttribute()) {
			DatabaseAttributeNode attribute = (DatabaseAttributeNode)node;
			for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
				DatabaseTextNode value = attribute.value().get(iValue);
				if ((value.hasTimestamp()) && (value.getTimestamp().lastValue() == deletedAt)) {
					insertTimestamp(con, value, value.getTimestamp().continueAt(version.number()));
				}
			}
		} else {
			DatabaseGroupNode group = (DatabaseGroupNode)node;
			for (int iChild = 0; iChild < group.children().size(); iChild++) {
				DatabaseElementNode child = group.children().get(iChild);
				if ((child.hasTimestamp()) && (child.getTimestamp().lastValue() == deletedAt)) {
					insertTimestamp(con, child, child.getTimestamp().continueAt(version.number()));
				}
				activateElementNode(con, child, deletedAt, version);
			}
		}
	}

	private void deletetSchemaNode(Connection con, SchemaNode schema, Version version) throws org.dbwiki.exception.WikiException {
		if (schema.getTimestamp().isCurrent()) {
			// mark the schema node itself as deleted
			updateTimestamp(con, schema, schema.getTimestamp().finishAt(version.number() - 1));
			if (schema instanceof GroupSchemaNode) {
				deleteGroupSchemaNode(con, (GroupSchemaNode)schema, version);
			}
		}
	}
	
	private void deleteNode(Connection con, DatabaseNode node, Version version) throws org.dbwiki.exception.WikiException {
		if (node.getTimestamp().isCurrent()) {
			updateTimestamp(con, node, node.getTimestamp().finishAt(version.number() - 1));
			if (node.isElement()) {
				deleteElementNode(con, (DatabaseElementNode)node, version);
			}
		}
	}

	private void deleteGroupSchemaNode(Connection con, GroupSchemaNode schema, Version version) throws org.dbwiki.exception.WikiException {
		for (int i = 0; i < schema.children().size(); i++) {
			SchemaNode child = schema.children().get(i);
			if ((child.hasTimestamp()) && child.getTimestamp().isCurrent()) {
				updateTimestamp(con, child, child.getTimestamp().finishAt(version.number() - 1));
			}
			deletetSchemaNode(con, child, version);
		}
	}
	
	private void deleteElementNode(Connection con, DatabaseElementNode node, Version version) throws org.dbwiki.exception.WikiException {
		if (node.isAttribute()) {
			DatabaseAttributeNode attribute = (DatabaseAttributeNode)node;
			for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
				DatabaseTextNode value = attribute.value().get(iValue);
				if ((value.hasTimestamp()) && (value.getTimestamp().isCurrent())) {
					updateTimestamp(con, value, value.getTimestamp().finishAt(version.number() - 1));
				}
			}
		} else {
			DatabaseGroupNode group = (DatabaseGroupNode)node;
			for (int iChild = 0; iChild < group.children().size(); iChild++) {
				DatabaseElementNode child = group.children().get(iChild);
				if ((child.hasTimestamp()) && (child.getTimestamp().isCurrent())) {
					updateTimestamp(con, child, child.getTimestamp().finishAt(version.number() - 1));
				}
				deleteElementNode(con, child, version);
			}
		}
	}
	
	private void exportEntry(DatabaseNode node, int version, NodeWriter out) throws org.dbwiki.exception.WikiException {
		
		out.startEntry();
		exportNode(node, version, out, true);
		out.endEntry();
	}
	
	private void exportNode(DatabaseNode node, int version, NodeWriter out, boolean last) throws org.dbwiki.exception.WikiException {
		if (node.getTimestamp().contains(version)) {
			if (node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)node;
				if (element.isAttribute()) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
					DatabaseTextNode value = null;
					for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
						if (attribute.value().get(iValue).getTimestamp().contains(version)) {
							value = attribute.value().get(iValue);
							break;
						}
					}
					out.writeAttributeNode(attribute, value,last);
				} else {
					DatabaseGroupNode group = (DatabaseGroupNode)element;
					out.startGroupNode(group);
					for (int iChild = 0; iChild < group.children().size(); iChild++) {
						boolean newLast = iChild == group.children().size() - 1;
						exportNode(group.children().get(iChild), version, out, newLast);
					}
					out.endGroupNode(group,last);
				}
			} else {
				out.writeTextNode((DatabaseTextNode)node);
			}
		}
	}
	
	private DocumentNode getPasteInsertNode(GroupSchemaNode parentSchemaNode, PasteElementNode sourceNode, DatabaseSchema schema) throws org.dbwiki.exception.WikiException {
		SchemaNode schemaNode = null;
		
		if (parentSchemaNode != null) {
			for (int i = 0; i < parentSchemaNode.children().size(); i++) {
				// TODO: we probably need to be careful about which bits of the
				// schema are current
				if (parentSchemaNode.children().get(i).label().equals(sourceNode.label())) {
					schemaNode = parentSchemaNode.children().get(i);
					break;
				}
			}
			if (schemaNode == null) {
				if (_wiki.getAutoSchemaChanges() == DatabaseWikiProperties.AutoSchemaChangesAllow) {
					if (sourceNode.isAttribute()) {
						schemaNode = new AttributeSchemaNode(schema.size(), sourceNode.label(), parentSchemaNode, null);
					} else {
						schemaNode = new GroupSchemaNode(schema.size(), sourceNode.label(), parentSchemaNode, null);
					}
					schema.add(schemaNode);
				} else if (_wiki.getAutoSchemaChanges() == DatabaseWikiProperties.AutoSchemaChangesNever) {
					throw new WikiDataException(WikiDataException.UnknownSchemaNode, sourceNode.label() + " not allowed under " + parentSchemaNode.label());
				}
			}
		} else if (schema.root() != null) {
			schemaNode = schema.root();
			if (!schemaNode.label().equals(sourceNode.label())) {
				throw new WikiDataException(WikiDataException.InvalidPasteTarget, "Node label does not match root label");
			}
		} else {
			if (_wiki.getAutoSchemaChanges() == DatabaseWikiProperties.AutoSchemaChangesAllow) {
				schemaNode = new GroupSchemaNode(schema.size(), sourceNode.label(), null, null);
				schema.add(schemaNode);
			} else if (_wiki.getAutoSchemaChanges() == DatabaseWikiProperties.AutoSchemaChangesNever) {
				throw new WikiDataException(WikiDataException.UnknownSchemaNode, sourceNode.label() + " not allowed as schema root");
			}
		}
		
		if (schemaNode != null) {
			if (schemaNode.isAttribute()) {
				return new DocumentAttributeNode((AttributeSchemaNode)schemaNode, ((PasteAttributeNode)sourceNode).getValue().getValue());
			} else {
				DocumentGroupNode group = new DocumentGroupNode((GroupSchemaNode)schemaNode);
				for (int iChild = 0; iChild < ((PasteGroupNode)sourceNode).children().size(); iChild++) {
					DocumentNode insertChild = getPasteInsertNode((GroupSchemaNode)schemaNode, (PasteElementNode)((PasteGroupNode)sourceNode).children().get(iChild), schema);
					if (insertChild != null) {
						group.children().add(insertChild);
					}
				}
				return group;
			}
		} else {
			return null;
		}
	}

	private void getValueIndex(DatabaseGroupNode group, Hashtable<String, DatabaseTextNode> valueIndex) {
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			DatabaseElementNode child = group.children().get(iChild);
			if (child.isAttribute()) {
				DatabaseTextNode value = ((DatabaseAttributeNode)child).value().getCurrent();
				if (value != null) {
					valueIndex.put(value.identifier().toParameterString(), value);
				}
			} else {
				getValueIndex((DatabaseGroupNode)child, valueIndex);
			}
		}
	}
	
	/**
	 * Update a node in a transaction.
	 */
	private void updateNodeWrapped(DatabaseNode node, Update update, Version version) throws org.dbwiki.exception.WikiException {
		try {
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			try {
				if (updateNodeTimestamps(con, node, update, version)) {
					new DatabaseWriter(con, this).updateNode(node);
					_versionIndex.add(version);
					_versionIndex.store(con);
				}
			} catch (org.dbwiki.exception.WikiException wikiException) {
				con.rollback();
				con.close();
				throw wikiException;
			}
			con.commit();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	/**
	 * Update modified timestamps associated with @node.
	 * 
	 * @return true if any timestamps were updated
	 */
	private boolean updateNodeTimestamps(Connection con, DatabaseNode node, Update update, Version version) throws org.dbwiki.exception.WikiException {
		boolean hasChanges = false;

		if (node.isElement()) {
			DatabaseElementNode element = (DatabaseElementNode)node;
			if (element.isAttribute()) {
				hasChanges = updateTextNodeTimestamps(con, ((DatabaseAttributeNode)element).value().getCurrent(), update.get(0), version);
			} else {
				Hashtable<String, DatabaseTextNode> valueIndex = new Hashtable<String, DatabaseTextNode>();
				getValueIndex((DatabaseGroupNode)element, valueIndex);
				for (int iUpdate = 0; iUpdate < update.size(); iUpdate++) {
					NodeUpdate upd = update.get(iUpdate);
					if (updateTextNodeTimestamps(con, valueIndex.get(upd.identifier().toParameterString()), upd, version)) {
						hasChanges = true;
					}
				}
			}
		} else {
			hasChanges = updateTextNodeTimestamps(con, (DatabaseTextNode)node, update.get(0), version);
		}
		return hasChanges;
	}

	/**
	 * Update modified timestamps associated with @node.
	 * 
	 * @return true if any timestamps were updated
	 */
	private boolean updateTextNodeTimestamps(Connection con, DatabaseTextNode node, NodeUpdate update, Version version) throws org.dbwiki.exception.WikiException {
		DatabaseAttributeNode attribute = ((DatabaseAttributeNode)node.parent());
		DatabaseNodeValue values = attribute.value();

		if (node.getTimestamp().isCurrent()) {
			if (!update.value().equals(node.text())) {
				updateTimestamp(con, node, node.getTimestamp().finishAt(version.number() - 1));
				for (int iValue = 0; iValue < values.size(); iValue++) {
					if (update.value().equals(values.get(iValue).text())) {
						DatabaseTextNode text = values.get(iValue);
						insertTimestamp(con, text, text.getTimestamp().continueAt(version.number()));
						return true;
					}
				}
				attribute.add(update.value(), new TimeSequence(version), node.getpre(),node.getpost());

				return true;
			}
		}
		return false;
	}
	
	private void updateTimestamp(Connection con, TimestampedObject obj, TimeSequence timestamp) throws org.dbwiki.exception.WikiException {
		TimeInterval interval = timestamp.lastInterval();
		
		ResourceIdentifier identifier = obj.identifier();

		if (obj.hasTimestamp() && !interval.isOpen()) {
			new DatabaseWriter(con, this).updateTimestamp(identifier, interval);
		} else {
			new DatabaseWriter(con, this).insertTimestamp(identifier, interval);
		}
		obj.setTimestamp(timestamp);
	}
	
	private void insertTimestamp(Connection con, DatabaseNode node, TimeSequence timestamp) throws org.dbwiki.exception.WikiException {
		TimeInterval interval = timestamp.lastInterval();

		new DatabaseWriter(con, this).insertTimestamp(node.identifier(), interval);

		node.setTimestamp(timestamp);
	}
	
	private void addSchemaIndexStatement(Vector<String> sqlStatements, Vector<String> parameters, AttributeCondition condition) {
		sqlStatements.add("SELECT DISTINCT " + RelDataColEntry + " " +
			"FROM " + this.name() + ViewSchemaIndex + " " +
			"WHERE " + ViewSchemaIndexColMaxCount + " >= " + condition.sqlPreparedStatement() + " " +
			"AND " + RelDataColSchema + " = " + condition.entity().id());
	}

	private void addSchemaValueStatement(Vector<String> sqlStatements, Vector<String> parameters, AttributeCondition condition) {
		
		sqlStatements.add("SELECT DISTINCT d1." + RelDataColEntry + " " +
			"FROM " + this.name() + RelationData + " d1, " + this.name() + RelationData + " d2 " +
			"WHERE d1." + RelDataColSchema + " = " + RelDataColSchemaValUnknown + " " +
			"AND d1." + RelDataColValue + " " + condition.sqlPreparedStatement() + " " +
			"AND d1." + RelDataColParent + " = d2." + RelDataColID + " " +
			"AND d2." + RelDataColSchema + " = " + condition.entity().id());
		condition.listValues(parameters);
	}

	@Override
	public ImportHandler createImportHandler(Connection con) {
		
		return new DatabaseImportHandler(con,this);
	}
}

