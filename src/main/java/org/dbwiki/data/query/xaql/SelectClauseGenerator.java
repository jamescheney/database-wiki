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

/** Generates a SELECT clause from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.query.xpath.VariableXPath;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;

public class SelectClauseGenerator {

	/*
	 * Public Methods
	 */
	
	public SelectClause getSelectClause(VersionIndex versionIndex, QueryVariableListing variables, XAQLToken token) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.SELECT_CLAUSE) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in getSelectClause()");
		}
		
		SelectClause selectClause = new SelectClause();
		
		XAQLToken selectToken = token.children().firstElement();
		switch (selectToken.type()) {
		case XAQLToken.SUBTREE_SELECT_CLAUSE:
			Vector<XAQLToken> subtreeSelectTokens = selectToken.children();
			int iOffset = 0;
	    	if (subtreeSelectTokens.firstElement().type() == XAQLToken.SUBTREE_SELECT_LIST) {
	    		this.getSubTreeSelectExpressions(selectClause, versionIndex, variables, subtreeSelectTokens.get(iOffset));
	    	} else {
	    		throw new WikiFatalException("Invalid token type for select clause: " + subtreeSelectTokens.firstElement().type());
	    	}
	    	return selectClause;
		default:
			throw new WikiFatalException("Invalid token type " + selectToken.type() + " as child in getSelectClause()");
		}
	}
	
	
	/*
	 * Private Variables
	 */
	
	private void getSubTreeSelectExpressions(SelectClause selectClause, VersionIndex versionIndex, QueryVariableListing variables, XAQLToken selectClauseTokens) throws org.dbwiki.exception.WikiException {
		
		for (int iTokenSet = 0; iTokenSet < selectClauseTokens.children().size(); iTokenSet++) {
			XAQLToken pathTokens =  selectClauseTokens.children().get(iTokenSet);
			selectClause.add(this.getVariableSelectStatement(pathTokens.children(), variables, versionIndex));
		}
	}
	
	public SubTreeSelectStatement getVariableSelectStatement(Vector<XAQLToken> statementTokens, QueryVariableListing variables, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		String variableName = statementTokens.firstElement().children().firstElement().value();
		QueryVariable variable = variables.get(variableName);
		if (variable == null) {
			throw new WikiFatalException("Unknown variable $" + variableName);
		}
		VariableXPath targetPath = new VariableTargetPathGenerator().getTargetPath(variable.targetEntity(), versionIndex, statementTokens.firstElement().children().iterator());
		
		String label = null;
		if (statementTokens.size() > 1) {
			label = statementTokens.get(1).value();
		}
		return new SubTreeSelectStatement(targetPath, label);
	}
}
