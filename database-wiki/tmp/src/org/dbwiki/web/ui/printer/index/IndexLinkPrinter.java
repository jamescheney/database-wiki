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
package org.dbwiki.web.ui.printer.index;

import org.dbwiki.data.index.DatabaseEntry;

import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.ui.CSS;

public class IndexLinkPrinter {
	/*
	 * Public Methods
	 */
	
	public String getLink(DatabaseIdentifier databaseIdentifier, DatabaseEntry entry, RequestParameterVersion versionParameter) throws org.dbwiki.exception.WikiException {
		String css = null;
		if (entry.timestamp().isCurrent()) {
			css = CSS.CSSLinkActive;
		} else {
			css = CSS.CSSLinkInactive;
		}

		String target = databaseIdentifier.linkPrefix() + entry.identifier().toURLString();
		if (!versionParameter.versionCurrent()) {
			target = target + "?" + versionParameter.toURLString();
		}
		
		return "<a CLASS=\"" + css + "\" HREF=\"" + target + "\">" + entry.label() + "</a>";

		//Version firstVersion = database.versionIndex().getByNumber(entry.timestamp().firstValue());
		//Version lastUpdate = database.versionIndex().getByNumber(entry.lastChange());
		//String title = "Created: " + firstVersion.name();
		//if (lastUpdate.number() > firstVersion.number()) {
		//	title = title + ", Last modified: " + lastUpdate.name();
		//}
		//return "<a CLASS=\"" + css + "\" HREF=\"" + target + "\" TITLE=\"" + title + "\">" + entry.label() + "</div></a>";
	}
}
