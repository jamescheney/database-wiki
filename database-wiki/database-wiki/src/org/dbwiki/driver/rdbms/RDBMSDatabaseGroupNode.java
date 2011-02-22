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

import org.dbwiki.data.database.DatabaseGroupNode;

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.data.time.Timestamp;

public class RDBMSDatabaseGroupNode extends DatabaseGroupNode {
	/*
	 * Private Variables
	 */
	
	private NID _identifier;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent, Timestamp timestamp, AnnotationList annotation) {
		super(entity, parent, timestamp, annotation);
		
		_identifier = new NID(id);
	}

	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent, Timestamp timestamp) {
		this(id, entity, parent, timestamp, new AnnotationList());
	}

	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent) {
		this(id, entity, parent, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public NID identifier() {
		return _identifier;
	}
}
