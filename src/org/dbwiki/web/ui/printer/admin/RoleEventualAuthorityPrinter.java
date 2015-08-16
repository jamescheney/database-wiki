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

public class RoleEventualAuthorityPrinter extends HtmlContentPrinter {
	private String _headline;
	private int _roleID;
	private DatabaseWiki _wiki;
	private Role _role;
	
	public RoleEventualAuthorityPrinter(String headline, DatabaseWiki wiki, int roleID) {
		this._headline = headline;
		this._wiki = wiki;
		this._roleID = roleID;
		this._role = _wiki.rolePolicy().getRole(_roleID);
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline + "\t- " + _role.getName(), CSS.CSSHeadline);

		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleAuthorization + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
				+ RequestParameter.ParameterRoleID + "=" + _roleID, "Go back");
		printer.closeCENTER();
		
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
		
		Permission permission = _wiki.rolePolicy().getRoleEventualPermission(_roleID);
		
		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyReadPermission, "1", permission.isPositiveRead());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyReadPermission, "-1", permission.isNegativeRead());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyReadPermission, "0", permission.isNeutralRead());
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyInsertPermission, "1", (permission.isPositiveInsert()));
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyInsertPermission, "-1", (permission.isNegativeInsert()));
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyInsertPermission, "0", (permission.isNeutralInsert()));
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyUpdatePermission, "1", permission.isPositiveUpdate());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyUpdatePermission, "-1", permission.isNegativeUpdate());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyUpdatePermission, "0", permission.isNeutralUpdate());
		printer.closeTD();

		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyDeletePermission, "1", permission.isPositiveDelete());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyDeletePermission, "-1", permission.isNegativeDelete());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyDeletePermission, "0", permission.isNeutralDelete());
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
		printer.text("Read Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		printer.closeTR();
		
		DatabaseContent entries = _wiki.database().content();
		
		for(int i = 0; i < entries.size(); i++){
			String entryValue = entries.getByIndex(i).label();
			int entryID = entries.getByIndex(i).id();
			
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entryValue);
			printer.closeCENTER();
			printer.closeTD();
			
			Capability cap = _wiki.rolePolicy().getRoleEventualCapability(_roleID, entryID);
			
			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyReadCapability, "Yes", (cap.getRead()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyReadCapability, "No", (!cap.getRead()));
			printer.closeTD();

			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyInsertCapability, "Yes", (cap.getInsert()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyInsertCapability, "No", (!cap.getInsert()));				
			printer.closeTD();

			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyUpdateCapability, "Yes", (cap.getUpdate()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyUpdateCapability, "No", (!cap.getUpdate()));
			printer.closeTD();

			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyDeleteCapability, "Yes", (cap.getDelete()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyDeleteCapability, "No", (!cap.getDelete()));
			printer.closeTD();
		
			printer.closeTR();
		}
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
	
	}
}
