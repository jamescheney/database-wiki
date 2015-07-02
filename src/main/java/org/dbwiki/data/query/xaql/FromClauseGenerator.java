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

/** Generates a FROM clause from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.query.xpath.VariableXPath;
import org.dbwiki.data.query.xpath.XPath;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;

public class FromClauseGenerator {

	/*
	 * Public Methods
	 */
	
	public FromClause getFromClause(SchemaNode schemaRoot, VersionIndex versionIndex, XAQLToken token) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.FROM_CLAUSE) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in FromClauseGenerator.getFromClause()");
		}

		Vector<XAQLToken> fromClauseTokens = token.children();
		int iToken = 0;
		
    	String variableName = fromClauseTokens.get(iToken++).value();
    	XPath archiveTargetPath = null;
    	if (fromClauseTokens.size() > iToken) {
    		if (fromClauseTokens.get(iToken).type() == XAQLToken.RELATIVE_TARGET_PATH) {
    			archiveTargetPath = new AbsoluteTargetPathGenerator().getTargetPath(schemaRoot, versionIndex, fromClauseTokens.get(iToken++).children().iterator());
    		} else {
        		archiveTargetPath = new AbsoluteTargetPathGenerator().getTargetPath(schemaRoot, versionIndex);
    		}
    	} else {
    		archiveTargetPath = new AbsoluteTargetPathGenerator().getTargetPath(schemaRoot, versionIndex);
    	}
    	QueryVariable rootVariable = new QueryVariable(variableName, archiveTargetPath);
    	QueryVariableListing variableListing = new QueryVariableListing(rootVariable);
    	
    	while (iToken < fromClauseTokens.size()) {
    		variableName = fromClauseTokens.get(iToken++).value();
    		QueryVariable parent = variableListing.get(fromClauseTokens.get(iToken).children().firstElement().value());
    		XPath targetPath = new VariableTargetPathGenerator().getTargetPath(parent.targetEntity(), versionIndex, fromClauseTokens.get(iToken++).children().iterator()); //should order matter??YES
    		QueryVariable variable = new QueryVariable(variableName, targetPath);
    		parent.children().add(variable);
    		variableListing.add(variable);
    	}
    	return new FromClause(rootVariable, variableListing);
	}
	

	public FromClause getSubqueryFromClause(QueryVariableListing variables, XAQLToken token, VersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.SUBQUERY_FROM_CLAUSE) {
			throw new WikiFatalException("Invalid token type " + token.type() + " in FromClauseGenerator.getSubqueryFromClause()");
		}

		Vector<XAQLToken> fromClauseTokens = token.children();
		int iToken = 0;
		
    	String variableName = fromClauseTokens.get(iToken++).value();
		QueryVariable rootParent = variables.get(fromClauseTokens.get(iToken).children().firstElement().value());
		VariableXPath variableTargetPath = new VariableTargetPathGenerator().getTargetPath(rootParent.targetEntity(), versionIndex, fromClauseTokens.get(iToken++).children().iterator());

    	QueryVariable rootVariable = new QueryVariable(variableName, variableTargetPath);
    	QueryVariableListing variableListing = new QueryVariableListing(rootVariable);
    	
    	while (iToken < fromClauseTokens.size()) {
    		variableName = fromClauseTokens.get(iToken++).value();
    		QueryVariable parent = variableListing.get(fromClauseTokens.get(iToken).children().firstElement().value());
    		XPath targetPath = new VariableTargetPathGenerator().getTargetPath(parent.targetEntity(), versionIndex, fromClauseTokens.get(iToken++).children().iterator());
    		QueryVariable variable = new QueryVariable(variableName, targetPath);
    		parent.children().add(variable);
    		variableListing.add(variable);
    	}
    	return new FromClause(rootVariable, variableListing);
	}
}
