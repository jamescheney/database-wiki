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

/** Generates a WHERE clause from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;

public class WhereClauseGenerator {

	/*
	 * Public Methods
	 */
	
	public WhereClause getWhereClause(QueryVariableListing variables, XAQLToken token, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.WHERE_CLAUSE) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in getWhereClause()");
		}
		
		WhereClause whereClause = new WhereClause();
		for (int iExpression = 0; iExpression < token.children().size(); iExpression++) {
			whereClause.add(this.getWhereExpression(variables, token.children().get(iExpression), versionIndex));
		}
		return whereClause;
	}
	
	public WhereCondition getWhereCondition(QueryVariableListing variables, XAQLToken token, VersionIndex versionIndex)  throws org.dbwiki.exception.WikiException {
		
		if ((token.type() != XAQLToken.WHERE_CLAUSE_EXPRESSION) && (token.type() != XAQLToken.COINCIDES_LIST_ELEMENT)) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in getWhereCondition()");
		}

		QueryVariable variable = null;
		
		for (XAQLToken childToken : token.children()) {
			if (childToken.type() == XAQLToken.VARIABLE_TARGET_PATH) {
				variable = variables.get(childToken.children().firstElement().value());
				break;
			}
		}
		return new WhereCondition(variable.name(), new ConditionGenerator().getCondition(variable.targetEntity(), versionIndex, token.children(), new VariableTargetPathGenerator()));
	}
	
	
	/*
	 * Private Methods
	 */
	
	private WhereExpression getWhereExpression(QueryVariableListing variables, XAQLToken token, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.WHERE_CLAUSE_EXPRESSION) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in getWhereExpression()");
		}
		
		int offset = 0;
		if (token.children().firstElement().type() == XAQLToken.NOT_OPERATOR) {
			offset++;
		}
		
		if (token.children().get(offset).type() == XAQLToken.COINCIDES_EXPRESSION) {
			return new CoincidesExpressionGenerator().getCoincidesExpression(variables, token, versionIndex);
		} else {
			return this.getWhereCondition(variables, token, versionIndex);
		}
	}
}
