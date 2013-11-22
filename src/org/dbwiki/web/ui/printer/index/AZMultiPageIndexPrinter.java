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

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.DatabaseContent;

import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.CSS;

/** 
 * Print A-Z index of entries as multiple pages, linked by bar across the top.
 * @author jcheney
 *
 */
public class AZMultiPageIndexPrinter extends IndexContentPrinter {
	/*
	 * Constructors
	 */
	
	public AZMultiPageIndexPrinter(WikiRequest<?>  request, DatabaseContent content) {
		super(request, content);
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void listContent(DatabaseIdentifier databaseIdentifier, ContentIterator iterator, WikiRequest<?>  request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		ContentIndex contentIndex = new ContentIndex(iterator);
		
		String indexLine = "";
		for (int iContainer = 0; iContainer < contentIndex.size(); iContainer++) {
			ContentIndexContainer container = contentIndex.get(iContainer);
			String indexLink = "";
			if (container.size() > 0) {
				try {
					indexLink = databaseIdentifier.databaseHomepage() + "?" + RequestParameter.ParameterIndexPosition + "=" + URLEncoder.encode(container.key(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				if (request.parameters().hasParameter(RequestParameter.ParameterVersion)) {
					indexLink = indexLink + "&" + request.parameters().get(RequestParameter.ParameterVersion).toURLString();
				}
				indexLink = "<a CLASS=\"" + CSS.CSSIndexContentIndex + "\" HREF=\"" + indexLink + "\">" + container.key() + "</a>";
			} else {
				indexLink = container.key();
			}
			if (iContainer == 0) {
				indexLine = indexLink;
			} else {
				indexLine = indexLine + "&nbsp;&nbsp;&nbsp;" + indexLink;
			}
		}
		
		body.add("<p CLASS=\"" + CSS.CSSIndexContentIndex + "\">" + indexLine + "</p>");
		
		
		String key = null;
		if (request.parameters().hasParameter(RequestParameter.ParameterIndexPosition)) {
			key = request.parameters().get(RequestParameter.ParameterIndexPosition).value();
		} else if (contentIndex.size() > 0) {
			key = contentIndex.get(0).key();
		}
		
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));

		if (key != null) {
			if (contentIndex.containsKey(key)) {
				this.addContainer(databaseIdentifier, contentIndex.get(key), versionParameter, body);
			}
		} else if (contentIndex.size() > 0) {
			for (int iContainer = 0; iContainer < contentIndex.size(); iContainer++) {
				this.addContainer(databaseIdentifier, contentIndex.get(iContainer), versionParameter, body);
			}
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void addContainer(DatabaseIdentifier databaseIdentifier, ContentIndexContainer container, RequestParameterVersion versionParameter, HtmlLinePrinter body) {
		if(container.size() > 0) {
			body.add("<p CLASS=\"" + CSS.CSSIndexContent + "\"><a NAME=\"" + container.key() + "\">" + container.key() + "</a></p>");
			body.add("<ul CLASS=\"" + CSS.CSSIndexContent + "\">");
			for (int iEntry = 0; iEntry < container.size(); iEntry++) {
				body.add("<li>" + databaseIdentifier.getLink(container.get(iEntry), versionParameter) + "</li>");
			}
			body.add("</ul>");
		}
	}
}
