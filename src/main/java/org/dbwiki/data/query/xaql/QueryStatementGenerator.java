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
package org.dbwiki.data.query.xaql;

/** Generates a XAQL query statement from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.database.Database;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.WikiFatalException;

public class QueryStatementGenerator {

	/*
	 * Public Methods
	 */
	
	public XAQLQuery getStatement(Database database, XAQLToken token) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.QUERY_STATEMENT) {
			throw new WikiFatalException("Invalid token type " + token.type());
		}

    	FromClause fromClause = new FromClauseGenerator().getFromClause(database.schema().root(), database.versionIndex(), token.children().get(1));
    	
    	SelectClause selectClause = new SelectClauseGenerator().getSelectClause(database.versionIndex(), fromClause.variables(), token.children().get(0));
    	
    	VersionClause versionClause = null;
    	WhereClause whereClause = null;
    	
    	for (int iNodeSet = 2; iNodeSet < token.children().size(); iNodeSet++) {
    		XAQLToken nodeSet = token.children().get(iNodeSet);
    		if (nodeSet.type() == XAQLToken.VERSION_CLAUSE) {
    			versionClause = new VersionClauseGenerator().getVersionClause(database.versionIndex(), nodeSet);
    		} else if (nodeSet.type() == XAQLToken.WHERE_CLAUSE) {
    			whereClause = new WhereClauseGenerator().getWhereClause(fromClause.variables(), nodeSet, database.versionIndex());
    		}
    	}
    	
    	// If there is no version clause, set it to NOW
    	if (versionClause == null) {
    		versionClause = new VersionClause(new TimeSequence(Integer.MAX_VALUE));
    	}
    	
    	return new XAQLQuery(selectClause, fromClause, versionClause, whereClause);
	}	
}
