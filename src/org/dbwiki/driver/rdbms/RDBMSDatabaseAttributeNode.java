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
	
	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation) {
		super(schema, parent, timestamp, annotation);
		
		_identifier = new NodeIdentifier(id);
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp) {
		this(id, schema, parent, timestamp, new AnnotationList());
	}
	
	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, String value, AnnotationList annotation) {
		this(id, schema, parent, timestamp, annotation);
		
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, null, value));
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeSchemaNode schema, DatabaseGroupNode parent) {
		this(id, schema, parent, null, new AnnotationList());
	}
	
		
	/*
	 * Public Methods
	 */
	
	public void add(String value, TimeSequence timestamp) {
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, timestamp, value));
	}
	
	public NodeIdentifier identifier() {
		return _identifier;
	}
}
