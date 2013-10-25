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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

// FIXME #wikipages: Find a way of encoding the timestamp
//   There is currently no way of encoding the timestamp in 
//   a single URI parameter.
//
//   ResourceIdentifier implicitly assumes that an entire
//   identifier can be encoded in a single URI parameter.

/** PageIdentifier identifies a wiki page.
 * Contains a title and a timestamp for the page.
 * Also provides a name for the page that can be used in queries.
 */
public class PageIdentifier implements ResourceIdentifier {
	/*
	 * Private Variables
	 */
	private String _title;
	private long _timestamp;
	
	/*
	 * Constructors
	 */
	public PageIdentifier(String title, long timestamp) {
		_title = title;
		_timestamp = timestamp;
	}
	
	public PageIdentifier(String title) {
		this(title, -1);
	}
	
	public PageIdentifier() {
		this("");
	}
	
	/*
	 * Public Methods
	 */
	public boolean equals(ResourceIdentifier identifier) {
		return this.toURLString().equals(((PageIdentifier)identifier).toURLString());
	}

	public boolean isRootIdentifier() {
		return (_title.length() == 0 && _timestamp == -1);
	}

	public long getTimestamp() {
		return _timestamp;
	}
	
	public String toParameterString() {
		return _title;
	}

	public String toQueryString() {
		try {
			return URLDecoder.decode(_title, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public String toURLString() {
		return "/" + _title;
	}
}
