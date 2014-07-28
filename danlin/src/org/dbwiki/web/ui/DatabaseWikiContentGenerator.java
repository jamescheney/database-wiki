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
import org.dbwiki.exception.WikiException;

import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.ui.printer.CSSLinePrinter;
import org.dbwiki.web.ui.printer.ExceptionPrinter;
import org.dbwiki.web.ui.printer.ImportPrinter;


/** Extends HtmlContentGenerator to provide behavior specific to DBWiki pages */

public class DatabaseWikiContentGenerator extends HtmlContentGenerator {
	/*
	 * Private Variables
	 */
	/** _parameters - the parameters passed into the request.  Currently used only for ContentSearch */
	private RequestParameterList _parameters;
	/** _dbIdentifier - the identifier of the DB this generator is associated with */
	private DatabaseIdentifier _dbIdentifier;
	/** _title - the title of the DBWiki */
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	// FIXME #inline next two constructors
	protected DatabaseWikiContentGenerator(WikiRequest<?> request, String title, CSSLinePrinter cssPrinter) {
		super(request);
		
		_dbIdentifier = request.wri().databaseIdentifier();
		_parameters = request.parameters();
		_title = title;
		
		this.put(ContentCSS, cssPrinter);
		this.put(ContentImport, new ImportPrinter());
	}

	public DatabaseWikiContentGenerator(DatabaseWiki wiki, WikiRequest<?> request) {
		this(request, request.wiki().getTitle(), wiki.cssLinePrinter());
	}
	
	// FIXME #htmlgeneration: This seems to not initialize the parent's _request field 
	// FIXME #inline next two constructors
	protected DatabaseWikiContentGenerator(DatabaseIdentifier dbIdentifier, String title, CSSLinePrinter cssPrinter) {
		_dbIdentifier = dbIdentifier;
		_parameters = new RequestParameterList();
		_title = title;

		this.put(ContentCSS, cssPrinter);
	}

	public DatabaseWikiContentGenerator(DatabaseWiki wiki, WikiException exception) {
		this(wiki.identifier(), wiki.getTitle(), wiki.cssLinePrinter());
		
		this.put(ContentContent, new ExceptionPrinter(exception));
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
	
	/** Uses first element of @args as a prefix to the title.
	 *  Overrides the DatabaseLink behavior to use the first argument, defaulting to title if null
	 *  Overrides the Search key to add the search textbox, using the "search" parameter to fill in a value from the URL if any.
	 *  Overrides Title to use title, with first argument as optional prefix.
	 */
	public void print(String key, Vector<String> args, HtmlPage page, String indention) throws org.dbwiki.exception.WikiException {
		if (key.equals(ContentDatabaseLink)) {
			String title = null;
			if (args != null) {
				if (args.size() > 0) {
					title = args.get(0);
				} else {
					title = "";
				}
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
