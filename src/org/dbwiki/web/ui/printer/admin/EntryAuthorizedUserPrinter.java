package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseEntry;
import org.dbwiki.data.security.Capability;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class EntryAuthorizedUserPrinter extends HtmlContentPrinter {
	private String _headline;
	private int _entryID;
	private DatabaseWiki _wiki;
	private UserListing _users;
	
	public EntryAuthorizedUserPrinter(String headline, DatabaseWiki wiki, int entryID, UserListing users) {
		this._headline = headline;
		this._wiki = wiki;
		this._entryID = entryID;
		this._users = users;
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		DatabaseEntry entry = _wiki.database().content().getByID(_entryID);
		
		printer.paragraph(_headline + "\t- " + entry.label(), CSS.CSSHeadline);
		
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryListing + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back to Entry Listing");
		printer.closeCENTER();
		
		// Entries
		printer.openTABLE(CSS.CSSFormContainer);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		printer.text("Users' authorities are obtained through role, and can not be changed directly. If you want to change any user's authorities, please do it by changing its role authorities");
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		
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
		
		printer.closeTR();
		
		for(int tempUserID : _wiki.rolePolicy().getUserIDListing()){
			Capability tempCap = _wiki.rolePolicy().getUserAuthority(tempUserID, entry.id());
			if(!tempCap.isNoneAccess()) {
				String tempUserName = _users.get(tempUserID).fullName();
				
				printer.openTR();
				printer.openTD(CSS.CSSFormText);
				printer.openCENTER();
				printer.text(tempUserID + "");
				printer.closeCENTER();
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.openCENTER();
				printer.text(tempUserName);
				printer.closeCENTER();
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", tempUserID + WikiServer.propertyReadCapability, "Yes", tempCap.getRead(), true);
				printer.addBR();
				printer.addRADIOBUTTON("No", tempUserID + WikiServer.propertyReadCapability, "No", !tempCap.getRead(), true);
				printer.closeTD();
				
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", tempUserID + WikiServer.propertyInsertCapability, "Yes", tempCap.getInsert(), true);
				printer.addBR();
				printer.addRADIOBUTTON("No", tempUserID + WikiServer.propertyInsertCapability, "No", !tempCap.getInsert(), true);
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", tempUserID + WikiServer.propertyDeleteCapability, "Yes", tempCap.getUpdate(), true);
				printer.addBR();
				printer.addRADIOBUTTON("No", tempUserID + WikiServer.propertyDeleteCapability, "No", !tempCap.getUpdate(), true);
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes",  tempUserID + WikiServer.propertyUpdateCapability, "Yes", tempCap.getDelete(), true);
				printer.addBR();
				printer.addRADIOBUTTON("No", tempUserID + WikiServer.propertyUpdateCapability, "No", !tempCap.getDelete(), true);
				printer.closeTD();
				
				printer.closeTR();
			}
		}
		
		printer.closeTABLE();
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryListing + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back to Entry Listing");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
