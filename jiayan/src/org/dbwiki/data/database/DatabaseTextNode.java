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

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.time.TimeSequence;

/** A subclass of DatabaseNode for text nodes.
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseTextNode extends DatabaseNode {
	/*
	 * Private Variables
	 */
	
	private String _value = null;
	private DatabaseAttributeNode _parent;

	
	/*
	 * Constructors
	 */
	
	public DatabaseTextNode(DatabaseAttributeNode parent, TimeSequence timestamp, String value, AnnotationList annotation) {
		super(parent, timestamp, annotation);

		_value = value;
		_parent = parent;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String getValue() {
		return this.value();
	}
	
	public boolean isElement() {
		return false;
	}
	
	public void setValue(String value) {
		_value = value;
	}
	
	public String text() {
		return this.value();
	}
	
	public String value() {
		return _value;
	}
	
	public String toString() {
		return value();
	}
	
	public DatabaseNode find(ResourceIdentifier identifier) {
		return null;
	}
	
	public DatabaseAttributeNode parent(){
		return _parent;
	}
	

}
