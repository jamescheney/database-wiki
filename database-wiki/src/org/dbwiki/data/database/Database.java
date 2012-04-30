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
package org.dbwiki.data.database;

import org.dbwiki.data.annotation.Annotation;

import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.document.PasteNode;

import org.dbwiki.data.index.DatabaseContent;

import org.dbwiki.data.io.NodeWriter;

import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.VersionIndex;

import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.query.condition.AttributeConditionListing;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.request.RequestURL;

/** Interface wrapping up all of the operations we can perform on the database.
 * 
 * @author jcheney
 *
 */
public interface Database {
	/*
	 * Public Constants
	 */
	
	// SchemaNode types
	public static final byte SchemaNodeTypeAttribute = 0;
	public static final byte SchemaNodeTypeGroup     = 1;
	
	
	/*
	 * Interface Methods
	 */
	
	
	/* General stuff */
	/** Identifier of this database
	 * @return The database id
	 * @throws org.dbwiki.exception.WikiException
	 */
	public DatabaseIdentifier identifier();
	
	/**
	 * @return Name of the database
	 */
	public String name();
	/**
	 * @return List of entries of the database.
	 * @throws org.dbwiki.exception.WikiException
	 */
	public DatabaseContent content() throws org.dbwiki.exception.WikiException;

	public UserListing users();
	public VersionIndex versionIndex();
	
	/* Queries
	 */
	public DatabaseNode get(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException;
	public DatabaseContent getMatchingEntries(AttributeConditionListing listing) throws org.dbwiki.exception.WikiException;
	public ResourceIdentifier getIdentifierForParameterString(String urlString) throws org.dbwiki.exception.WikiException;
	public ResourceIdentifier getNodeIdentifierForURL(RequestURL url) throws org.dbwiki.exception.WikiException;
	
	public QueryResultSet query(String query) throws org.dbwiki.exception.WikiException;
	public DatabaseContent search(String keywords) throws org.dbwiki.exception.WikiException;
	
	/* Updates
	 */
	/** Insert node /subtree into database
	 * 
	 * @param identifier Node into which to insert
	 * @param node Subtree to insert
	 * @param user User performing the action
	 * @return Identifier of inserted node
	 * @throws org.dbwiki.exception.WikiException
	 */
	public ResourceIdentifier insertNode(ResourceIdentifier identifier, DocumentNode node, User user) throws org.dbwiki.exception.WikiException;
	public void update(ResourceIdentifier identifier, Update update, User user) throws org.dbwiki.exception.WikiException;
	public void delete(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException;
	public void activate(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException;
	public void annotate(ResourceIdentifier identifier, Annotation annotation) throws org.dbwiki.exception.WikiException;
	public void paste(ResourceIdentifier target, PasteNode pasteNode, String sourceURL, User user) throws org.dbwiki.exception.WikiException;
	
	/* Import/export */
	public void export(ResourceIdentifier identifier, int version, NodeWriter out) throws org.dbwiki.exception.WikiException;

	
	/* Schema operations */
	public DatabaseSchema schema();
	public SchemaNode getSchemaNode(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException;
	public ResourceIdentifier getSchemaNodeIdentifierForURL(RequestURL url) throws org.dbwiki.exception.WikiException;

	public void deleteSchemaNode(ResourceIdentifier identifier, User user) throws org.dbwiki.exception.WikiException;
	public void insertSchemaNode(GroupSchemaNode parent, String name, byte type, User user) throws org.dbwiki.exception.WikiException;

}
