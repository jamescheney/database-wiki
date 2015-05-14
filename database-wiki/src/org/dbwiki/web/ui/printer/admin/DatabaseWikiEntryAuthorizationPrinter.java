package org.dbwiki.web.ui.printer.admin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.server.Entry;
import org.dbwiki.data.security.DBPolicy;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/**
 * Generates a form that allows managing the entry-level access permissions
 */

public class DatabaseWikiEntryAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private UserListing _users;
	private int _user_id;
	private String _wiki;
	private Map<Integer, Entry> _entryListing;
	private Map<Integer,Map<Integer,DBPolicy>> _policyListing;

	public DatabaseWikiEntryAuthorizationPrinter(String _headline,String _action, UserListing _users, String _wiki, int _user_id, Map<Integer,Entry> _entryListing, Map<Integer,Map<Integer,DBPolicy>> _policyListing) {
		this._headline = _headline;
		this._action = _action;
		this._users = _users;
		this._wiki = _wiki;
		this._user_id = _user_id;
		this._entryListing = _entryListing;
		this._policyListing = _policyListing;
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
		// Sorting key set helps align form with update code, see WikiServer.getUpdateEntryAuthorizationResponseHandler
		ArrayList<Integer> keys = new ArrayList<Integer>(_entryListing.keySet());
		Collections.sort(keys);
		for(Integer i:keys){
			String entry_value = _entryListing.get(i).entry_value();
			int entry_id = _entryListing.get(i).entry_id();
			
			//
			// entry name
			//
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entry_value);
			printer.closeCENTER();
			printer.closeTD();
			
			boolean flag = false;
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
			}
			
			if(flag){
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyReadPermission, "HoldPermission", (_policyListing.get(policyKey).get(entry_id).capability().isRead() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyReadPermission, "NoPermission", (_policyListing.get(policyKey).get(entry_id).capability().isRead() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyInsertPermission, "HoldPermission", (_policyListing.get(policyKey).get(entry_id).capability().isInsert() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyInsertPermission, "NoPermission", (_policyListing.get(policyKey).get(entry_id).capability().isInsert() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyDeletePermission, "HoldPermission", (_policyListing.get(policyKey).get(entry_id).capability().isDelete() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyDeletePermission, "NoPermission", (_policyListing.get(policyKey).get(entry_id).capability().isDelete() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", entry_id+WikiServer.propertyUpdatePermission, "HoldPermission", (_policyListing.get(policyKey).get(entry_id).capability().isUpdate() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", entry_id+WikiServer.propertyUpdatePermission, "NoPermission", (_policyListing.get(policyKey).get(entry_id).capability().isUpdate() != WikiAuthenticator.HoldPermission));
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
		printer.addHIDDEN(WikiServer.ParameterName, _wiki);
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
