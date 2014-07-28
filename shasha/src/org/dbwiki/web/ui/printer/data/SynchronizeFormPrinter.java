package org.dbwiki.web.ui.printer.data;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class SynchronizeFormPrinter implements HtmlContentPrinter{
	/*
	 * Private Variables
	 */
	
	private String _actionParameterName;
	private String _command;
	private String _inputParameterName;
	private WikiDataRequest<?> _request;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public SynchronizeFormPrinter(WikiDataRequest<?> request, String title, String command, String actionParameterName, String inputParameterName) {
		_actionParameterName = actionParameterName;
		_command = command;
		_inputParameterName = inputParameterName;
		_request = request;
		_title = title;
	}

	@Override
	public void print(HtmlLinePrinter body) throws WikiException {
		// TODO Auto-generated method stub
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
