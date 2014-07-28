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
import org.dbwiki.data.time.TimeSequence;

/** Version parameter that represents the timestamp in the VERSION clause
 * of a XAQL query.
 * 
 * @author hmueller
 *
 */

public class RequestParameterVersionTimestamp extends RequestParameterVersion {

	/*
	 * Private Variables
	 */
	private TimeSequence _timestamp;
	
	/*
	 * Constructors
	 */
	public RequestParameterVersionTimestamp(TimeSequence timestamp) {
		_timestamp = timestamp;
	}

	/*
	 * Public Methods
	 */
	/** Filter those data nodes that match the given timestamp
	 * 
	 */
	public boolean matches(DatabaseNode node) throws org.dbwiki.exception.WikiException {
		return _timestamp.overlap(node.getTimestamp());
	}

	/** Filter those data nodes that match the given timestamp
	 * 
	 */
	public boolean matches(SchemaNode schemaNode) throws org.dbwiki.exception.WikiException {
		return _timestamp.overlap(schemaNode.getTimestamp());
	}

	/** Returns the same parameter as the current version request parameter
	 * 
	 *  FIXME: Not sure how to fix this but it results in the generation of links
	 *  in Wiki pages that refer to the current version of a node even if the
	 *  node itself isn't present in the current version of the database. Thus,
	 *  following the link displays an empty page. Have to make the timestamp of the
	 *  node a parameter to this function in order for it to work properly. 
	 * 
	 */
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
		return _timestamp.isCurrent();
	}

	public boolean versionSingle() {
		return false;
	}
}
