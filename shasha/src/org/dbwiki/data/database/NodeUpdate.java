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

import org.dbwiki.data.resource.ResourceIdentifier;


/**
 *  An atomic update that replaces the text value of a node with a new value.
 * @author jcheney
 * 
 */
public class NodeUpdate {
	/*
	 * Private Variables
	 */
	
	private ResourceIdentifier _identifier;
	private String _value;
	
	
	/*
	 * Constructors
	 */
	
	public NodeUpdate(ResourceIdentifier identifier, String value) {
		_identifier = identifier;
		_value = value;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public ResourceIdentifier identifier() {
		return _identifier;
	}
	
	public String value() {
		return _value;
	}
}
