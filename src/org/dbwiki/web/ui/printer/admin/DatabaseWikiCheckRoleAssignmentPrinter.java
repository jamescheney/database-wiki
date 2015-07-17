package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiCheckRoleAssignmentPrinter extends HtmlContentPrinter {
	private String _headline;
	private DatabaseWiki _wiki;
	private UserListing _users;

	public DatabaseWikiCheckRoleAssignmentPrinter(String headline, DatabaseWiki wiki, UserListing users) {
		this._headline = headline;
		this._wiki = wiki;
		this._users = users;
	}
	
	public void print(HtmlLinePrinter printer) {
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		//exit button
		printer.openFORM("BackToRoleManagement", "POST", "/");
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		
		//table
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		//user full name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("User");
		printer.closeTH();
		
		//roles
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Roles");
		printer.closeTH();
		
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.closePARAGRAPH();
		
		for(Integer userID : _wiki.rolePolicy().getUserIDListing()) {
			printer.openTABLE(CSS.CSSFormFrame);
			printer.openTR();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(_users.get(userID).fullName());
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			for(Integer roleID : _wiki.rolePolicy().getUserRoles(userID)) {
				printer.text(_wiki.rolePolicy().getRole(roleID).getName() + " <br>");
			}
			printer.closeCENTER();
			printer.closeTD();
			
			printer.closeTR();
			printer.closeTABLE();
			
			printer.openPARAGRAPH(CSS.CSSButtonLine);
			printer.closePARAGRAPH();
		}
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
