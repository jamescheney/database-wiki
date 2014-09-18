package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/**
 * Generates a form that allows managing the user information and identities
 */

public class DatabaseWikiUserListingPrinter extends HtmlContentPrinter {
	
	public static final int MessageNone                 = -1;
	public static final int MessageNoFullName           = 1;
	
	private String _headline;
	private UserListing _user_listing;
	private String _action;
	private int _message;
	private ArrayList<Integer[]> _probs;

	public DatabaseWikiUserListingPrinter(UserListing _user_listing,String _action,String _headline) {
		this._headline = _headline;
		this._user_listing = _user_listing;
		this._action = _action;
		_probs = null;
	}
	
	public DatabaseWikiUserListingPrinter(UserListing _user_listing,String _action,String _headline, ArrayList<Integer[]> _probs) {
		this._headline = _headline;
		this._user_listing = _user_listing;
		this._action = _action;
		this._probs = _probs;
	}
	
	

	/*
	 * Public Methods
	 */
	
	
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openFORM("frmUpdateUsers", "POST", "/");
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();

		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		printer.openTABLE(CSS.CSSFormFrame);
		
		//
		// User ID
		//
		printer.openTR();
		printer.openTH(CSS.CSSFormLabel);
		printer.text("User ID");
		printer.closeTH();
		
		//
		// Login Name
		//
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Login Name");
		printer.closeTH();
		
		//
		// Full Name
		//
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Full Name");
		printer.closeTH();
		
		//
		// Identity
		//
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Administrator");
		printer.closeTH();
		printer.closeTR();
		
		//content
		int i=1;
		while(_user_listing.get(i)!=null){
			_message = MessageNone;
			//
			// User ID
			//
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text(_user_listing.get(i).id()+"");
			printer.closeTD();
			
			//
			// Login Name
			//
			printer.openTD(CSS.CSSFormLabel);
			printer.text(_user_listing.get(i).login());
			printer.closeTD();
			//
			// Full Name
			//
			if(_probs != null){
				for(int iUser=0;iUser<_probs.size();iUser++){
					Integer[] prob = _probs.get(iUser);
					int user_index = prob[0];
					int message = prob[1];
					if(user_index == i && message == MessageNoFullName){
						_message = message;
						break;
					}
				}
			}
			if(_message == MessageNoFullName){
				printer.openTD(CSS.CSSFormMessage);
				System.out.print("no full name");
				printer.text("Please enter a valid name.");
				printer.addBR();
				printer.addTEXTBOX(_user_listing.get(i).fullName(), "300", _user_listing.get(i).fullName());
				printer.closeTD();
			}else{
				printer.openTD(CSS.CSSFormLabel);
				printer.addTEXTBOX(_user_listing.get(i).fullName(), "300", _user_listing.get(i).fullName());
				printer.closeTD();
			}
			
			//
			// Identity
			//
			printer.openTD(CSS.CSSFormControl);
			if(_user_listing.get(i).is_admin()){
				printer.addRADIOBUTTON("yes",_user_listing.get(i)+"" , "admin", true);
				printer.addRADIOBUTTON("no", _user_listing.get(i)+"", "not_admin", false);
			}else{
				printer.addRADIOBUTTON("yes", _user_listing.get(i)+"", "admin", false);
				printer.addRADIOBUTTON("no", _user_listing.get(i)+"", "not_admin", true);
			}
			printer.closeTD();
			printer.closeTR();
			i++;
		}
		printer.closeTABLE();

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
		
}
