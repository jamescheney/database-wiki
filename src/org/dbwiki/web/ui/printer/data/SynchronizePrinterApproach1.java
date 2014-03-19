package org.dbwiki.web.ui.printer.data;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class SynchronizePrinterApproach1 implements HtmlContentPrinter {
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
	
	public SynchronizePrinterApproach1(WikiDataRequest<?> request, String title, String command, String actionParameterName, String inputParameterName) {
		_actionParameterName = actionParameterName;
		_command = command;
		_inputParameterName = inputParameterName;
		_request = request;
		_title = title;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
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
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed both locally and remotely, would you like to pull remote copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedChanged", "true", true);
		body.addRADIOBUTTON("No (pushes local copy)", "changedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries deletedly locally but changed remotely, would you like to pull remote copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "deletedChanged", "true", true);
		body.addRADIOBUTTON("No (deletes them remotely)", "deletedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed locally but deleted remotely, would you like to delete the entry locally?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedDeleted", "true", true);
		body.addRADIOBUTTON("No (re-activates and changes them remotely)", "changedDeleted", "false", false);
		body.closeTD();
		body.closeTR();

		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries added locally, and idetical entries are added remotely, would you like to merge them?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "addedAdded", "true", true);
		body.addRADIOBUTTON("No ", "addedAdded", "false", false);
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
