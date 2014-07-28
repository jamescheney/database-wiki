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

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** Implementation of DatabaseAttributeNode for RDBMSDatabase. 
 * The main difference seems to be the addition of NodeIdentifier.
 * Maybe we can simplify this.
 * @author jcheney
 *
 */
public class RDBMSDatabaseAttributeNode extends DatabaseAttributeNode {
	/*
	 * Private Variables
	 */
	
	private NodeIdentifier _identifier;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(schema, parent, timestamp, annotation, pre, post);
		
		_identifier = new NodeIdentifier(id);
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, int pre, int post) {
		this(id, schema, parent, timestamp, new AnnotationList(), pre, post);
	}
	
	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, String value, AnnotationList annotation, int pre, int post) {
		this(id, schema, parent, timestamp, annotation, pre, post);
		
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, null, value, pre, post));
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, int pre, int post) {
		this(id, schema, parent, null, new AnnotationList(), pre, post);
	}
	
	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent) {
		this(id, schema, parent, null, new AnnotationList(), -1, -1);
	}
		
	/*
	 * Public Methods
	 */
	
	@Override
	public void add(String value, TimeSequence timestamp, int pre, int post) {
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, timestamp, value, pre, post));
	}
	
	@Override
	public NodeIdentifier identifier() {
		return _identifier;
	}
	
	@Override
	public String toString(){
		return "";
	}
	
	public DocumentNode toDocumentNode(){
		DocumentAttributeNode node = new DocumentAttributeNode((AttributeSchemaNode)this.schema(), this.value().getMostRecent().getValue());
		return node;
	}
}
