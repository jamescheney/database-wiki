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

import java.net.URLEncoder;

import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.WikiServer;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public abstract class HtmlContentGenerator {
	/*
	 * Public Constants
	 */
	
	public static final String ContentAnnotation   = "annotation";
	public static final String ContentContent      = "content";
	public static final String ContentCSS          = "css";
	public static final String ContentDatabaseLink = "database_home_link";
	public static final String ContentLogin        = "login";
	public static final String ContentMenu         = "menu";
	public static final String ContentObjectLink   = "object_path_link";
	public static final String ContentProvenance   = "provenance";
	public static final String ContentSearch       = "search";
	public static final String ContentTimemachine  = "timemachine";
	public static final String ContentTitle        = "title";
	
	
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, HtmlContentPrinter> _contentPrinter;
	private HttpRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public HtmlContentGenerator(HttpRequest request) {
		_request = request;
		
		_contentPrinter = new Hashtable<String, HtmlContentPrinter>();
	}
	
	public HtmlContentGenerator() {
		this(null);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean contains(String key) {
		if (key.equals(ContentLogin)) {
			return (_request != null);
		} else {
			return _contentPrinter.containsKey(key);
		}
	}
	
	public void put(String key, HtmlContentPrinter printer) {
		_contentPrinter.put(key, printer);
	}

	public void print(String key, Vector<String> args, HtmlPage page, String indention) throws org.dbwiki.exception.WikiException {
	if (key.equals(ContentLogin)) {
		if (_request != null) {
			if (_request.user() != null) {
				page.add(indention + "<p CLASS=\"" + CSS.CSSLogin + "\">You are currently logged in as <span CLASS=\"" + CSS.CSSLogin + "\">" + _request.user().fullName() + "</span></p>");
			} else {
				try {
					String loginRedirectLink = WikiServer.SpecialFolderLogin + "?" + RequestParameter.ParameterResource + "=" + URLEncoder.encode(_request.exchange().getRequestURI().toASCIIString(), "UTF-8");
					page.add(indention + "<p CLASS=\"" + CSS.CSSLogin + "\"><a CLASS=\"" + CSS.CSSLogin + "\" HREF=\"" + loginRedirectLink + "\">Login</a></p>");
				} catch (java.io.UnsupportedEncodingException uee) {
				}
			}
		}
	} else if (_contentPrinter.containsKey(key)) {
			_contentPrinter.get(key).print(new HtmlLinePrinter(page, indention));
		}
	}
}
