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

/** A XAQLQueryStatement consisting of a reference to the database and the XAQLQuery definition.
 * Represents query statements for XAQL queries.
 * 
 * @author hmueller
 *
 */

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.query.handler.QueryNodeHandler;
import org.dbwiki.data.query.xaql.QueryStatementGenerator;
import org.dbwiki.data.query.xaql.XAQLQuery;
import org.dbwiki.data.query.xaql.XAQLSyntaxParser;
import org.dbwiki.data.query.xaql.XAQLToken;
import org.dbwiki.data.query.xpath.AbsoluteXPathConsumer;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.data.WikiQueryException;

import org.parboiled.Parboiled;
import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class XAQLQueryStatement extends QueryStatement {

	/*
	 * Private Variables
	 */
	
	private Database _database;
	private XAQLQuery _query;
	
	
	/*
	 * Constructors
	 */
	
	public XAQLQueryStatement(Database database, String queryExpression) throws org.dbwiki.exception.WikiException {
	
		_database = database;
		
		XAQLSyntaxParser parser = Parboiled.createParser(XAQLSyntaxParser.class);
		
		ParsingResult<XAQLToken> result = new ReportingParseRunner<XAQLToken>(parser.QueryStatement()).run(new DefaultInputBuffer(queryExpression.toCharArray()));

		if (result.hasErrors()) {
			throw new WikiQueryException(WikiQueryException.InvalidWikiQuery, queryExpression + "\n" + ErrorUtils.printParseErrors(result));
        } else {
    		//result.parseTreeRoot.getValue().print("", "\t");
        	_query = new QueryStatementGenerator().getStatement(database, result.parseTreeRoot.getValue());
        }
	}

	/**
	 * Evaluates the query against the database and returns a QueryResultSet
	 * 
	 * @throws org.dbwiki.exception.WikiException
	 */
	public QueryResultSet execute() throws WikiException {

		QueryResultSet result = null;
		if (_query.getVersionClause() != null) {
			result = new QueryResultSet(_query.getVersionClause().getTimestamp());
		} else {
			result = new QueryResultSet();
		}
		
		QueryNodeHandler queryHandler = _query.getQueryHandler(result);
		
		// Collect all the entries that potentially match the query condition. For each of the
		// candidate entries we then evaluate the query statement and add the results to the
		// QueryResultSet.
		DatabaseContent content = _database.getMatchingEntries(_query.getConditionListing());
		for (int iEntry = 0; iEntry < content.size(); iEntry++) {
			DatabaseGroupNode entry = (DatabaseGroupNode)_database.get(content.get(iEntry).identifier());
			if (_query.getVersionClause() != null) {
				entry = this.versionReader(entry, _query.getVersionClause().getTimestamp());
				if (entry != null) {
					new AbsoluteXPathConsumer().consume(entry, _query.rootTargetPath(), queryHandler);
				}
			} else { // This is dead code because version clause defaults to NOW.  
				// Could handle "interpret missing version as NOW" here. 
				new AbsoluteXPathConsumer().consume(entry, _query.rootTargetPath(), queryHandler);
			}
		}
		return result;
	}

	/*
	 * Private Methods
	 */
	
	private void filterChildrenByTimestamp(DatabaseAttributeNode node, TimeSequence timestamp) {
		
		int iChild = 0;
		while (iChild < node.value().size()) {
			DatabaseTextNode child = node.value().get(iChild);
			if (child.hasTimestamp()) {
				if (!child.getTimestamp().intersect(timestamp).isEmpty()) {
					iChild++;
				} else {
					node.value().remove(iChild);
				}
			} else {
				iChild++;
			}
		}
	}
	
	private void filterChildrenByTimestamp(DatabaseGroupNode node, TimeSequence timestamp) {
		
		int iChild = 0;
		while (iChild < node.children().size()) {
			DatabaseElementNode child = node.children().get(iChild);
			if (!child.getTimestamp().intersect(timestamp).isEmpty()) {
				if (child.isAttribute()) {
					this.filterChildrenByTimestamp((DatabaseAttributeNode)child, timestamp);
				} else {
					this.filterChildrenByTimestamp((DatabaseGroupNode)child, timestamp);
				}
				iChild++;
			} else {
				node.children().remove(iChild);
			}
		}
	}
	
	private DatabaseGroupNode versionReader(DatabaseGroupNode entry, TimeSequence timestamp) {
		
		if (!entry.getTimestamp().intersect(timestamp).isEmpty()) {
			this.filterChildrenByTimestamp(entry, timestamp);
			return entry;
		} else {
			return null;
		}
	}
}
