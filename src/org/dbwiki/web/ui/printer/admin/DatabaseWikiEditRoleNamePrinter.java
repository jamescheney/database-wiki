package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiEditRoleNamePrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private int _role_id;
	private DatabaseWiki _wiki;

	public DatabaseWikiEditRoleNamePrinter(String headline, String action, DatabaseWiki wiki, int role_id) {
		this._headline = headline;
		this._action = action;
		this._wiki = wiki;
		this._role_id = role_id;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		String roleName =  _wiki.rolePolicy().getRole(_role_id).getName();
		_headline += "\t- " + roleName;
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		printer.openFORM("editRoleName", "POST", "/");
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _role_id+"");
		
		printer.openTABLE(CSS.CSSFormContainer);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);

		printer.openTR();
		
		printer.openTH();
		printer.text("New-Role-Name:" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTD();
		
		printer.addRequiredTEXTBOX("new_role_name", "120", roleName, "");
		printer.closeTD();
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.closePARAGRAPH();
		
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		
		printer.closeFORM();
		

	}
}
