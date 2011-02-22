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
package org.dbwiki.web.ui;

import java.util.Vector;


import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.ui.printer.CSSLinePrinter;

public abstract class DatabaseWikiContentGenerator extends HtmlContentGenerator {
	/*
	 * Private Variables
	 */
	
	private RequestParameterList _parameters;
	private DatabaseIdentifier _dbIdentifier;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWikiContentGenerator(WikiRequest request, String title, CSSLinePrinter cssPrinter) {
		super(request);
		
		_dbIdentifier = request.wri().databaseIdentifier();
		_parameters = request.parameters();
		_title = title;
		
		this.put(ContentCSS, cssPrinter);
	}

	public DatabaseWikiContentGenerator(DatabaseIdentifier dbIdentifier, String title, CSSLinePrinter cssPrinter) {
		_dbIdentifier = dbIdentifier;
		_parameters = new RequestParameterList();
		_title = title;

		this.put(ContentCSS, cssPrinter);
	}

	
	/*
	 * Public Methods
	 */
	
	public boolean contains(String key) {
		if (key.equals(ContentDatabaseLink)) {
				return true;
		} else if (key.equals(ContentSearch)) {
			return true;
		} else if (key.equals(ContentTitle)) {
			return true;
		} else {
			return super.contains(key);
		}
	}
	
	public void print(String key, Vector<String> args, HtmlPage page, String indention) throws org.dbwiki.exception.WikiException {
		if (key.equals(ContentDatabaseLink)) {
			String title = null;
			if (args != null) {
				title = args.get(0);
			} else {
				title = _title;
			}
			String link = "<a CLASS=\"" + CSS.CSSDatabaseHomeLink + "\" HREF=\"" + _dbIdentifier.databaseHomepage() + "\">" + title + "</a>";
			page.add(indention + "<p CLASS=\"" + CSS.CSSDatabaseHomeLink + "\">" + link + "</p>");
		} else if (key.equals(ContentSearch)) {
			page.add(indention + "<form name=\"frmSearch\" method=\"GET\" action=\"" + _dbIdentifier.databaseHomepage() + "\">");
			String line = "<input id=\"" + CSS.CSSSearch + "\" name=\"search\" type=\"text\" value=\"";
			if (_parameters.hasParameter(RequestParameter.ParameterSearch)) {
				RequestParameter searchParameter = _parameters.get(RequestParameter.ParameterSearch);
				if (searchParameter.hasValue()) {
					line = line + searchParameter.value();
				}
			}
			page.add(indention + line + "\"/>");
			page.add(indention + "</form>");
		} else if (key.equals(ContentTitle)) {
			if (args != null) {
				page.add(args.get(0) + " - " + _title);
			} else {
				page.add(_title);
			}
		} else {
			super.print(key, args, page, indention);
		}
	}
}
