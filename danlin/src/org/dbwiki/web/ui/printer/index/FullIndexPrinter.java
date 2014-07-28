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

/** 
 * Print full index as an unordered list
 * @author jcheney
 *
 */
public class FullIndexPrinter extends IndexContentPrinter {
	/*
	 * Constructors
	 */
	
	public FullIndexPrinter(WikiRequest<?> request, DatabaseContent content) {
		super(request, content);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void listContent(DatabaseIdentifier databaseIdentifier, ContentIterator iterator, WikiRequest<?> request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.add("<ul CLASS=\"" + CSS.CSSIndexContent + "\">");
		
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));

		DatabaseEntry entry = null;
		while ((entry = iterator.next()) != null) {
			body.add("<li>" + databaseIdentifier.getLink( entry, versionParameter) + "</li>");
		}
		
		body.add("</ul>");
	}
}
