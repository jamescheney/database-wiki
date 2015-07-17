package org.dbwiki.web.ui.printer.admin;

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
import org.dbwiki.web.server.DatabaseWikiProperties;

public class DatabaseWikiRoleManagementPrinter extends HtmlContentPrinter {
	private String _headline;
	private User _user;
	private DatabaseWiki _wiki;

	public DatabaseWikiRoleManagementPrinter(String headline, User user, DatabaseWikiProperties properties, DatabaseWiki wiki) {
		this._headline = headline;
		this._user = user;
		this._wiki = wiki;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		// Exit button
		printer.openFORM("BackToDatabaseSetting", "POST", "/");
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToDatabaseSetting, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		
		
		printer.openTABLE(CSS.CSSFormContainer);
		
		// table 1===================================
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		//role id
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role-ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//role name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role-Name" + "&nbsp;&nbsp;&nbsp;");
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
		printer.text("Edit-name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//set super roles
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Super-roles" + "&nbsp;&nbsp;&nbsp;");
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
				printer.link("?"+RequestParameter.ParameterRoleAuthorization + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _wiki.name(), ">>>");
				printer.closeTD();
				
				// Assignment
				printer.openTD(CSS.CSSFormControl);
				printer.link("?"+RequestParameter.ParameterRoleAssignment + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _wiki.name(), ">>>");
				printer.closeTD();
				
				// Edit name
				printer.openTD(CSS.CSSFormControl);
				printer.link("?"+RequestParameter.ParameterRoleEditName + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _wiki.name(), ">>>");
				printer.closeTD();
				
				// super roles
				printer.openTD(CSS.CSSFormControl);
				printer.link("?"+RequestParameter.ParameterSetSuperRoles + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _wiki.name(), ">>>");
				printer.closeTD();
				
				// Delete
				printer.openTD(CSS.CSSFormControl);
				printer.openFORM("DeleteRole"+role_id, "POST", "/");
				printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
				printer.addHIDDEN("role_id", role_id+"");
				printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionDeleteRole, "<img src=\"/pictures/button_ok.gif\">");
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
		
		printer.openFORM("AddNewRole", "POST", "/");
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		
		printer.openTD();
		printer.addRequiredTEXTBOX("new_role_name", "120", "", "enter role name");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionAddRole, "<img src=\"/pictures/button_ok.gif\">");
		printer.closeTD();
		
		printer.closeFORM();
		
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
		
		if(_wiki.rolePolicy().getUserRoles(_user.id()).contains(0) || _user.is_admin()) {
			printer.link("?"+RequestParameter.ParameterRoleAssignment + "=" + Role.assistantID + "&"+ RequestParameter.ParameterRoleManagement + "=" + _wiki.name(), "Manage Assistant");
			printer.text("&nbsp;&nbsp;&nbsp;");
		}
		printer.link("?"+RequestParameter.Type + "=" + RequestParameter.SetMutexRolePairs + "&"+ RequestParameter.DBName + "=" + _wiki.name(), "Set Mutex Role Pairs");
		printer.text("&nbsp;&nbsp;&nbsp;");
		printer.link("?" + RequestParameter.ParameterDBWikiCheckAssignment + "=" + _wiki.name(), "Check All Assignment");
		
		printer.closeTD();
		
		printer.closeTR();
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		// end of table 2=======================================================
		
		printer.closeTABLE();
	}
}
