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
package org.dbwiki.data.query.xpath;

/** Find all matching nodes for a XPAth expression starting at the root
 * of the tree.
*
* @author hmueller
*
*/
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.query.handler.QueryNodeHandler;

public class AbsoluteXPathConsumer {

	public void consume(DatabaseElementNode node, XPath targetPath, QueryNodeHandler consumer) {
		
		XPathComponent pathComponent = targetPath.firstElement();
		if (pathComponent.matches(node)) {
			if (targetPath.size() > 1) {
				DatabaseGroupNode group = (DatabaseGroupNode)node;
				for (int iChild = 0; iChild < group.children().size(); iChild++) {
					new AbsoluteXPathConsumer().consume(group.children().get(iChild), targetPath.subpath(1), consumer);
				}
			} else {
				consumer.handle(node);
			}
		}
	}
}
