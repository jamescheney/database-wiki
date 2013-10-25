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

import java.util.Vector;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.query.xaql.AbsoluteTargetPathGenerator;
import org.dbwiki.data.query.xaql.XAQLSyntaxParser;
import org.dbwiki.data.query.xaql.XAQLToken;
import org.dbwiki.data.query.xpath.AbsoluteXPathConsumer;
import org.dbwiki.data.query.xpath.XPath;

import org.dbwiki.exception.data.WikiQueryException;
import org.parboiled.Parboiled;
import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A QueryStatement consisting of a vector of path component steps.
 * FIXME #query: Delocalize these queries
 * @author jcheney
 *
 */
public class WikiPathQueryStatement extends QueryStatement {
	/*
	 * Private Variables
	 */
	
	private Database _database;
	private XPath _targetPath;
	
	
	/*
	 * Constructors
	 */
	/** Creates a wiki path query statement for a given database from a path expression
	 */
	public WikiPathQueryStatement(Database database, String pathExpression) throws org.dbwiki.exception.WikiException {
		//
		// Expects a XPath-like expression, i.e., /schema-node{[child='...'] | :<<node-index>>}/schema-node{[child='...'] | :<<node-index>>}/...
		// The [child='...'] | :<<node-index>>-part is optional.
		// Only one of the options [child='...'] OR :<<node-index>> are allowed.
		//
		
		_database = database;
		// Initialising parser
		XAQLSyntaxParser parser = Parboiled.createParser(XAQLSyntaxParser.class);
		//parsing result: it returns XAQL tokens as nodes of the path
		ParsingResult<XAQLToken> result = new ReportingParseRunner<XAQLToken>(parser.XPathStatement()).run(new DefaultInputBuffer(pathExpression.toCharArray()));
		
		if (result.hasErrors()) {
			throw new WikiQueryException(WikiQueryException.InvalidWikiQuery, pathExpression + "\n" + ErrorUtils.printParseErrors(result));
        } else {	//
        	_targetPath = new AbsoluteTargetPathGenerator().getTargetPath(database.schema().root(), database.versionIndex(), result.parseTreeRoot.getValue().children().firstElement().children().iterator());
        }
	}
	
	
	/*
	 * Public Methods
	 */
	
	public QueryResultSet execute() throws org.dbwiki.exception.WikiException {


		QueryResultSet result = new QueryResultSet();
		DatabaseContent content = _database.getMatchingEntries(_targetPath.getConditionListing());
		for (int iEntry = 0; iEntry < content.size(); iEntry++) {
			DatabaseGroupNode entry = (DatabaseGroupNode)_database.get(content.get(iEntry).identifier());
			new AbsoluteXPathConsumer().consume(entry, _targetPath, result);

		}

		return result;

	}

}
