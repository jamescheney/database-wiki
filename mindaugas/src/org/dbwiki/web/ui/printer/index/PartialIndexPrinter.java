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

import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.DatabaseEntry;
import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;


import org.dbwiki.web.ui.CSS;


/** Print a partial index , listing a fixed number per page with pager links
 * 
 * @author jcheney
 *
 */
public class PartialIndexPrinter extends IndexContentPrinter {
	/*
	 * Private Constants
	 */
	
	private int _entriesPerPage = 50;
	
	
	/*
	 * Constructors
	 */
	
	public PartialIndexPrinter(WikiRequest request, DatabaseContent content, int entriesPerPage) {
		super(request, content);
		_entriesPerPage = entriesPerPage;
	}
	public PartialIndexPrinter(WikiRequest request, DatabaseContent content) {
		super(request, content);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void listContent(DatabaseIdentifier databaseIdentifier, ContentIterator iterator, WikiRequest request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		int maxOutputCount = _entriesPerPage;
		int outputCount = 0;
		int readCount = 0;
		int startIndex = 1;
		
		if (request.parameters().hasParameter(RequestParameter.ParameterIndexPosition)) {
			try {
				startIndex = Integer.parseInt(request.parameters().get(RequestParameter.ParameterIndexPosition).value());
			} catch (Exception e) {
			}
		}

		RequestParameterVersion versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
		
		body.add("<ul CLASS=\"" + CSS.CSSIndexContent + "\">");

		DatabaseEntry entry = null;
		while ((entry = iterator.next()) != null) {
			readCount++;
			if (readCount >= startIndex) {
				body.add("<li>" + databaseIdentifier.getLink(entry, versionParameter) + "</li>");
				outputCount++;
			}
			if (outputCount == maxOutputCount) {
				break;
			}
		}
		
		body.add("</ul>");
		
		RequestParameter parameterVersion = request.parameters().get(RequestParameter.ParameterVersion);
		
		if (outputCount > 0) {
			String navigationLine = "";
			if (startIndex > 1) {
				navigationLine = "<a CLASS=\"" + CSS.CSSIndexNavbar + "\" HREF=\"" + databaseIdentifier.databaseHomepage() + "?" + RequestParameter.ParameterIndexPosition + "=" + Math.max(1, (startIndex - _entriesPerPage));
				if (parameterVersion != null) {
					navigationLine = navigationLine + "&" + parameterVersion.toURLString();
				}
				navigationLine = navigationLine + "\">&lt;&lt; prev&nbsp;&nbsp;&nbsp;</a>";
			}
			navigationLine = navigationLine + "[" + startIndex + "-" + (startIndex + outputCount - 1) + "]";
			if (iterator.next() != null) {
				navigationLine = navigationLine + "<a CLASS=\"" + CSS.CSSIndexNavbar + "\" HREF=\"" + databaseIdentifier.databaseHomepage() + "?" + RequestParameter.ParameterIndexPosition + "=" + (startIndex + outputCount);
				if (parameterVersion != null) {
					navigationLine = navigationLine + "&" + parameterVersion.toURLString();
				}
				navigationLine = navigationLine + "\">&nbsp;&nbsp;&nbsp;next &gt;&gt;</a>";
			}
			body.add("<p CLASS=\"" + CSS.CSSIndexNavbar + "\">" + navigationLine + "</p>");
		}
	}
}
