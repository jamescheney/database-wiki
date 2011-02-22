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
package org.dbwiki.web.request.parameter;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;

public class RequestParameterVersionChanges extends RequestParameterVersionNumber {
	/*
	 * Constructors
	 */
	
	public RequestParameterVersionChanges(int versionNumber) {
		super(versionNumber);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean matches(DatabaseNode node) throws org.dbwiki.exception.WikiException {
		if (node.getTimestamp().changedSince(this.versionNumber())) {
			return true;
		} else if (node.isElement()) {
			boolean matches = false;
			if (((DatabaseElementNode)node).isAttribute()) {
				DatabaseAttributeNode attribute = (DatabaseAttributeNode)node;
				for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
					if (attribute.value().get(iValue).getTimestamp().changedSince(this.versionNumber())) {
						matches = true;
						break;
					}
				}
			} else {
				DatabaseGroupNode group = (DatabaseGroupNode)node;
				for (int iElement = 0; iElement < group.children().size(); iElement++) {
					if (this.matches((DatabaseNode)group.children().get(iElement))) {
						matches = true;
						break;
					}
				}
			}
			return matches;
		} else {
			return false;
		}
	}

	public String value() {
		return RequestParameterVersion.VersionChanges + this.versionNumber();
	}

	public boolean versionChangesSince() {
		return true;
	}

	public boolean versionSingle() {
		return false;
	}
}
