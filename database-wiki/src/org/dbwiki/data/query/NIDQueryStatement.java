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
package org.dbwiki.data.query;

import org.dbwiki.exception.data.WikiQueryException;

public class NIDQueryStatement implements QueryStatement {
	/*
	 * Private Variables
	 */
	
	private int _nodeID;
	
	
	/*
	 * Constructors
	 */
	
	public NIDQueryStatement(String nodeHexID) throws org.dbwiki.exception.WikiException {
		//
		// Expects a node identifier in hexadecimal integer format
		//
		try {
			_nodeID = Integer.decode("0x" + nodeHexID);
		} catch (java.lang.NumberFormatException exception) {
			throw new WikiQueryException(WikiQueryException.InvalidNIDQuery, nodeHexID);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean isNIDStatement() {
		return true;
	}

	public boolean isWikiPathStatement() {
		return false;
	}
	
	public int nodeID() {
		return _nodeID;
	}
}
