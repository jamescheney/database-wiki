package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class WikiServerCheckRoleAssignmentPrinter extends HtmlContentPrinter {
	private String _headline;
	private User _user;
	private HashMap<Integer, HashMap<String, ArrayList<String>>> _roleAssignment;

	public WikiServerCheckRoleAssignmentPrinter(String headline, User user, HashMap<Integer, HashMap<String, ArrayList<String>>> roleAssignment) {
		this._headline = headline;
		this._user = user;
		this._roleAssignment = roleAssignment;
	}
	
	public void print(HtmlLinePrinter printer) {
		printer.paragraph(_headline + " - " + _user.fullName(), CSS.CSSHeadline);
		
		//exit button
		printer.openFORM("BackToUserListing", "POST", "/");
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToUserListing, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		//user full name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Database");
		printer.closeTH();
		
		//roles
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Roles");
		printer.closeTH();
		
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.closePARAGRAPH();
		
		for(String dbName : _roleAssignment.get(_user.id()).keySet()) {
			printer.openTABLE(CSS.CSSFormFrame);
			printer.openTR();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(dbName);
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			for(String roleName : _roleAssignment.get(_user.id()).get(dbName)) {
				printer.text(roleName + " <br>");
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
