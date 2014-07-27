package org.dbwiki.web.ui.printer.admin;

import java.util.Vector;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.server.Authorization;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;
import org.dbwiki.web.ui.printer.server.DatabaseWikiProperties;


public class DatabaseWikiAuthenticationPrinter implements HtmlContentPrinter {
	private String _headline;
	private String _action;
	private UserListing _user_listing;
	private Vector<Authorization> _authorizationListing;

	public DatabaseWikiAuthenticationPrinter(String _headline,String _action, UserListing _user_listing, Vector<Authorization> _authorizationListing) {
		this._headline = _headline;
		this._action = _action;
		this._user_listing = _user_listing;
		this._authorizationListing = _authorizationListing;
	}
	

	/*
	 * Public Methods
	 */
	
	
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("manageAuthenticationMode", "POST", "/");

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		//user login name
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Login Name");
		printer.closeTH();
		
		//read permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Read Permission");
		printer.closeTD();
		printer.closeTH();
		
		//insert permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Insert Permission");
		printer.closeTD();
		printer.closeTH();
		
		//delete permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Delete Permission");
		printer.closeTD();
		printer.closeTH();
		
		//update permission
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Update Permission");
		printer.closeTD();
		printer.closeTH();
		
		//content
		int i=1;
		while(_user_listing.get(i)!=null){
			String login = _user_listing.get(i).login();
			
			//
			// User Login Name
			//
			printer.openTR();
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(login);
			printer.closeCENTER();
			printer.closeTD();
			
			boolean flag = false;
			int j = 0;
			for(j = 0; j<_authorizationListing.size();j++){
				String user_login = _authorizationListing.get(j).user_login();
				if(user_login.equals(login)){
					flag = true;
					break;
				}
			}
			
			if(flag){
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyReadPermission, "HoldPermission", (_authorizationListing.get(j).is_read() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyReadPermission, "NoPermission", (_authorizationListing.get(j).is_read() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyInsertPermission, "HoldPermission", (_authorizationListing.get(j).is_insert() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyInsertPermission, "NoPermission", (_authorizationListing.get(j).is_insert() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyDeletePermission, "HoldPermission", (_authorizationListing.get(j).is_delete() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyDeletePermission, "NoPermission", (_authorizationListing.get(j).is_delete() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyUpdatePermission, "HoldPermission", (_authorizationListing.get(j).is_update() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyUpdatePermission, "NoPermission", (_authorizationListing.get(j).is_update() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				
			}else{
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyReadPermission, "HoldPermission", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyReadPermission, "NoPermission", false);
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyInsertPermission, "HoldPermission", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyInsertPermission, "NoPermission", false);
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyDeletePermission, "HoldPermission", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyDeletePermission, "NoPermission", false);
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyUpdatePermission, "HoldPermission", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyUpdatePermission, "NoPermission", false);
				printer.closeTD();
			}
			i++;
		}
		
		printer.closeTABLE();

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancelAuthorizationUpdate, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
		
}
