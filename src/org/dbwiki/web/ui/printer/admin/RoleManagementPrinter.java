package org.dbwiki.web.ui.printer.admin;

import java.util.Date;

import org.dbwiki.data.security.Role;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class RoleManagementPrinter extends HtmlContentPrinter {
	private String _headline;
	private User _user;
	private DatabaseWiki _wiki;

	public RoleManagementPrinter(String headline, User user, DatabaseWiki wiki) {
		this._headline = headline;
		this._user = user;
		this._wiki = wiki;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		printer.openCENTER();
		printer.link("?edit=" + _wiki.id(), "Go back");
		printer.closeCENTER();
		
		printer.openTABLE(CSS.CSSFormContainer);
		
		// table 1===================================
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		//role id
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role&nbsp;ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//role name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role&nbsp;Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//role authorization
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Authorization" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//role assignment
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Assignment" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//edit role name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Edit&nbsp;name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//set super roles
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Inheritance" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//delete role
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete");
		printer.closeTH();
		
		printer.closeTR();
		
		//content
		for(int role_id : _wiki.rolePolicy().getRoleIDListing()) {
			if(role_id > 0) {
				printer.openTR();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(role_id + "");
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(_wiki.rolePolicy().getRole(role_id).getName());
				printer.closeTD();
				
				// Authorization
				printer.openTD(CSS.CSSFormControl);
				printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleAuthorization + "&" 
						+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
						+ RequestParameter.ParameterRoleID + "=" + role_id, ">>>");
				printer.closeTD();
				
				// Assignment
				printer.openTD(CSS.CSSFormControl);
				printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleAssignment + "&" 
						+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
						+ RequestParameter.ParameterRoleID + "=" + role_id, ">>>");
				printer.closeTD();
				
				// Edit name
				printer.openTD(CSS.CSSFormControl);
				printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleEditName + "&" 
						+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
						+ RequestParameter.ParameterRoleID + "=" + role_id, ">>>");
				printer.closeTD();
				
				// inheritance
				printer.openTD(CSS.CSSFormControl);
				printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleInheritance + "&" 
						+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
						+ RequestParameter.ParameterRoleID + "=" + role_id, ">>>");
				printer.closeTD();
				
				// Delete
				printer.openTD(CSS.CSSFormControl);
				printer.openFORM("DeleteRole"+role_id, "POST", "/");
				printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
				printer.addHIDDEN("role_id", role_id+"");
				printer.addAlterBUTTON("submit", "action", RequestParameterAction.DeleteRole, "<img src=\"/pictures/button_ok.gif\">", "Are you sure you want to delete this role permanently?");
				printer.closeFORM();
				printer.closeTD();
				
				printer.closeTR();
			}
		}
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		// end of table 1=======================================
		
		// table 2===============================================
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		printer.openTH();
		printer.text("Add a new Role:");
		printer.closeTH();
		
		printer.openTD();
		printer.openFORM("AddNewRole", "POST", "/");
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addRequiredTEXTBOX("new_role_name", "120", "", "enter role name");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.AddRole, "<img src=\"/pictures/button_ok.gif\">");
		printer.closeFORM();
		printer.closeTD();	
		
		printer.closeTR();
		
		printer.openTD();
		printer.text("&nbsp;&nbsp;&nbsp;");
		printer.closeTD();
		
		printer.openTR();
		
		printer.closeTR();
		
		printer.openTR();
		
		printer.openTH();
		printer.text("Advanced Setting:");
		printer.closeTH();
		
		printer.openTD();
		if(_user.is_admin()) {
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleAssignment + "&" 
					+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
					+ RequestParameter.ParameterRoleID + "=" + Role.ownerID, "Set Owner");
			printer.text("&nbsp;&nbsp;&nbsp;");
		}
		
		if(_user.is_admin() || (!_wiki.rolePolicy().getUserRoles(_user.id()).isEmpty() || _wiki.rolePolicy().getUserRoles(_user.id()).contains(0))) {
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleAssignment + "&" 
					+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
					+ RequestParameter.ParameterRoleID + "=" + Role.assistantID, "Set Assistants");
			printer.text("&nbsp;&nbsp;&nbsp;<br>");
		}
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleMutex + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Set Mutex Role");
		printer.text("&nbsp;&nbsp;&nbsp;");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.DBWikiUserRoleMapping + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Users");
		printer.text("&nbsp;&nbsp;&nbsp;");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryListing + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Entries");
		
		printer.closeTD();
		
		printer.closeTR();
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		// end of table 2=======================================================
		
		printer.closeTABLE();
	}
}
