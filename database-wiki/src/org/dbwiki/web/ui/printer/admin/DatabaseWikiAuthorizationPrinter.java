package org.dbwiki.web.ui.printer.admin;

import java.util.Vector;
/**
 * Generates a form that allows managing the database-level access permissions
 */

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.data.security.Authorization;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;
import org.dbwiki.web.server.DatabaseWikiProperties;


public class DatabaseWikiAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private DatabaseWikiProperties _properties;
	private UserListing _user_listing;
	private Vector<Authorization> _authorizationListing;

	public DatabaseWikiAuthorizationPrinter(String _headline,String _action, DatabaseWikiProperties _properties, UserListing _user_listing, Vector<Authorization> _authorizationListing) {
		this._headline = _headline;
		this._action = _action;
		this._user_listing = _user_listing;
		this._authorizationListing = _authorizationListing;
		this._properties = _properties;
	}
	

	/*
	 * Public Methods
	 */
	
	
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("manageAuthenticationMode", "POST", "/");
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancelAuthorizationUpdate, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();
		
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
		
		//Manage by each entry
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Manage By Entries");
		printer.closeTH();
		printer.closeTR();
		
		//content
		int i=1;
		while(_user_listing.get(i)!=null){
			String login = _user_listing.get(i).login();
			int id = _user_listing.get(i).id();
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
				//String user_login = _authorizationListing.get(j).user_login();
				int user_id= _authorizationListing.get(j).user_id();
				String database_name = _authorizationListing.get(j).database_name();
				if(user_id == id && database_name.equals(_properties.getName())){
					flag = true;
					break;
				}
			}
			
			if(flag){
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyReadPermission, "HoldPermission", (_authorizationListing.get(j).capability().isRead() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyReadPermission, "NoPermission", (_authorizationListing.get(j).capability().isRead() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyInsertPermission, "HoldPermission", (_authorizationListing.get(j).capability().isInsert() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyInsertPermission, "NoPermission", (_authorizationListing.get(j).capability().isInsert() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyDeletePermission, "HoldPermission", (_authorizationListing.get(j).capability().isDelete() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyDeletePermission, "NoPermission", (_authorizationListing.get(j).capability().isDelete() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyUpdatePermission, "HoldPermission", (_authorizationListing.get(j).capability().isUpdate() == WikiAuthenticator.HoldPermission));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyUpdatePermission, "NoPermission", (_authorizationListing.get(j).capability().isUpdate() != WikiAuthenticator.HoldPermission));
				printer.closeTD();
				//manage by entry
				printer.openTD(CSS.CSSFormControl);
				printer.link("?"+RequestParameter.ParameterEntryAuthorization + "=" + id + "&"+ RequestParameter.ParameterAuthorization + "=" + _properties.getName(), ">>>");
				printer.closeTD();
				
			}else{
				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyReadPermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyReadPermission, "NoPermission", true);
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyInsertPermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyInsertPermission, "NoPermission", true);
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyDeletePermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyDeletePermission, "NoPermission", true);
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyUpdatePermission, "HoldPermission", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyUpdatePermission, "NoPermission", true);
				printer.closeTD();
				//manage by entry
				printer.openTD(CSS.CSSFormControl);
				printer.link("?"+RequestParameter.ParameterEntryAuthorization + "=" + id + "&&" + RequestParameter.ParameterAuthorization + "=" +_properties.getName(), ">>>");
				printer.closeTD();
			}
			printer.closeTR();
			i++;
		}
		
		printer.closeTABLE();
		printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
		printer.closePARAGRAPH();
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
