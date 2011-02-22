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
import org.dbwiki.exception.data.WikiQueryException;

public class QueryStatementFactory {
	/*
	 * Private Variables
	 */
	
	private Database _database;
	
	
	/*
	 * Constructors
	 */
	
	public QueryStatementFactory(Database database) {
		_database = database;
	}
	
	
	/*
	 * Public Methods
	 */
	
	//
	// Returns a QueryStatement for th given query string,
	// which may be one of the following:
	//
	// * nid://<<node-id>>
	// * wpath://<<entity-name>>{[<<child>>='...'] | :<<node-index>>}/...
	//
	public QueryStatement createStatement(String query) throws org.dbwiki.exception.WikiException {
		if (query != null) {
			if (query.toLowerCase().startsWith(QueryStatement.QueryNID.toLowerCase() + "://")) {
				return new NIDQueryStatement(query.substring(QueryStatement.QueryNID.length() + 3));
			} else if (query.toLowerCase().startsWith(QueryStatement.QueryWikiPath.toLowerCase() + "://")) {
				return new WikiPathQueryStatement(_database, query.substring(QueryStatement.QueryWikiPath.length() + 2));
			} else {
				throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, query);
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidQueryFormat, "(null)");
		}
	}
}
