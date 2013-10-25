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
package org.dbwiki.web.ui.printer.data;

import org.dbwiki.data.database.DatabaseElementNode;

import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;

import org.dbwiki.web.ui.printer.MenuPrinter;

/** Prints Edit menu for data pages
 * 
 * @author jcheney
 *
 */
public class DataMenuPrinter extends MenuPrinter {
	/*
	 * Private Constants
	 */
	
	private static final String menuLabelCopy          = "Copy";
	private static final String menuLabelDelete        = "Delete";
	private static final String menuLabelEdit          = "Edit";
	private static final String menuLabelInsert        = "New ...";
	private static final String menuLabelNew           = "Element ...";
	private static final String menuLabelPaste         = "Paste ...";
	private static final String menuLabelPasteExternal = "Enter URL ...";
	private static final String menuLabelPasteLocal    = "Copy buffer ...";
	
	
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private WikiDataRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public DataMenuPrinter(WikiDataRequest request, DatabaseLayouter layouter) {
		super(request);
		
		_request = request;
		_layouter = layouter;
	}
	
	
	/*
	 * Public Methods
	 */
	public void printEditMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t<a class=\"" + CSS.CSSMenu + "\" id=\"" + TabEdit + "\" onMouseOut=\"HideItem('" + MenuEdit + "');\" onMouseOver=\"ShowItem('" + MenuEdit + "');\">Edit</a>");

		if (_request.node() != null) {
			if (_request.node().getTimestamp().isCurrent()) {
				Entity entity = null;
				if (_request.node().isElement()) {
					entity = ((DatabaseElementNode)_request.node()).entity();
					if (!entity.isGroup()) {
						entity = null;
					}
				}
				this.printObjectMenu((GroupEntity)entity, printer);
			} else {
				DatabaseElementNode parent = _request.node().parent();
				if (parent != null) {
					if (parent.getTimestamp().isCurrent()) {
						printActivateMenu(printer);
					} else {
						printCopyOnlyMenu(printer);
					}
				} else {
					printActivateMenu(printer);
				}
			}
		} else {
			printIndexMenu(printer);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void printActivateMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterActivate + "\">Activate</a></li>");
		
		RequestParameterVersion version = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));
		String target = _request.wri().getURL() + "?" + RequestParameter.ParameterCopy;
		if (version.versionSingle()) {
			target = target + "&" + version.toURLString();
		}
		printer.add("\t\t\t\t\t<li><a href=\"" + target + "\">" + menuLabelCopy + "</a></li>");
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
	
	private void printCopyOnlyMenu(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		RequestParameterVersion version = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));
		String target = _request.wri().getURL() + "?" + RequestParameter.ParameterCopy;
		if (version.versionSingle()) {
			target = target + "&" + version.toURLString();
		}
		printer.add("\t\t\t\t\t<li><a href=\"" + target + "\">" + menuLabelCopy + "</a></li>");
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}

	private void printIndexMenu(HtmlLinePrinter printer) {
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		Entity root = _request.wiki().database().schema().root();
		printer.add("\t\t\t\t\t<li><span class=\"" + CSS.CSSMenuSubBox + "\">" + menuLabelInsert + "</span></li>");
		if (root != null) {
			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterCreate + "=" + root.id() + "\" class=\"" + CSS.CSSMenuSubSub + "\">" + _layouter.get(root).getName() + "</a></li>");
		} else {
			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterCreateEntity + "\" class=\"" + CSS.CSSMenuSubSub + "\">" + menuLabelNew + "</a></li>");
		}
		
		this.printPasteSubMenu(printer);
		
		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
	
	private void printObjectMenu(GroupEntity entity, HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.add("\t\t<div class=\"" + CSS.CSSMenuSub + "\" id=\"" + MenuEdit + "\" onMouseOver=\"ShowItem('" + MenuEdit + "');\" onMouseOut=\"HideItem('" + MenuEdit + "');\">");
		printer.add("\t\t\t<div class=\"" + CSS.CSSMenuSubBox + "\">");
		printer.add("\t\t\t\t<ul>");
		
		if (entity != null) {
			printer.add("\t\t\t\t\t<li><span class=\"" + CSS.CSSMenuSubBox + "\">" + menuLabelInsert + "</span></li>");
			for (int iChild = 0; iChild < entity.children().size(); iChild++) {
				Entity child = entity.children().get(iChild);
				// skip deleted entities
				if(child.getTimestamp().isCurrent()) {
					printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() +
							"?" + RequestParameter.ParameterCreate + "=" + child.id() +
							"\" class=\"" + CSS.CSSMenuSubSub + "\">" + _layouter.get(child).getName() +
							"</a></li>");
				}
			}
			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterCreateEntity + "\" class=\"" + CSS.CSSMenuSubSub + "\">" + menuLabelNew + "</a></li>");
		}

		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterEdit + "\">" + menuLabelEdit + "</a></li>");

		RequestParameterVersion version = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));
		String target = _request.wri().getURL() + "?" + RequestParameter.ParameterCopy;
		if (version.versionSingle()) {
			target = target + "&" + version.toURLString();
		}
		printer.add("\t\t\t\t\t<li><a href=\"" + target + "\">" + menuLabelCopy + "</a></li>");
		
		this.printPasteSubMenu(printer);
		
		printer.add("\t\t\t\t\t<li><a href=\"#\" onclick=\"loadPopup();return false\">" + menuLabelDelete + "</a></li>");

		printer.add("\t\t\t\t</ul>");
		printer.add("\t\t\t</div>");
		printer.add("\t\t</div>");
	}
	
	private void printPasteSubMenu(HtmlLinePrinter printer) {
		printer.add("\t\t\t\t\t<li><span class=\"" + CSS.CSSMenuSubBox + "\">" + menuLabelPaste + "</span></li>");
		if (_request.copyBuffer() != null) {
			printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterPaste + "\" class=\"" + CSS.CSSMenuSubSub + "\">" + menuLabelPasteLocal + "</a></li>");
		}
		printer.add("\t\t\t\t\t<li><a href=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterPasteForm + "\" class=\"" + CSS.CSSMenuSubSub + "\">" + menuLabelPasteExternal + "</a></li>");
	}
}
