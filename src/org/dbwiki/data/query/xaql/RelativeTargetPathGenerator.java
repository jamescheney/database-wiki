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

/** Generates relative target paths from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Iterator;

import org.dbwiki.data.query.condition.Condition;
import org.dbwiki.data.query.condition.Conjunction;
import org.dbwiki.data.query.xpath.IndexCondition;
import org.dbwiki.data.query.xpath.SubPathCondition;
import org.dbwiki.data.query.xpath.XPath;
import org.dbwiki.data.query.xpath.XPathComponent;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.data.WikiQueryException;

public class RelativeTargetPathGenerator implements TargetPathGenerator {

	/*
	 * Public Methods
	 */
	
	public XPath getTargetPath(SchemaNode parentEntity, VersionIndex versionIndex, Iterator<XAQLToken> pathTokens) throws org.dbwiki.exception.WikiException {

		XAQLToken token = pathTokens.next();
		
		String entityName = token.children().firstElement().value();
		SchemaNode entity = ((GroupSchemaNode)parentEntity).children().get(entityName);
		if (entity == null) {
			throw new WikiQueryException(WikiQueryException.InvalidQueryStatement, "Unknown entity " + entityName + " as child of " + parentEntity.label());
		}
		
		XPathComponent pathElement = null;
		if (token.children().size() == 2) {
			XAQLToken conditionToken = token.children().get(1);
			if (conditionToken.type() == XAQLToken.INDEX_CONDITION) {
				int index = -1;
				try {
					index = Integer.parseInt(conditionToken.children().firstElement().value());
				} catch (java.lang.NumberFormatException nfe) {
					throw new WikiQueryException(WikiQueryException.InvalidQueryStatement, "Invalid number format " + conditionToken.children().firstElement().value());
				}
				pathElement = new XPathComponent(entity, new IndexCondition(index));
			} else {
				Condition condition = null;
				if (conditionToken.children().size() > 1) {
					Conjunction conjunction = new Conjunction();
					for (int iCondition = 0; iCondition < conditionToken.children().size(); iCondition++) {
						conjunction.add(new ConditionGenerator().getCondition(entity, versionIndex, conditionToken.children().get(iCondition).children(), this));
					}
					condition = conjunction;
				} else {
					condition = new ConditionGenerator().getCondition(entity, versionIndex, conditionToken.children().get(0).children(), this);
				}
				pathElement = new XPathComponent(entity, new SubPathCondition(condition));
			}
		} else {
			pathElement = new XPathComponent(entity);
		}
		
		if (pathTokens.hasNext()) {
			return new XPath(pathElement, new RelativeTargetPathGenerator().getTargetPath(entity, versionIndex, pathTokens));
		} else {
			return new XPath(pathElement);
		}
	}
}
