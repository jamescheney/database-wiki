package org.dbwiki.web.ui.printer.data;

import java.net.UnknownHostException;
import java.net.InetAddress;

import org.dbwiki.main.SynchronizeDatabaseWiki;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class PushToRemotePrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private String _actionParameterName;
	private String _command;
	private String _inputParameterName;
	private WikiDataRequest<?> _request;
	private String _title;
	private String _localPort;
	
	
	/*
	 * Constructors
	 */
	
	public PushToRemotePrinter(WikiDataRequest<?> request, String title, String command, String actionParameterName,
			String inputParameterName, String localPort) {
		_actionParameterName = actionParameterName;
		_command = command;
		_inputParameterName = inputParameterName;
		_request = request;
		_title = title;
		_localPort = localPort;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.paragraph(_title, CSS.CSSHeadline);
		
		body.addBR();
		body.addBR();

		body.openFORM("frmInput", "POST", "http://192.168.1.68:8080/CIAWFB/31A5");
		body.addHIDDEN(_actionParameterName, "");

		body.openCENTER();
		body.openTABLE(CSS.CSSInputForm);
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text(_command);
		body.closePARAGRAPH();
		body.text("<textarea name=\"" + RequestParameter.ParameterRemoteAddr + "\" style=\"width: 99%; height:3.2em;\" id=\""
				+ RequestParameter.ParameterRemoteAddr + "\">" + "http://192.168.1.67:8080" + "</textarea>");		
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.closePARAGRAPH();
		body.text("<textarea name=\"" + "localport" + "\" style=\"display:none;\">" + _localPort + "</textarea>");		
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries added locally, would you like to add them remotely?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteAdded", "true", true);
		body.addRADIOBUTTON("No", "remoteAdded", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed locally, would you like to push local copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteChanged", "true", true);
		body.addRADIOBUTTON("No", "remoteChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries deleted locally, would you like to delete remotely?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "remoteDeleted", "true", true);
		body.addRADIOBUTTON("No", "remoteDeleted", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed both remotely and locally, would you like to push local copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedChanged", "true", true);
		body.addRADIOBUTTON("No", "changedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries deletedly remotely but changed locally, would you like to push local copy?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "deletedChanged", "true", true);
		body.addRADIOBUTTON("No", "deletedChanged", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.openTD(CSS.CSSInputForm);
		body.openPARAGRAPH(CSS.CSSInputForm);
		body.text("If there are entries changed remotely but deleted locally, would you like to delete the entry remotely?");
		body.closePARAGRAPH();
		body.addRADIOBUTTON("Yes", "changedDeleted", "true", true);
		body.addRADIOBUTTON("No", "changedDeleted", "false", false);
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeCENTER();
		
		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();
		body.addBUTTONwithOnClick("image", "button", "/pictures/button_ok.gif", "javascript:this.form.action=document.getElementById('"
				+ RequestParameter.ParameterRemoteAddr + "').value + window.location.pathname;");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		body.closePARAGRAPH();
		body.closeFORM();
	}
}
