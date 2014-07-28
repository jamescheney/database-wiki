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

/** XAQL query definition.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.query.condition.AttributeConditionListing;
import org.dbwiki.data.query.handler.MultiVariableQueryNodeHandler;
import org.dbwiki.data.query.handler.QueryNodeHandler;
import org.dbwiki.data.query.handler.SingleVariableQueryNodeHandler;
import org.dbwiki.data.query.xpath.XPath;

public class XAQLQuery {

	/*
	 * Public Constants
	 */
	
	public static final String DefaultResultLabel = "result";

	
	/*
	 * Private Variables
	 */
	
	private FromClause _fromClause;
	private SelectClause _selectClause;
	private VersionClause _versionClause;
	private WhereClause _whereClause;
	
	
	/*
	 * Constructors
	 */
	
	public XAQLQuery(SelectClause selectClause, FromClause fromClause, VersionClause versionClause, WhereClause whereClause) {
		
		_selectClause = selectClause;
		_fromClause = fromClause;
		_versionClause = versionClause;
		_whereClause = whereClause;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public AttributeConditionListing getConditionListing() {
		
		AttributeConditionListing listing = new AttributeConditionListing();
		_fromClause.listConditions(listing);
		if (_whereClause != null) {
			_whereClause.listConditions(listing);
		}
		return listing;
	}
	
	public QueryNodeHandler getQueryHandler(QueryNodeHandler consumer) {
		
		if (_fromClause.variables().size() > 1) {
			return new MultiVariableQueryNodeHandler(_selectClause, _whereClause, _fromClause.rootVariable(), _fromClause.variables(), consumer);
		} else {
			return new SingleVariableQueryNodeHandler(_selectClause, _whereClause, _fromClause.rootVariable(), consumer);
		}
	}
	
	public VersionClause getVersionClause() {
		
		return _versionClause;
	}
	
	public XPath rootTargetPath() {
		
		return _fromClause.rootVariable().targetPath();
	}
}
