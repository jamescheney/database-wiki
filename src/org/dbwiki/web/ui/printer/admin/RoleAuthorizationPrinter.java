package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.Capability;
import org.dbwiki.data.security.Permission;
import org.dbwiki.data.security.Role;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class RoleAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private int _roleID;
	private DatabaseWiki _wiki;
	private Role _role;
	
	public RoleAuthorizationPrinter(String headline,String action, DatabaseWiki wiki, int roleID) {
		this._headline = headline;
		this._action = action;
		this._wiki = wiki;
		this._roleID = roleID;
		this._role = _wiki.rolePolicy().getRole(_roleID);
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline + "\t- " + _role.getName(), CSS.CSSHeadline);

		printer.openFORM("RoleAuthorization", "POST", "/");
		
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
		printer.closePARAGRAPH();
		
		if(_role.hasSuperRole()) {
			printer.openTABLE(CSS.CSSFormContainer);
			printer.openTR();
			printer.openTD(CSS.CSSFormContainer);
			printer.openTABLE(CSS.CSSFormFrame);
			printer.text("As \"" + _role.getName() + "\" is sub role of ");
			for(int tempID : _role.getSuperRoles())  {
				printer.text("\"" + _wiki.rolePolicy().getRole(tempID).getName() + "\",");
			}
			printer.text(" it has all positive permissions and capabilities of its super role(s). Click ");
			printer.link("?"+RequestParameter.ParameterType + "=" + RequestParameter.RoleEventualAuthority + "&"
			+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&"
			+ RequestParameter.ParameterRoleID + "=" + _roleID, "here");
			printer.text(" to check eventual authorization of \"" + _role.getName() + "\"");
			printer.closeTABLE();
			printer.closeTD();
			printer.closeTR();
			printer.closeTABLE();
		}
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read Permission");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Permission");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Permission");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Permission");
		printer.closeTH();
		
		printer.closeTR();
		
		Permission permission = _role.getpermission();
		

		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyReadPermission, "1", permission.isPositiveRead());
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyReadPermission, "-1", permission.isNegativeRead());
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyReadPermission, "0", permission.isNeutralRead());
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyInsertPermission, "1", (permission.isPositiveInsert()));
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyInsertPermission, "-1", (permission.isNegativeInsert()));
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyInsertPermission, "0", (permission.isNeutralInsert()));
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyUpdatePermission, "1", permission.isPositiveUpdate());
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyUpdatePermission, "-1", permission.isNegativeUpdate());
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyUpdatePermission, "0", permission.isNeutralUpdate());
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyDeletePermission, "1", permission.isPositiveDelete());
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyDeletePermission, "-1", permission.isNegativeDelete());
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyDeletePermission, "0", permission.isNeutralDelete());
		printer.closeTD();
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
	
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Entry Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();

		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read&nbsp;Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();

		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert&nbsp;Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update&nbsp;Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();

		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete&nbsp;Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.closeTR();

		DatabaseContent entries = _wiki.database().content();

		for(int i = 0; i < entries.size(); i++){
			String entry_value = entries.getByIndex(i).label();
			int entry_id = entries.getByIndex(i).id();
			
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entry_value);
			printer.closeCENTER();
			printer.closeTD();
			
			boolean flag = _role.isCapabilityExist(entry_id);
			
			if(flag){
				Capability cap = _role.getCapability(entry_id);
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyReadCapability, "Yes", (cap.getRead()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyReadCapability, "No", (!cap.getRead()));
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyInsertCapability, "Yes", (cap.getInsert()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyInsertCapability, "No", (!cap.getInsert()));
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyUpdateCapability, "Yes", (cap.getUpdate()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyUpdateCapability, "No", (!cap.getUpdate()));
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyDeleteCapability, "Yes", (cap.getDelete()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyDeleteCapability, "No", (!cap.getDelete()));
				printer.closeTD();
				
			}else{
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyReadCapability, "Yes", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyReadCapability, "No", true);
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyInsertCapability, "Yes", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyInsertCapability, "No", true);
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyUpdateCapability, "Yes", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyUpdateCapability, "No", true);
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyDeleteCapability, "Yes", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyDeleteCapability, "No", true);
				printer.closeTD();
				
			}
			printer.closeTR();
		}
		
		printer.closeTABLE();
		
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _roleID + "");
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.closeFORM();
	
	}
}
