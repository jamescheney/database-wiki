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

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.schema.AttributeEntity;

import org.dbwiki.data.time.Timestamp;

public class RDBMSDatabaseAttributeNode extends DatabaseAttributeNode {
	/*
	 * Private Variables
	 */
	
	private NID _identifier;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseAttributeNode(int id, AttributeEntity entity, DatabaseGroupNode parent, Timestamp timestamp, AnnotationList annotation) {
		super(entity, parent, timestamp, annotation);
		
		_identifier = new NID(id);
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeEntity entity, DatabaseGroupNode parent, Timestamp timestamp) {
		this(id, entity, parent, timestamp, new AnnotationList());
	}
	
	public RDBMSDatabaseAttributeNode(int id, AttributeEntity entity, DatabaseGroupNode parent, Timestamp timestamp, String value, AnnotationList annotation) {
		this(id, entity, parent, timestamp, annotation);
		
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, null, value));
	}

	public RDBMSDatabaseAttributeNode(int id, AttributeEntity entity, DatabaseGroupNode parent) {
		this(id, entity, parent, null, new AnnotationList());
	}
	
		
	/*
	 * Public Methods
	 */
	
	public void add(String value, Timestamp timestamp) {
		this.value().add(new RDBMSDatabaseTextNode(DatabaseConstants.RelDataColIDValUnknown, this, timestamp, value));
	}
	
	public NID identifier() {
		return _identifier;
	}
}
