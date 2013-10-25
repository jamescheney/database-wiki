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

import org.dbwiki.data.index.ChangesSinceContentIterator;
import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.CurrentContentIterator;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.DatabaseEntry;
import org.dbwiki.data.index.FullContentIterator;
import org.dbwiki.data.index.VersionContentIterator;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionChanges;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Print the results of a search.
 * 
 * @author jcheney
 *
 */
public class SearchResultPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseContent _content;
	private WikiRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public SearchResultPrinter(WikiRequest request, DatabaseContent content) {
		_request = request;
		_content = content;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));

		ContentIterator iterator = null;

		if (versionParameter.versionAll()) {
			iterator = new FullContentIterator(_content);
		} else if (versionParameter.versionChangesSince()) {
			iterator = new ChangesSinceContentIterator(_content, ((RequestParameterVersionChanges)versionParameter).versionNumber());
		} else if (versionParameter.versionCurrent()) {
			iterator = new CurrentContentIterator(_content);
		} else if (versionParameter.versionSingle()) {
			iterator = new VersionContentIterator(_content, ((RequestParameterVersionSingle)versionParameter).versionNumber());
		}
		// may be unnecessary
		assert(iterator != null);
		
		body.add("<ul CLASS=\"" + CSS.CSSIndexContent + "\">");
		
		DatabaseEntry entry = null;
		while ((entry = iterator.next()) != null) {
			body.add("<li>" + _request.wri().databaseIdentifier().getLink(entry, versionParameter) + "</li>");
		}
		
		body.add("</ul>");
	}
}
