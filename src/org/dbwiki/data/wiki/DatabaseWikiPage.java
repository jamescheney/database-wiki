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
package org.dbwiki.data.wiki;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.dbwiki.lib.XML;
import org.dbwiki.user.User;

/** A struct that contains the database wiki page information:
 * id, name, content, timestamp and user
 * @author jcheney
 *
 */
public class DatabaseWikiPage {
	/*
	 * Private Variables
	 */
	private int _id;
	private String _name;
	private String _content;
	private long _timestamp;
	private User _user;
	
	/*
	 * Constructors
	 */
	public DatabaseWikiPage(int id, String name, String content, long timestamp, User user) {
		_id = id;
		_name = name;
		_content = content;
		_timestamp = timestamp;
		_user = user;
	}
	
	/*
	 * Public Methods
	 */
	public int getID() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getContent() {
		return _content;
	}

	public long getTimestamp() {
		return _timestamp;
	}
	
	public User getUser() {
		return _user;
	}
	
	public void write(OutputStreamWriter wikiout) throws IOException {
		// For each wiki page
		// Create file named <wikipagedir>/page<id>.xml
		// Write content of each file
		wikiout.write("<page id=\"" + getID() 
				    + "\" title=\"" + getName());
		if(getUser() != null) {
			wikiout.write( "\" user=\"" + getUser().login());
		}
		wikiout.write("\" timestamp=\"" + getTimestamp() + "\" >\n");
		wikiout.write(XML.maskText(getContent())); 
		wikiout.write("</page>");
	}
}
