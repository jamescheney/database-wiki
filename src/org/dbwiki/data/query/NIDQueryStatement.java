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

import org.dbwiki.data.database.Database;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.exception.data.WikiQueryException;

/** A QueryStatement that fetches the data associated with a node id.
 * FIXME #query: Make this more modular
 * @author jcheney
 *
 */
public class NIDQueryStatement extends QueryStatement {
	/*
	 * Private Variables
	 */
	
	private Database _database;
	private int _nodeID;
	
	
	/*
	 * Constructors
	 */
	
	public NIDQueryStatement(Database database, String nodeHexID) throws org.dbwiki.exception.WikiException {
		//
		// Expects a node identifier in hexadecimal integer format
		//
		_database = database;
		try {
			_nodeID = Integer.decode("0x" + nodeHexID);
		} catch (java.lang.NumberFormatException exception) {
			throw new WikiQueryException(WikiQueryException.InvalidNIDQuery, nodeHexID);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public QueryResultSet execute() throws org.dbwiki.exception.WikiException {
		return new QueryResultSet(_database.get(new NodeIdentifier(_nodeID)));
	}
}
