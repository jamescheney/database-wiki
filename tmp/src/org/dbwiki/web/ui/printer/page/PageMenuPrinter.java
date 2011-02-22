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
package org.dbwiki.web.ui.printer.page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiPageRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.MenuPrinter;

public class PageMenuPrinter extends MenuPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiPageRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public PageMenuPrinter(WikiPageRequest request) {
		super(request);
		
		_request = request;
	}
	
	protected void printViewMenu(HtmlLinePrinter printer) {
		String name = null;
		try {
			name = URLEncoder.encode(_request.page().getName(), "UTF-8");
		} catch (WikiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		printer.add("\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabView + "\" onMouseOut=\"HideItem('" + MenuView + "');\" onMouseOver=\"ShowItem('" + MenuView + "');\">View</a>");
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuView + "\" onMouseOver=\"ShowItem('" + MenuView + "');\" onMouseOut=\"HideItem('" + MenuView + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		printer.add("\t\t\t\t\t<li><a href=\"/\">Server Home</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "\">Database</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "/" + DatabaseWiki.WikiPageRequestPrefix + "\">Wiki</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "/" + DatabaseWiki.WikiPageRequestPrefix +
										"/" + name + "?history" + "\">History</a></li>");
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
	
	/*
	 * Public Methods
	 */
	
	public void printEditMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabEdit + "\" onMouseOut=\"HideItem('" + MenuEdit + "');\" onMouseOver=\"ShowItem('" + MenuEdit + "');\">Edit</a>");

		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterCreate + "\">New</a></li>");
		if (!_request.wri().resourceIdentifier().isRootIdentifier()) {
			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterEdit + "\">Edit</a></li>");
			printer.add("\t\t\t\t\t<li><a href=\"#\" onclick=\"loadPopup();return false\">Delete</a></li>");
		}
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
}
