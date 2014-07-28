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

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** Implementation of DatabaseGroupNode for RDBMS.  
 * Main difference seems to be presence of NodeIdentifier
 * 
 * @author jcheney
 *
 */

public class RDBMSDatabaseGroupNode extends DatabaseGroupNode {
	/*
	 * Private Variables
	 */
	
	private NodeIdentifier _identifier;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseGroupNode(int id, GroupSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(schema, parent, timestamp, annotation, pre, post);
		
		_identifier = new NodeIdentifier(id);
	}

	public RDBMSDatabaseGroupNode(int id, GroupSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, int pre, int post) {
		this(id, schema, parent, timestamp, new AnnotationList(), pre, post);
	}

	public RDBMSDatabaseGroupNode(int id, GroupSchemaNode schema, DatabaseGroupNode parent) {
		this(id, schema, parent, null, -1, -1);
	}
	
	public RDBMSDatabaseGroupNode(int id, GroupSchemaNode schema, DatabaseGroupNode parent, int pre, int post) {
		this(id, schema, parent, null, pre, post);
	}
	
	/*
	 * Public Methods
	 */
	
	@Override
	public NodeIdentifier identifier() {
		return _identifier;
	}
	
	public DocumentNode toDocumentNode(){
		DocumentGroupNode node = new DocumentGroupNode((GroupSchemaNode)this.schema());
		for(int i = 0; i < this.children().size(); i++){
			node.children().add(((DatabaseElementNode)this.children().get(i)).toDocumentNode());
		}
		return node;
	}
}
