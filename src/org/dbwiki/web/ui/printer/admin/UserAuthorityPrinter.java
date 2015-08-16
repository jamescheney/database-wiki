package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.Capability;
import org.dbwiki.data.security.Permission;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class UserAuthorityPrinter extends HtmlContentPrinter {
	private DatabaseWiki _wiki;
	private User _user;
	
	public UserAuthorityPrinter(DatabaseWiki wiki, User user) {
		this._wiki = wiki;
		this._user = user;
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph("Check User Authorization- " + _user.login(), CSS.CSSHeadline);

		printer.openCENTER();
		printer.linkWithOnClick("", "javascript :history.back(-1);", "Go back", "");
		printer.closeCENTER();
		
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
		
		Permission permission = _wiki.rolePolicy().getUserPermission(_user.id());
		
		//read permission
		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyReadPermission, "1", permission.isPositiveRead());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyReadPermission, "-1", permission.isNegativeRead());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyReadPermission, "0", permission.isNeutralRead());
		printer.closeTD();
		//insert permission
		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyInsertPermission, "1", (permission.isPositiveInsert()));
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyInsertPermission, "-1", (permission.isNegativeInsert()));
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyInsertPermission, "0", (permission.isNeutralInsert()));
		printer.closeTD();
		//delete permission
		printer.openTD(CSS.CSSFormControl);
		printer.addDisabledRADIOBUTTON("Yes", WikiServer.propertyUpdatePermission, "1", permission.isPositiveUpdate());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("No", WikiServer.propertyUpdatePermission, "-1", permission.isNegativeUpdate());
		printer.addBR();
		printer.addDisabledRADIOBUTTON("By Entry", WikiServer.propertyUpdatePermission, "0", permission.isNeutralUpdate());
		printer.closeTD();
		//update permission
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
			String entryValue = entries.getByIndex(i).label();
			int entryID = entries.getByIndex(i).id();
			
			//
			// entry name
			//
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entryValue);
			printer.closeCENTER();
			printer.closeTD();
			
			Capability cap = _wiki.rolePolicy().getUserCapability(_user.id(), entryID);
			//read permission
			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyReadCapability, "Yes", (cap.getRead()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyReadCapability, "No", (!cap.getRead()));
			printer.closeTD();
			//insert permission
			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyInsertCapability, "Yes", (cap.getInsert()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyInsertCapability, "No", (!cap.getInsert()));				
			printer.closeTD();
			//delete permission
			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyUpdateCapability, "Yes", (cap.getDelete()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyUpdateCapability, "No", (!cap.getDelete()));
			printer.closeTD();
			//update permission
			printer.openTD(CSS.CSSFormControl);
			printer.addDisabledRADIOBUTTON("Yes", entryID+WikiServer.propertyDeleteCapability, "Yes", (cap.getUpdate()));
			printer.addBR();
			printer.addDisabledRADIOBUTTON("No", entryID+WikiServer.propertyDeleteCapability, "No", (!cap.getUpdate()));
			printer.closeTD();
		
			printer.closeTR();
		}
		
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openCENTER();
		printer.linkWithOnClick("", "javascript :history.back(-1);", "Go back", "");
		printer.closeCENTER();
	
	}
}
