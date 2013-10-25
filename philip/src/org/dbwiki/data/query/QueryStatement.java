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

import java.util.logging.Logger;

import org.dbwiki.data.database.Database;
import org.dbwiki.exception.data.WikiQueryException;

/** An interface for QueryStatements
 * These are either nodeid queries or path queries
 * Methods allow distinguishing the two cases.
 * FIXME #query: Provide query-related behaviors in appropriate subclasses.
 * 
 * @author jcheney
 *
 */
public abstract class QueryStatement {
	/*
	 * Public Constants
	 */
	
	//
	// N(ode) ID expression, i.e., nid://<<node-id>>
	//
	// <<node-id>>: hexadecimal integer value
	//
	public static final String QueryNID   = "nid";
	
	//
	// XPATH-like expression (called Wiki-Path), i.e., wpath://<<schema-node-name>>{[<<child-path>>='...'] | :<<node-index>>}/...
	//
	public static final String QueryWikiPath = "wpath";
	
	
	/*
	 * Interface Methods
	 */
	
	public abstract QueryResultSet execute() throws org.dbwiki.exception.WikiException;
	
	public static QueryStatement createStatement(Database database, String query) throws org.dbwiki.exception.WikiException {
		if (query != null) {
			if (query.toLowerCase().startsWith(QueryStatement.QueryNID.toLowerCase() + "://")) {
				return new NIDQueryStatement(database, query.substring(QueryStatement.QueryNID.length() + 3));
			} else if (query.toLowerCase().startsWith(QueryStatement.QueryWikiPath.toLowerCase() + "://")) {
				return new WikiPathQueryStatement(database, query.substring(QueryStatement.QueryWikiPath.length() + 2));
			} else if (query.toLowerCase().startsWith("select ")) {
				return new XAQLQueryStatement(database, query);
			} else {
				throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, query);
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidQueryFormat, "(null)");
		}
	}
	
	/* NEW FOR TESTING --DELETE and also remove import logger */	
	public static QueryStatement createStatement(Database database, String query, Logger log) throws org.dbwiki.exception.WikiException {
		if (query != null) {
			if (query.toLowerCase().startsWith(QueryStatement.QueryNID.toLowerCase() + "://")) {
				log.info("NID query executing");
				return new NIDQueryStatement(database, query.substring(QueryStatement.QueryNID.length() + 3));
			} else if (query.toLowerCase().startsWith(QueryStatement.QueryWikiPath.toLowerCase() + "://")) {
				log.info("XPath query executing");
				log.info("the query wiki path: " + query.substring(QueryStatement.QueryWikiPath.length() + 2));
				return new WikiPathQueryStatement(database, query.substring(QueryStatement.QueryWikiPath.length() + 2),log);
			} else if (query.toLowerCase().startsWith("select ")) {
				log.info("XAQL query executing");
				return new XAQLQueryStatement(database, query,log); 
			} else {
				log.info("Unknown query format exception");
				throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, query);
			}
		} else {
			log.info("Invalid query format exception");
			throw new WikiQueryException(WikiQueryException.InvalidQueryFormat, "(null)");
		}
	}
}
