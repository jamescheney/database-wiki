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
package org.dbwiki.data.resource;

import org.dbwiki.exception.web.WikiRequestException;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.server.DatabaseWiki;

/** An SchemaNodeIdentifier - id of a schema node
 * 
 * @author jcheney
 *
 */
public class SchemaNodeIdentifier implements ResourceIdentifier {
	/*
	 * Private Variables
	 */
	
	private int _nodeID;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaNodeIdentifier() {
		_nodeID = -1;
	}
	
	public SchemaNodeIdentifier(int nodeID) {
		_nodeID = nodeID;
	}
	
	public SchemaNodeIdentifier(RequestURL<?> url) throws org.dbwiki.exception.WikiException {
		if (url.size() == 0) {
			_nodeID = -1;
		} else if (url.size() == 1) {
			try {
				_nodeID = Integer.decode("0x" + url.get(0).decodedText());
			} catch (java.lang.NumberFormatException exception) {
				throw new WikiRequestException(WikiRequestException.InvalidUrl, url.toString());
			}
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidUrl, url.toString());
		}
	}
	
	/*
	 * Public Methods
	 */
	
	public int compareTo(ResourceIdentifier identifier) {
		return (nodeID() - ((SchemaNodeIdentifier)identifier).nodeID());
	}

	public boolean equals(ResourceIdentifier identifier) {
		return (nodeID() == ((SchemaNodeIdentifier)identifier).nodeID());
	}

	public boolean isRootIdentifier() {
		return (_nodeID == -1);
	}
	
	public int nodeID() {
		return _nodeID;
	}
	
	public String toParameterString() {
		return String.valueOf(_nodeID);
	}
	
	public String toURLString() {
		if (_nodeID == -1) {
			return "/" + DatabaseWiki.SchemaRequestPrefix + "/";
		} else {
			return "/" + DatabaseWiki.SchemaRequestPrefix + "/" + Integer.toHexString(_nodeID).toUpperCase();
		}
	}
}
