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
import org.dbwiki.data.schema.AttributeSchemaNode;

import org.dbwiki.data.time.TimeSequence;

/** An attribute node with a value (which can be a sequence of timestamped text nodes.
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseAttributeNode extends DatabaseElementNode {
	/*
	 * Private Variables
	 */
	
	private DatabaseNodeValue _value;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseAttributeNode(AttributeSchemaNode schema, DatabaseGroupNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(schema, parent, timestamp, annotation, pre, post);
		
		_value = new DatabaseNodeValue();
	}

	
	/*
	 * Abstract Methods
	 */
	
	public abstract void add(String value, TimeSequence timestamp, int pre, int post);
	
	
	
	/*
	 * Public Methods
	 */
	
	public DatabaseNodeValue value() {
		return _value;
	}
	
	@Override
	public String toString() {
		return (label() + "/" + _value.getCurrent().toString());
	}
	
	@Override
	public DatabaseNode find(ResourceIdentifier identifier) {
		for (int iValue = 0; iValue < _value.size(); iValue++) {
			DatabaseTextNode text = _value.get(iValue);
			if (text.identifier().equals(identifier)) {
				return text;
			}
		}
		return null;
	}
}
