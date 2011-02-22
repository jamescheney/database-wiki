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

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class PageUpdateFormPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiPageRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public PageUpdateFormPrinter(WikiPageRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.paragraph("Page", CSS.CSSHeadline);

		if (_request.parameters().hasParameter(RequestParameter.ParameterCreate)) {
			this.printInsertForm(_request, body);
		} else if (_request.parameters().hasParameter(RequestParameter.ParameterEdit)) {
			this.printUpdateForm(_request, body);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private String getTextareaLine(String name, String value, int height) {
		return "<textarea name=\"" + name + "\" style=\"width: 99%; height: " + height + "px\">" + value + "</textarea>";		
	}

	private void printInsertForm(WikiPageRequest request, HtmlLinePrinter body) {
		body.openFORM("frmInsert", "POST", request.wri().getURL());

		body.openTABLE(CSS.CSSObjectFrame);
		body.openTR();
		body.openTD(CSS.CSSObjectListing);
		body.openTABLE(CSS.CSSObjectFrameActive);

		body.openTR();
		body.openTD(CSS.CSSObjectLabelActive);
		body.text("Title");
		body.closeTD();
		body.openTD(CSS.CSSObjectValueActive);
		body.text(this.getTextareaLine(RequestParameter.ActionValuePageTitle, "", 16));		
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeTD();
		body.closeTR();

		body.openTR();
		body.openTD(CSS.CSSObjectListing);
		body.openTABLE(CSS.CSSObjectFrameActive);

		body.openTR();
		body.openTD(CSS.CSSObjectValueActive);
		body.text(this.getTextareaLine(RequestParameter.ActionValuePageValue, "", 160));		
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeTD();
		body.closeTR();

		body.closeTABLE();

		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();
		body.addBUTTON("image", "action", RequestParameterAction.ActionInsert, "/pictures/button_save.gif");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addBUTTON("image", "action", RequestParameterAction.ActionCancel, "/pictures/button_cancel.gif");
		body.closeCENTER();
		body.closePARAGRAPH();
		body.closeFORM();
	}
	
	private void printUpdateForm(WikiPageRequest request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.openFORM("frmInsert", "POST", request.wri().getURL());
		body.addHIDDEN(RequestParameter.ActionValuePageID, request.page().getName());

		body.openTABLE(CSS.CSSObjectFrame);
		body.openTR();
		body.openTD(CSS.CSSObjectListing);
		body.openTABLE(CSS.CSSObjectFrameActive);

		body.openTR();
		body.openTD(CSS.CSSObjectLabelActive);
		body.text("Title");
		body.closeTD();
		body.openTD(CSS.CSSObjectValueActive);
		body.text(this.getTextareaLine(RequestParameter.ActionValuePageTitle, request.page().getName(), 16));		
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeTD();
		body.closeTR();

		body.openTR();
		body.openTD(CSS.CSSObjectListing);
		body.openTABLE(CSS.CSSObjectFrameActive);

		body.openTR();
		body.openTD(CSS.CSSObjectValueActive);
		body.text(this.getTextareaLine(RequestParameter.ActionValuePageValue, request.page().getContent(), 160));		
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeTD();
		body.closeTR();

		body.closeTABLE();

		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();
		body.addBUTTON("image", "action", RequestParameterAction.ActionUpdate, "/pictures/button_save.gif");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addBUTTON("image", "action", RequestParameterAction.ActionCancel, "/pictures/button_cancel.gif");
		body.closeCENTER();
		body.closePARAGRAPH();
		body.closeFORM();
	}
}
