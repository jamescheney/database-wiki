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
package org.dbwiki.data.resource;

/** A WRI is a struct containing a database id and resource id.
 * The getURL method concatenates the database id's prefix and the resource id's suffix.
 * 
 * @author jcheney
 *
 */

public class WRI {
	/*
	 * Private Variables
	 */
	
	private DatabaseIdentifier _di;
	private ResourceIdentifier _ri;
	
	
	/*
	 * Constructors
	 */
	
	public WRI(DatabaseIdentifier di, ResourceIdentifier ri) {
		_di = di;
		_ri = ri;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public DatabaseIdentifier databaseIdentifier() {
		return _di;
	}
	
	public String getURL() {
		return _di.linkPrefix() + _ri.toURLString();
	}
	
	public ResourceIdentifier resourceIdentifier() {
		return _ri;
	}
}
