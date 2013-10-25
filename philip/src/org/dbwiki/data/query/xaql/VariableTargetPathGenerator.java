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

/** Generates variable target paths from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Iterator;

import org.dbwiki.data.query.xpath.VariableXPath;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.time.VersionIndex;

public class VariableTargetPathGenerator implements TargetPathGenerator {

	public VariableXPath getTargetPath(SchemaNode entity, VersionIndex versionIndex, Iterator<XAQLToken> pathTokens) throws org.dbwiki.exception.WikiException {

		String variableName = pathTokens.next().value();
		
		if (pathTokens.hasNext()) {
			return new VariableXPath(variableName, entity, new RelativeTargetPathGenerator().getTargetPath(entity, versionIndex, pathTokens));
		} else {
			return new VariableXPath(variableName, entity);
		}
	}
}
