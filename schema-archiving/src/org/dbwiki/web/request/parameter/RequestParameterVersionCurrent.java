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

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.schema.Entity;

public class RequestParameterVersionCurrent extends RequestParameterVersion {
	/*
	 * Public Methods
	 */
	
	public boolean matches(DatabaseNode node) {
		return node.getTimestamp().isCurrent();
	}
	
	public boolean matches(Entity entity) {
		return entity.getTimestamp().isCurrent();
	}
		
	public String value() {
		return RequestParameterVersion.VersionCurrent;
	}

	public boolean versionAll() {
		return false;
	}

	public boolean versionChangesSince() {
		return false;
	}

	public boolean versionCurrent() {
		return true;
	}

	public boolean versionSingle() {
		return false;
	}
}
