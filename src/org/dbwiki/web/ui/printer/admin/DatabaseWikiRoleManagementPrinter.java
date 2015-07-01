package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.exception.WikiException;
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

	private DatabaseWikiProperties _properties;
	private DatabaseWiki _wiki;

	public DatabaseWikiRoleManagementPrinter(String headline, DatabaseWikiProperties properties, DatabaseWiki wiki) {
		this._headline = headline;

		this._wiki = wiki;
		this._properties = properties;
	}

	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		// Exit button
		printer.openFORM("ExitRoleManagement", "POST", "/");
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
		printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionExitRoleManagement, "<img src=\"/pictures/button.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		//role name
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role Name");
		printer.closeTH();
		
		//role authorization
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Authorization");
		printer.closeTH();
		
		//role assignment
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Assignment");
		printer.closeTH();
		
		//edit role name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Edit name");
		printer.closeTH();
		
		//delete role
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete");
		printer.closeTH();
		
		printer.closeTR();
		
		//content
		for(int role_id : _wiki.rolePolicy().getRoleIDListing()) {
			
			printer.openFORM("DeleteRole"+role_id, "POST", "/");
			printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
			printer.addHIDDEN("role_id", role_id+"");
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(_wiki.rolePolicy().getRole(role_id).getName());
			printer.closeCENTER();
			printer.closeTD();
			
			// Authorization
			printer.openTD(CSS.CSSFormControl);
			printer.link("?"+RequestParameter.ParameterRoleAuthorization + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _properties.getName(), ">>>");
			printer.closeTD();
			
			// Assignment
			printer.openTD(CSS.CSSFormControl);
			printer.link("?"+RequestParameter.ParameterRoleAssignment + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _properties.getName(), ">>>");
			printer.closeTD();
			
			// Edit name
			printer.openTD(CSS.CSSFormControl);
			printer.link("?"+RequestParameter.ParameterRoleEditName + "=" + role_id + "&"+ RequestParameter.ParameterRoleManagement + "=" + _properties.getName(), ">>>");
			printer.closeTD();
			
			// Delete
			printer.openTD(CSS.CSSFormControl);
			printer.addREALBUTTON("submit",
					"action", RequestParameterAction.ActionDeleteRole, "<img src=\"/pictures/button.gif\">");
			printer.closeTD();
			
			printer.closeFORM();
		}
		
		printer.closeTABLE();
		
		printer.openFORM("AddNewRole", "POST", "/");
		
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		
		printer.addREQUIREDTEXTBOX("new_role_name", "80", "");
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.addREALBUTTON("submit", "action", RequestParameterAction.ActionAddRole, "<img src=\"/pictures/button.gif\">");
		printer.closeCENTER();
		
		printer.closeFORM();
		
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
