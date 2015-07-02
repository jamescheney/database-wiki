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

/** Generates COINCIDES expression from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;

public class CoincidesExpressionGenerator {

	/*
	 * Public Methods
	 */
	
	public CoincidesExpression getCoincidesExpression(QueryVariableListing variables, XAQLToken token, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		XAQLToken expressionList = null;
		
		boolean negated = false;
		if (token.type() == XAQLToken.WHERE_CLAUSE_EXPRESSION) {
			negated =  (token.children().firstElement().type() == XAQLToken.NOT_OPERATOR);
			if (negated) {
				expressionList = token.children().get(1);
			} else {
				expressionList = token.children().firstElement();
			}
		} else if (token.type() == XAQLToken.COINCIDES_EXPRESSION) {
			expressionList = token;
		} else {
			throw new WikiFatalException("Invalid token type " + token.type() + " in getCoincidesExpression()");
		}
		
		if (expressionList.type() != XAQLToken.COINCIDES_EXPRESSION) {
			throw new WikiFatalException("Invalid token type " + expressionList.type());
		}
		
		CoincidesExpression coincidesExpression = new CoincidesExpression(negated);
		for (int iExpression = 0; iExpression < expressionList.children().size(); iExpression++) {
			coincidesExpression.add(new WhereClauseGenerator().getWhereCondition(variables, expressionList.children().get(iExpression), versionIndex));
		}
		return coincidesExpression;
	}
	
}
