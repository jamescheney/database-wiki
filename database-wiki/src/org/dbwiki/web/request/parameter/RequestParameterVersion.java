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
import org.dbwiki.data.schema.SchemaNode;

public abstract class RequestParameterVersion {
	/*
	 * Public Constants
	 */
	public static final String VersionAll     = "all";
	public static final String VersionChanges = "changes_since_";
	public static final String VersionCurrent = "current";
	
	/*
	 * Abstract Methods
	 */
	
	/**
	 * Returns true if the node matches the filter associated with this
	 * 'RequestParameterVersion'.
	 */
	public abstract boolean matches(DatabaseNode node) throws org.dbwiki.exception.WikiException;
	
	/**
	 * Returns true if the schema node matches the filter associated with this
	 * 'RequestParameterVersion'.
	 */
	public abstract boolean matches(SchemaNode schemaNode) throws org.dbwiki.exception.WikiException;
	public abstract String value();
	public abstract boolean versionAll();
	public abstract boolean versionChangesSince();
	public abstract boolean versionCurrent();
	public abstract boolean versionSingle();
	
	/*
	 * Public Methods
	 */
	
	public String toURLString() {
		return RequestParameter.ParameterVersion + "=" + this.value();
	}
}
