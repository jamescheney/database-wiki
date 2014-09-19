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
package org.dbwiki.web.ui.printer.server;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints the server popup menu
 * 
 * @author jcheney
 *
 */
public class ServerMenuPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiServer _server;
	private HttpRequest _request;
	
	/*
	 * Constructors
	 */
	
	public ServerMenuPrinter(WikiServer server, HttpRequest request) {
		_server = server;
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.add("\t\t<div class=\"" + CSS.CSSMenu + "\">");

		body.add("\t\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"t1\" href=\"/?" + RequestParameter.ParameterCreate + "\">New</a>");
		
		if (_server.size() > 0 && _request.user() != null && (ownsSomething() || _request.user().is_admin())) {
			body.add("\t\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"t2\" onMouseOut=\"HideItem('edit_submenu');\" onMouseOver=\"ShowItem('edit_submenu');\">Edit</a>");
			this.printPopUp(RequestParameter.ParameterEdit, body);
			body.add("\t\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"t3\" onMouseOut=\"HideItem('reset_submenu');\" onMouseOver=\"ShowItem('reset_submenu');\">Reset</a>");
			this.printPopUp(RequestParameter.ParameterReset, body);
			if(_request.user().is_admin()) {
				body.add("\t\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"t4\" href=\"/?" + RequestParameter.ParameterAllUsers + "\">Users</a>");
			}
		}
		
		body.add("\t\t</div>");
		
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void printPopUp(String name, HtmlLinePrinter body) {
		body.add("\t\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + name + "_submenu\" onMouseOver=\"ShowItem('" + name + "_submenu');\" onMouseOut=\"HideItem('" + name + "_submenu');\">");
		body.add("\t\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		body.add("\t\t\t\t\t<ul>");
		
		for (int iWiki = 0; iWiki < _server.size(); iWiki++) {
			DatabaseWiki wiki = _server.get(iWiki);
			if(_request.user().id() == wiki.owner() || _request.user().is_admin()) {
				body.add("\t\t\t\t\t\t<li><a href=\"?" + name + "=" + wiki.id() + "\">" + wiki.getTitle() + "</a></li>");
			}
		}
		
		body.add("\t\t\t\t\t</ul>");
		body.add("\t\t\t\t</div>");
		body.add("\t\t\t</div>");
	}
	
	private boolean ownsSomething() {
		for (int iWiki = 0; iWiki < _server.size(); iWiki++) {
			if(_server.get(iWiki).owner() == _request.user().id()) {
				return true;
			}
		}
		return false;
	}

}
