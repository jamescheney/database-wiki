package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiRoleAssignmentPrinter extends HtmlContentPrinter {
	private String _headline;
	private int _role_id;
	private DatabaseWiki _wiki;
	private UserListing _allUsers;
	private ArrayList<User> _searchedUsers;

	public DatabaseWikiRoleAssignmentPrinter(String headline, DatabaseWiki wiki, int role_id, UserListing allUsers, ArrayList<User> searchedUsers) {
		this._headline = headline;
		this._wiki = wiki;
		this._role_id = role_id;
		this._allUsers = allUsers;
		this._searchedUsers = searchedUsers;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		String roleName =  _wiki.rolePolicy().getRole(_role_id).getName();
		_headline += "\t- " + roleName;
		
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		printer.openFORM("ExitRoleAssignment", "POST", "/");
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionExitRoleAssignment, "<img src=\"/pictures/button.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		
		// table
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		// header
		printer.openTR();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("User ID");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Login Name");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Full Name");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Unassign");
		printer.closeTH();	
		
		printer.closeTR();
		
		ArrayList<Integer> users = _wiki.rolePolicy().getRole(_role_id).getUsers();
		
		for(Integer userID : users) {
			printer.openTR();
			
			printer.openTD(CSS.CSSFormControl);
			printer.text(userID + "");
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormControl);
			printer.text(_allUsers.get(userID).login());
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormControl);
			printer.text(_allUsers.get(userID).fullName());
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormControl);
			printer.openFORM("unassignUser", "POST", "/");
			printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
			printer.addHIDDEN("role_id", _role_id + "");
			printer.addHIDDEN("user_id", userID + "");
			printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionUnassignUser, "<img src=\"/pictures/button.gif\">");
			printer.closeFORM();
			printer.closeTD();
			
			printer.closeTR();
		}
		
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		
		printer.closeCENTER();
		printer.closePARAGRAPH();
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		printer.closePARAGRAPH();
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		
		printer.closePARAGRAPH();
		
		
		if(_searchedUsers != null) {
			System.out.println(_searchedUsers.size());
			// table
			printer.openTABLE(CSS.CSSFormContainer);
			printer.openTR();
			printer.openTD(CSS.CSSFormContainer);
			printer.openTABLE(CSS.CSSFormFrame);
			
			// header
			printer.openTR();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("User ID");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Login Name");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Full Name");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Assign");
			printer.closeTH();	
			
			printer.closeTR();
			
			for(User user : _searchedUsers) {
				printer.openTR();
				
				printer.openTD(CSS.CSSFormControl);
				printer.text(user.id() + "");
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.text(user.login());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.text(user.fullName());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.openFORM("addUser", "POST", "/");
				printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
				printer.addHIDDEN("role_id", _role_id + "");
				printer.addHIDDEN("user_id", user.id() + "");
				printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionAssignUser, "<img src=\"/pictures/button.gif\">");
				printer.closeFORM();
				printer.closeTD();
				
				printer.closeTR();
			}
			
			
			printer.closeTABLE();
			printer.closeTD();
			printer.closeTR();
			printer.closeTABLE();
			
		}
		
		printer.openFORM("searchUsers", "POST", "/");
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _role_id+"");
		printer.addREQUIREDTEXTBOX("keyword", "80", "");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionSearchUsers, "<img src=\"/pictures/button.gif\">");
		printer.closeFORM();

		printer.closeCENTER();
		printer.closePARAGRAPH();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
