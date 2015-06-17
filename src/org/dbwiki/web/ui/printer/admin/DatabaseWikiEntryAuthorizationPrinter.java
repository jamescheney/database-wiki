package org.dbwiki.web.ui.printer.admin;


import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.Capability;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/**
 * Generates a form that allows managing the entry-level access permissions
 */

/* FIXME: Use Database content function instead of passed-in map
 * FIXME: #security USe policy instead of passed-in map
 */
public class DatabaseWikiEntryAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private UserListing _users;
	private int _user_id;
	private DatabaseWiki _wiki;
	
	public DatabaseWikiEntryAuthorizationPrinter(String _headline,String _action, UserListing _users, DatabaseWiki _wiki, int _user_id) {
		this._headline = _headline;
		this._action = _action;
		this._users = _users;
		this._wiki = _wiki;
		this._user_id = _user_id;
	}
	

	/*
	 * Public Methods
	 */
	
	
	
	public void print(HtmlLinePrinter printer) throws WikiException {
	
		_headline += "\t-" + _users.get(_user_id).login();
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("manageEntryAuthenticationMode", "POST", "/");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancelEntryAuthorizationUpdate, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		//entry name
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Entry Name");
		printer.closeTH();
		
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
			
			/*boolean flag = false;
			int policyKey = 0;
			for(int key:_policyListing.keySet()){
				if (_user_id == key){
					Map<Integer,DBPolicy> map = _policyListing.get(key);
					for(Integer entryId : map.keySet()){
						if(entryId == entry_id){
							policyKey = key;
							flag = true;
							break;
						}
					}
				}
			}*/
			boolean flag = _wiki.policy().findEntry(_user_id, entry_id);
			
			if(flag){
				Capability cap = _wiki.policy().findEntryCapability(_user_id, entry_id);
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
		printer.addHIDDEN("user_id", _user_id+"");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancelEntryAuthorizationUpdate, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	
	}
		
}
