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
package org.dbwiki.web.ui.printer.schema;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.MenuPrinter;

/** Printer for the schema edit menu
 * 
 * @author jcheney
 *
 */
public class SchemaMenuPrinter extends MenuPrinter {
	/*
	 * Private Variables
	 */
	
	private static final String menuLabelDelete = "Delete";
	// FIXME #schemaversioning: The "Edit" field is currently unused.
	@SuppressWarnings("unused")
	private static final String menuLabelEdit   = "Edit";
	private static final String menuLabelNew    = "New ...";
	
	private WikiSchemaRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaMenuPrinter(WikiSchemaRequest request) {
		super(request);
		
		_request = request;
	}
	
	/*
	 * Public Methods
	 */
	
	public void printEditMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabEdit + "\" onMouseOut=\"HideItem('" + MenuEdit + "');\" onMouseOver=\"ShowItem('" + MenuEdit + "');\">Edit</a>");

		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
//		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterCreate + "\">New</a></li>");
//		if (!_request.wri().resourceIdentifier().isRootIdentifier()) {
//			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterEdit + "\">Edit</a></li>");
//			printer.add("\t\t\t\t\t<li><a href=\"#\" onclick=\"loadPopup();return false\">Delete</a></li>");
//		}
		
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" +
					RequestParameter.ParameterCreateEntity + "\" class=\"" +
						CSS.CSSMenuSubSub + "\">" + menuLabelNew + "</a></li>");
		printer.add("\t\t\t\t\t<li><a href=\"#\" onclick=\"loadPopup();return false\">" + menuLabelDelete + "</a></li>");

		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
}
