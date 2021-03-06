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

import org.dbwiki.data.index.DatabaseEntry;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.ui.CSS;

/** Utility class to manage database identity and URL generation
 * 
 * @author jcheney
 *
 */
public class DatabaseIdentifier {
	/*
	 * Public Constants
	 */
	
	public static final String PathSeparator = "/";

	
	/*
	 * Private Variables
	 */
	
	private String _databaseHomepage;
	private String _linkPrefix;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseIdentifier(String name) {
		if (name.startsWith(PathSeparator)) {
			_databaseHomepage = name;
		} else {
			_databaseHomepage = PathSeparator + name;
		}
		
		if (_databaseHomepage.equals(PathSeparator)) {
			_linkPrefix = "";
		} else if (_databaseHomepage.endsWith(PathSeparator)) {
			_linkPrefix = _databaseHomepage.substring(0, _databaseHomepage.length() - PathSeparator.length());
		} else {
			_linkPrefix = _databaseHomepage;
		}
	}

	
	/*
	 * Public Methods
	 */
	
	public String databaseHomepage() {
		return _databaseHomepage;
	}
	
	public String linkPrefix() {
		return _linkPrefix;
	}
	
	/** Gets the link associated with a DatabaseIdentifier.
	 * FIXME: Does this belong here?
	 * @param entry
	 * @param versionParameter
	 * @return
	 */
	public String getLink(DatabaseEntry entry, RequestParameterVersion versionParameter) {
		String css = null;
		if (entry.timestamp().isCurrent()) {
			css = CSS.CSSLinkActive;
		} else {
			css = CSS.CSSLinkInactive;
		}

		String target = linkPrefix() + entry.identifier().toURLString();
		if (!versionParameter.versionCurrent()) {
			target = target + "?" + versionParameter.toURLString();
		}
		
		return "<a CLASS=\"" + css + "\" HREF=\"" + target + "\">" + entry.label() + "</a>";

	}
}
