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
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.time.TimeSequence;

/** Implementation of DatabaseTextNode
 * Main difference is addition of NodeIdentifier.
 * @author jcheney
 *
 */
public class RDBMSDatabaseTextNode extends DatabaseTextNode {
	/*
	 * Private Variables
	 */
	
	private NodeIdentifier _identifier;
	
	
	/*
	 * Constructors
	 */

	public RDBMSDatabaseTextNode(int id, DatabaseAttributeNode parent, TimeSequence timestamp, String value, AnnotationList annotation, /*int pre, int post,*/ String dewey) {
		super(parent, timestamp, value, annotation, /*pre, post,*/ dewey);
		
		_identifier = new NodeIdentifier(id);
	}

	public RDBMSDatabaseTextNode(int id, DatabaseAttributeNode parent, TimeSequence timestamp, String value, /*int pre, int post,*/ String dewey) {
		this(id, parent, timestamp, value, new AnnotationList(), /*pre, post,*/ dewey);
	}

	public RDBMSDatabaseTextNode(int id, DatabaseAttributeNode parent, String value) {
		this(id, parent, null, value, /*-1, -1,*/ "");
	}
	
	
	/*
	 * Public Methods
	 */
	
	public NodeIdentifier identifier() {
		return _identifier;
	}
}
