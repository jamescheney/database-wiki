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
package org.dbwiki.data.query.handler;

/** DatabaseGroupNode for the result returned by the SELECT clause of a query.
 *
 * @author hmueller
 *
 */
import org.dbwiki.data.annotation.AnnotationList;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.time.TimeSequence;

public class ResultGroupNode extends DatabaseGroupNode {
	public ResultGroupNode(GroupSchemaNode entity, TimeSequence timestamp) {
		//FIXME: Pre/post numbers for result nodes are nonsense and should never be used! 
		super(entity, null, timestamp, new AnnotationList(),-1,-1);
	}

	/*
	 * Public Methods
	 */
	
	public NodeIdentifier identifier() {
		return new NodeIdentifier();
	}
}
