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

import org.dbwiki.data.database.Database;
import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;

import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints a form for creating new schema nodes.
 * 
 * @author jcheney
 *
 */
public class CreateSchemaNodeFormPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiRequest  _request;
	
	
	/*
	 * Constructors
	 */
	
	public CreateSchemaNodeFormPrinter(WikiRequest  request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter printer) throws org.dbwiki.exception.WikiException {
		printer.paragraph("Create new schema element", CSS.CSSHeadline);

		printer.openFORM("frmSchema", "POST", _request.wri().getURL());

		printer.openTABLE(CSS.CSSFormContainer);

		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);

		printer.openTABLE(CSS.CSSObjectFrameActive);
		
		printer.openTR();
		printer.openTD(CSS.CSSObjectLabelActive);
		printer.text("NAME");
		printer.closeTD();
		printer.openTD(CSS.CSSObjectValueActive);
		printer.addTEXTAREA(DatabaseWiki.ParameterSchemaNodeName, "95", "");
		printer.closeTD();
		printer.closeTR();
		
		printer.closeTABLE();
		
		printer.closeTD();
		printer.closeTR();

		printer.openTR();
		printer.openTD(CSS.CSSObjectListing);

		printer.openTABLE(CSS.CSSObjectFrameActive);
		
		printer.openTR();
		printer.openTD(CSS.CSSObjectLabelActive);
		printer.text("TYPE");
		printer.closeTD();
		printer.openTD(CSS.CSSObjectValueActive);
		printer.addRADIOBUTTON("Attribute", DatabaseWiki.ParameterSchemaNodeType, String.valueOf(Database.SchemaNodeTypeAttribute), true);
		printer.addBR();
		printer.addRADIOBUTTON("Group", DatabaseWiki.ParameterSchemaNodeType, String.valueOf(Database.SchemaNodeTypeGroup), false);
		printer.closeTD();
		printer.closeTR();

		printer.closeTABLE();

		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
				
//		printer.addBUTTON("image", "action", RequestParameterAction.ActionSchemaNode, "/pictures/button_save.gif");
		printer.addRealBUTTON("submit",
				"action", RequestParameterAction.ActionSchemaNode, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
//		printer.addBUTTON("image", "action", RequestParameterAction.ActionCancel, "/pictures/button_cancel.gif");
		printer.closeCENTER();
		printer.closePARAGRAPH();
		printer.closeFORM();
	}
}
