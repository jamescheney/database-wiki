package org.dbwiki.web.ui.printer.data;

import org.dbwiki.main.SynchronizeDatabaseWiki;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class SynchronizePrinter implements HtmlContentPrinter {
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
	
	public SynchronizePrinter(WikiDataRequest<?> request, String title, String command, String actionParameterName, String inputParameterName) {
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
		body.text("If there are entries added remotely, would you like to add them locally?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteAdded", "true", true);
		body.addRADIOBUTTON("No", "remoteAdded", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed remotely, would you like to pull remote copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteChanged", "true", true);
		body.addRADIOBUTTON("No", "remoteChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries deleted remotely, would you like to delete locally?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteDeleted", "true", true);
		body.addRADIOBUTTON("No", "remoteDeleted", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed both locally and remotely, would you like to pull remote copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedChanged", "true", true);
		body.addRADIOBUTTON("No", "changedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries deletedly locally but changed remotely, would you like to pull remote copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "deletedChanged", "true", true);
		body.addRADIOBUTTON("No", "deletedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed locally but deleted remotely, would you like to delete the entry locally?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedDeleted", "true", true);
		body.addRADIOBUTTON("No", "changedDeleted", "false", false);
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
