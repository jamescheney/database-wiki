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

import org.dbwiki.web.html.HtmlLinePrinter;


import org.dbwiki.web.request.WikiDataRequest;

import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints a form containing an input text box.
 * 
 * @author jcheney
 *
 */
public class InputFormPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private String _actionParameterName;
	private String _command;
	private String _inputParameterName;
	private WikiDataRequest _request;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public InputFormPrinter(WikiDataRequest request, String title, String command, String actionParameterName, String inputParameterName) {
		_actionParameterName = actionParameterName;
		_command = command;
		_inputParameterName = inputParameterName;
		_request = request;
		_title = title;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.paragraph(_title, CSS.CSSHeadline);
		
		body.addBR();
		body.addBR();
		
		body.openFORM("frmInput", "POST", _request.wri().getURL());
		body.addHIDDEN(_actionParameterName, "");

		body.openCENTER();
		body.openTABLE(CSS.CSSInputForm);
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text(_command);
		body.closePARAGRAPH();
		body.text("<textarea name=\"" + _inputParameterName + "\" style=\"width: 99%; height:1.2em;\"></textarea>");		
		body.closeTD();
		body.closeTR();
		body.closeTABLE();
		body.closeCENTER();
		
		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();
		body.addBUTTON("image", "button", "/pictures/button_ok.gif");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		body.closePARAGRAPH();
		body.closeFORM();
	}
}
