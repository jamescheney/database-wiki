package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;

import org.dbwiki.data.security.Role;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class RoleAssignmentPrinter extends HtmlContentPrinter {
	private String _headline;
	private int _role_id;
	private DatabaseWiki _wiki;
	private UserListing _allUsers;
	private ArrayList<User> _searchedUsers;
	private Role _role;

	public RoleAssignmentPrinter(String headline, DatabaseWiki wiki, int role_id, UserListing allUsers, ArrayList<User> searchedUsers) {
		this._headline = headline;
		this._wiki = wiki;
		this._role_id = role_id;
		this._allUsers = allUsers;
		this._searchedUsers = searchedUsers;
		this._role = _wiki.rolePolicy().getRole(_role_id);
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		_headline += " :<br>" + _role.getName();
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
		
		printer.openTABLE(CSS.CSSFormContainer);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		
		printer.openTR();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("User-ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Login-Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Full-Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Unassign" + "&nbsp;&nbsp;&nbsp;");
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
			printer.addAlterBUTTON("submit", "action", RequestParameterAction.UnassignUser, "<img src=\"/pictures/button_ok.gif\">", "Are you sure you want to unassign permanently?");
			printer.closeFORM();
			printer.closeTD();
			
			printer.closeTR();
		}
		
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		
		
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
			printer.openTR();
			printer.openTD(CSS.CSSFormContainer);
			printer.openTABLE(CSS.CSSFormFrame);
			
			// header
			printer.openTR();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("User ID"+ "&nbsp;&nbsp;&nbsp;");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Login Name" + "&nbsp;&nbsp;&nbsp;");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Full Name" + "&nbsp;&nbsp;&nbsp;");
			printer.closeTH();
			
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Assign" + "&nbsp;&nbsp;&nbsp;");
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
				printer.addRealBUTTON("submit", "action", RequestParameterAction.AssignUser, "<img src=\"/pictures/button_ok.gif\">");
				printer.closeFORM();
				printer.closeTD();
				
				printer.closeTR();
			}
			
			
			printer.closeTABLE();
			printer.closeTD();
			printer.closeTR();		
		}
		
		
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		printer.openTH();
		printer.text("Search Roles:");
		printer.closeTH();
		
		printer.openTD();
		printer.openFORM("searchUsers", "POST", "/");
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _role_id+"");
		printer.addRequiredTEXTBOX("keyword", "120", "", "enter role name");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.SearchUsers, "<img src=\"/pictures/button_ok.gif\">");
		printer.closeFORM();
		printer.closeTD();
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		
		printer.closeTABLE();
	}
}
