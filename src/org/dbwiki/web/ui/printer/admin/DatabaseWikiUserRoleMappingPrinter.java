package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class DatabaseWikiUserRoleMappingPrinter extends HtmlContentPrinter {
	private String _headline;
	private DatabaseWiki _wiki;
	private UserListing _users;

	public DatabaseWikiUserRoleMappingPrinter(String headline, DatabaseWiki wiki, UserListing users) {
		this._headline = headline;
		this._wiki = wiki;
		this._users = users;
	}
	
	public void print(HtmlLinePrinter printer) {
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
		
		//table
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		printer.openTR();
		
		//user full name
		printer.openTH(CSS.CSSFormLabel);
		printer.text("User");
		printer.closeTH();
		
		//roles
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Roles");
		printer.closeTH();
		
		//authorization
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Authority");
		printer.closeTH();
		
		printer.closeTR();
		
		for(Integer userID : _wiki.rolePolicy().getUserIDListing()) {
			printer.openTR();
			
			printer.openTD(CSS.CSSFormText);
			//printer.openCENTER();
			printer.text(_users.get(userID).fullName());
			//printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			//printer.openCENTER();
			for(Integer roleID : _wiki.rolePolicy().getUserRoles(userID)) {
				printer.text(_wiki.rolePolicy().getRole(roleID).getName() + " <br>");
			}
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.UserAuthority + "&"
					+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&"
					+ RequestParameter.ParameterUserID + "=" + userID, ">>>");
			printer.closeCENTER();
			printer.closeTD();
			printer.closeTR();
			
			
			printer.openPARAGRAPH(CSS.CSSButtonLine);
			printer.closePARAGRAPH();
		}
		printer.closeTABLE();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
