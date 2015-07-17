package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.Capability;
import org.dbwiki.data.security.Permission;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiRoleAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private int _roleID;
	private DatabaseWiki _wiki;
	
	public DatabaseWikiRoleAuthorizationPrinter(String headline,String action, DatabaseWiki wiki, int roleID) {
		this._headline = headline;
		this._action = action;
		this._wiki = wiki;
		this._roleID = roleID;
	}
	

	/*
	 * Public Methods
	 */
	
	
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		String roleName =  _wiki.rolePolicy().getRole(_roleID).getName();
		_headline += "\t- " + roleName;
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("RoleAuthorization", "POST", "/");
		
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();
		
		// Overall
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		
		//read permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read Permission");
		printer.closeTH();
		
		//insert permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Permission");
		printer.closeTH();
		
		//delete permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Permission");
		printer.closeTH();
		
		//update permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Permission");
		printer.closeTH();
		printer.closeTR();
		
		Permission permission = _wiki.rolePolicy().getRole(_roleID).getpermission();
		
		//read permission
		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyReadPermission, "1", (permission.positiveReadPermission()));
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyReadPermission, "-1", (permission.negativeReadPermission()));
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyReadPermission, "0", (permission.neutralReadPermission()));
		printer.closeTD();
		//insert permission
		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyInsertPermission, "1", (permission.positiveInsertPermission()));
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyInsertPermission, "-1", (permission.negativeInsertPermission()));
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyInsertPermission, "0", (permission.neutralInsertPermission()));
		printer.closeTD();
		//delete permission
		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyUpdatePermission, "1", (permission.positiveUpdatePermission()));
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyUpdatePermission, "-1", (permission.negativeUpdatePermission()));
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyUpdatePermission, "0", (permission.neutralUpdatePermission()));
		printer.closeTD();
		//update permission
		printer.openTD(CSS.CSSFormControl);
		printer.addRADIOBUTTON("Yes", WikiServer.propertyDeletePermission, "1", (permission.positiveDeletePermission()));
		printer.addBR();
		printer.addRADIOBUTTON("No", WikiServer.propertyDeletePermission, "-1", (permission.negativeDeletePermission()));
		printer.addBR();
		printer.addRADIOBUTTON("By Entry", WikiServer.propertyDeletePermission, "0", (permission.neutralDeletePermission()));
		printer.closeTD();
		
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		
		
		
		// Entries
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		//entry name
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Entry Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//read permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//insert permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//delete permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		//update permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Permission" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		printer.closeTR();
		//contents
		
		
		DatabaseContent entries = _wiki.database().content();
		
		// Sorting key set helps align form with update code, see WikiServer.getUpdateEntryAuthorizationResponseHandler
		for(int i = 0; i < entries.size(); i++){
			String entry_value = entries.get(i).label();
			int entry_id = entries.get(i).id();
			
			//
			// entry name
			//
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entry_value);
			printer.closeCENTER();
			printer.closeTD();
			
			boolean flag = _wiki.rolePolicy().getRole(_roleID).isCapabilityExist(entry_id);
			
			if(flag){
				Capability cap = _wiki.rolePolicy().getRole(_roleID).getCapability(entry_id);
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyReadPermission, "HoldPermission", (cap.isRead()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyReadPermission, "NoPermission", (!cap.isRead()));
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyInsertPermission, "HoldPermission", (cap.isInsert()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyInsertPermission, "NoPermission", (!cap.isInsert()));
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyDeletePermission, "HoldPermission", (cap.isDelete()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyDeletePermission, "NoPermission", (!cap.isDelete()));
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyUpdatePermission, "HoldPermission", (cap.isUpdate()));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyUpdatePermission, "NoPermission", (!cap.isUpdate()));
				printer.closeTD();
			}else{
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyReadPermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyReadPermission, "NoPermission", true);
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyInsertPermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyInsertPermission, "NoPermission", true);
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyDeletePermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyDeletePermission, "NoPermission", true);
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyUpdatePermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyUpdatePermission, "NoPermission", true);
				printer.closeTD();
				
			}
			printer.closeTR();
		}
		
		printer.closeTABLE();
		//find  name of the database here
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("role_id", _roleID+"");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addRealBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit",
				"action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	
	}
}
