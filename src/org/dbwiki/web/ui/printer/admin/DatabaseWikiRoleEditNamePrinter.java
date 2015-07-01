package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiRoleEditNamePrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	//private DatabaseWikiProperties _properties;
	private int _role_id;
	private DatabaseWiki _wiki;

	public DatabaseWikiRoleEditNamePrinter(String headline, String action, DatabaseWiki wiki, int role_id) {
		this._headline = headline;
		this._action = action;
		//this._properties = properties;
		this._wiki = wiki;
		this._role_id = role_id;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		String roleName =  _wiki.rolePolicy().getRole(_role_id).getName();
		_headline += "\t- " + roleName;
		
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("editRoleName", "POST", "/");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		
		printer.closeCENTER();
		printer.closePARAGRAPH();
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		//printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _role_id+"");
		printer.closePARAGRAPH();
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		
		printer.closePARAGRAPH();
		printer.addREQUIREDTEXTBOX("new_role_name", "80", roleName);

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancelRoleNameUpdate, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
