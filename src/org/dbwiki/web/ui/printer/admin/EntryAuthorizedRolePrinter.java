package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseEntry;
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

public class EntryAuthorizedRolePrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private int _entryID;
	private DatabaseWiki _wiki;
	
	public EntryAuthorizedRolePrinter(String headline,String action, DatabaseWiki wiki, int entryID) {
		this._headline = headline;
		this._action = action;
		this._wiki = wiki;
		this._entryID = entryID;
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		DatabaseEntry entry = _wiki.database().content().getByID(_entryID);
		
		printer.paragraph(_headline + "\t- " + entry.label(), CSS.CSSHeadline);

		printer.openFORM("EntryAuthorizedRole", "POST", "/");
		
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryListing + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back to Entry Listing");
		printer.closeCENTER();
		
		// Entries
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Role Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Capability" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Remark" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.closeTR();
		
		for(int tempRoleID : _wiki.rolePolicy().getRoleIDListing()){
			Capability tempCap = _wiki.rolePolicy().getRoleAuthority(tempRoleID, entry.id());
			if(tempRoleID != Role.ownerID && tempRoleID != Role.assistantID) {
				String tempRoleName = _wiki.rolePolicy().getRole(tempRoleID).getName();
				Capability inheritedCap = _wiki.rolePolicy().getSuperRoleCapability(tempRoleID, entry.id());
				Permission tempRolePer = _wiki.rolePolicy().getRole(tempRoleID).getpermission();
				
				printer.openTR();
				printer.openTD(CSS.CSSFormText);
				printer.openCENTER();
				printer.text(tempRoleID + "");
				printer.closeCENTER();
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.openCENTER();
				printer.text(tempRoleName);
				printer.closeCENTER();
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", WikiServer.propertyReadCapability + tempRoleID, "Yes", tempCap.getRead(), inheritedCap.getRead() || !tempRolePer.isNeutralRead());
				printer.addBR();
				printer.addRADIOBUTTON("No", WikiServer.propertyReadCapability + tempRoleID, "No", !tempCap.getRead(), inheritedCap.getRead() || !tempRolePer.isNeutralRead());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", WikiServer.propertyInsertCapability + tempRoleID, "Yes", tempCap.getInsert(), inheritedCap.getInsert() || !tempRolePer.isNeutralInsert());
				printer.addBR();
				printer.addRADIOBUTTON("No", WikiServer.propertyInsertCapability + tempRoleID, "No", !tempCap.getInsert(), inheritedCap.getInsert() || !tempRolePer.isNeutralInsert());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", WikiServer.propertyUpdateCapability + tempRoleID, "Yes", tempCap.getUpdate(), inheritedCap.getUpdate() || !tempRolePer.isNeutralUpdate());
				printer.addBR();
				printer.addRADIOBUTTON("No", WikiServer.propertyUpdateCapability + tempRoleID, "No", !tempCap.getUpdate(), inheritedCap.getUpdate() || !tempRolePer.isNeutralUpdate());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes",  WikiServer.propertyDeleteCapability + tempRoleID, "Yes", tempCap.getDelete(), inheritedCap.getDelete() || !tempRolePer.isNeutralUpdate());
				printer.addBR();
				printer.addRADIOBUTTON("No", WikiServer.propertyDeleteCapability + tempRoleID, "No", !tempCap.getDelete(), inheritedCap.getDelete() || !tempRolePer.isNeutralDelete());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				if(!inheritedCap.isNoneAccess()) {
					printer.addIMG("/pictures/annotation.gif", "If capability is inherited from super roles, it can not be changed directly.");
				} else if(!tempRolePer.isNeutralRead() || !tempRolePer.isNeutralInsert() || !tempRolePer.isNeutralUpdate() || !tempRolePer.isNeutralUpdate()) {
					printer.addIMG("/pictures/annotation.gif", "If permission is not neutral(set as by entry), capability can not be changed directly.");
				}
				printer.closeTD();
				
				printer.closeTR();
			}
		}
		
		printer.closeTABLE();
		
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addHIDDEN("entry_id", entry.id() + "");
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addRealBUTTON("submit", "action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryListing + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back to Entry Listing");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.closeFORM();
	
	}
}
