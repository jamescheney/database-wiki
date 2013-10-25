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

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.data.time.TimeSequence;

/** Implementation of DatabaseGroupNode for RDBMS.  
 * Main difference seems ot be presence of NodeIdentifier
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
	
	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation) {
		super(entity, parent, timestamp, annotation);
		
		_identifier = new NodeIdentifier(id);
	}

	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent, TimeSequence timestamp) {
		this(id, entity, parent, timestamp, new AnnotationList());
	}

	public RDBMSDatabaseGroupNode(int id, GroupEntity entity, DatabaseGroupNode parent) {
		this(id, entity, parent, null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public NodeIdentifier identifier() {
		return _identifier;
	}
}
