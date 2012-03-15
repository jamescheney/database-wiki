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
package org.dbwiki.web.ui.printer;

import java.net.URLEncoder;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.ui.CSS;

/** 
 * Abstract base class providing default behavior for menu printers
 * @author jcheney
 *
 */
public abstract class MenuPrinter implements HtmlContentPrinter {
	/*
	 * Public Constants
	 */
	
	public static final String MenuEdit = "menu_edit";
	public static final String MenuSettings = "menu_settings";
	public static final String MenuView = "menu_view";

	public static final String TabEdit = "t_edit";
	public static final String TabSettings = "t_settings";
	public static final String TabView = "t_view";

	
	/*
	 * Private Variables
	 */
	
	private WikiRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public MenuPrinter(WikiRequest request) {
		_request = request;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	protected abstract void printEditMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException;
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("<div class=\"" + CSS.CSSMenu + "\">");

		this.printEditMenu(printer);
		this.printViewMenu(printer);
		this.printSettingsMenu(printer);
		
		printer.add("</div>");

		this.printDeletePopup(printer);
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void printDeletePopup(HtmlLinePrinter printer) {
		printer.add("<div id=\"popup_delete\">"); 
		printer.add("\t<p class=\"" + CSS.CSSPopoupText + "\">Do you really want to delete the current object?</p>");
		printer.add("\t<br/><br/>");
		printer.add("\t<ul class=\"" + CSS.CSSPopupButton + "\">");
		printer.add("\t\t<li class=\"" + CSS.CSSPopupButton + "\"> <a class=\"" + CSS.CSSPopupButton + "\" href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterDelete + "\">Yes</a></li>");
		printer.add("\t\t<li class=\"" + CSS.CSSPopupButton + "\"> <a class=\"" + CSS.CSSPopupButton + "\" href=\"#\" onclick=\"disablePopup();return false;\">No</a></li>");
		printer.add("\t</ul>");  
		printer.add("</div>");  
		printer.add("<div id=\"background_popup\"></div>");  
	}
	
	private void printSettingsMenu(HtmlLinePrinter printer) {
		printer.add("\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabSettings + "\" onMouseOut=\"HideItem('" + MenuSettings + "');\" onMouseOver=\"ShowItem('" + MenuSettings + "');\">Settings</a>");
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuSettings + "\" onMouseOver=\"ShowItem('" + MenuSettings + "');\" onMouseOut=\"HideItem('" + MenuSettings + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		String dbHomeLink = _request.wri().databaseIdentifier().databaseHomepage();
		String resource = null;
		try {
			resource = "&" + RequestParameter.ParameterResource + "=" + URLEncoder.encode(_request.getRequestURI().toASCIIString(), "UTF-8");
		} catch (java.io.UnsupportedEncodingException uee) {
			resource = "&" + RequestParameter.ParameterResource + "=" + _request.getRequestURI().toASCIIString();
		}
		printer.add("\t\t\t\t\t<li><a href=\"" + dbHomeLink + "?" + RequestParameter.ParameterLayout + resource + "\">Layout</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + dbHomeLink + "?" + RequestParameter.ParameterTemplate + resource + "\">Template</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + dbHomeLink + "?" + RequestParameter.ParameterStyleSheet + resource + "\">Style Sheet</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + dbHomeLink + "?" + RequestParameter.ParameterURLDecoding + resource + "\">URL Decoding</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" +  RequestParameter.ParameterSettings + "\">Previous ...</a></li>");
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
	
	protected void printViewMenu(HtmlLinePrinter printer) {
		printer.add("\t\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabView + "\" onMouseOut=\"HideItem('" + MenuView + "');\" onMouseOver=\"ShowItem('" + MenuView + "');\">View</a>");
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuView + "\" onMouseOver=\"ShowItem('" + MenuView + "');\" onMouseOut=\"HideItem('" + MenuView + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		printer.add("\t\t\t\t\t<li><a href=\"/\">Server Home</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "\">Database</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "/" + DatabaseWiki.WikiPageRequestPrefix + "\">Wiki</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wiki().database().identifier().databaseHomepage() + "/" + DatabaseWiki.SchemaRequestPrefix + "\">Schema</a></li>");
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
}
