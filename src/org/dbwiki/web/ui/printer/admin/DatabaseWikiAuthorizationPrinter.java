package org.dbwiki.web.ui.printer.admin;

/**
 * Generates a form that allows managing the database-level access permissions
 */



import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.data.security.Capability;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;
import org.dbwiki.web.server.DatabaseWikiProperties;


public class DatabaseWikiAuthorizationPrinter extends HtmlContentPrinter {
	private String _headline;
	private String _action;
	private DatabaseWikiProperties _properties;
	private UserListing _user_listing;
	private DatabaseWiki _wiki;

	public DatabaseWikiAuthorizationPrinter(String headline, String action, DatabaseWikiProperties properties, UserListing user_listing, DatabaseWiki wiki) {
		this._headline = headline;
		this._action = action;
		this._user_listing = user_listing;
		this._wiki = wiki;
		this._properties = properties;
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
			
			
			
			if(_wiki.policy().find(id)){
				Capability capability = _wiki.policy().findCapability(id);

				//read permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyReadPermission, "HoldPermission", (capability.isRead()));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyReadPermission, "NoPermission", (!capability.isRead()));
				printer.closeTD();
				//insert permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyInsertPermission, "HoldPermission", (capability.isInsert()));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyInsertPermission, "NoPermission", (!capability.isInsert()));
				printer.closeTD();
				//delete permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyDeletePermission, "HoldPermission", (capability.isDelete()));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyDeletePermission, "NoPermission", (!capability.isDelete()));
				printer.closeTD();
				//update permission
				printer.openTD(CSS.CSSFormControl);
				printer.addRADIOBUTTON("Yes", login+WikiServer.propertyUpdatePermission, "HoldPermission", (capability.isUpdate()));
				printer.addBR();
				printer.addRADIOBUTTON("No", login+WikiServer.propertyUpdatePermission, "NoPermission", (!capability.isUpdate()));
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
