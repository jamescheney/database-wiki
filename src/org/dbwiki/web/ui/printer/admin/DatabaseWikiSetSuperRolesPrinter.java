package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.security.Role;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;


public class DatabaseWikiSetSuperRolesPrinter extends HtmlContentPrinter{
	private String _headline;
	private DatabaseWiki _wiki;
	private int _roleID;
	private String _action;
	private Role _role;
	
	
	public DatabaseWikiSetSuperRolesPrinter(String headline, String action, DatabaseWiki wiki, int roleID) {
		_headline = headline;
		_action = action;
		_wiki = wiki;
		_roleID = roleID;
		_role = _wiki.rolePolicy().getRole(_roleID);
	}

	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline + "\t- " + _role.getName(), CSS.CSSHeadline);
		
		
		printer.openFORM("SetSuperRoles", "POST", "/");
		
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("sub_role_id", _roleID + "");
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		
		printer.openTABLE(CSS.CSSFormContainer);
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
		
		//super role
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Is-Super-Role" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.closeTR();
		
		//content
		for(int tempRoleID : _wiki.rolePolicy().getRoleIDListing()) {
			
			if(tempRoleID > 0 && tempRoleID != _roleID ) {
				
				printer.openTR();
				printer.openTD(CSS.CSSFormText);
				printer.text(tempRoleID + "");
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(_wiki.rolePolicy().getRole(tempRoleID).getName());
				printer.closeTD();
				
				if(_wiki.rolePolicy().getRole(tempRoleID).hasSuperRole(_roleID) ) {
					printer.openTD(CSS.CSSFormControl);
					printer.addDisabledRADIOBUTTON("Yes", "role_" + tempRoleID, "yes", _role.hasSuperRole(tempRoleID));
					printer.addBR();
					printer.addDisabledRADIOBUTTON("No", "role_" + tempRoleID, "no", !_role.hasSuperRole(tempRoleID));
					printer.closeTD();
				} else {
					printer.openTD(CSS.CSSFormControl);
					printer.addRADIOBUTTON("Yes", "role_" + tempRoleID, "yes", _role.hasSuperRole(tempRoleID));
					printer.addBR();
					printer.addRADIOBUTTON("No", "role_" + tempRoleID, "no", !_role.hasSuperRole(tempRoleID));
					printer.closeTD();
				}
				printer.closeTR();
			}
		}
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		
		printer.closeFORM();
	}
}
