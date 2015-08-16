package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class WikiServerUserRoleMappingPrinter extends HtmlContentPrinter {
	private String _headline;
	private User _user;
	private HashMap<Integer, HashMap<String, ArrayList<String>>> _roleAssignment;

	public WikiServerUserRoleMappingPrinter(String headline, User user, HashMap<Integer, HashMap<String, ArrayList<String>>> roleAssignment) {
		this._headline = headline;
		this._user = user;
		this._roleAssignment = roleAssignment;
	}
	
	public void print(HtmlLinePrinter printer) {
		printer.paragraph(_headline + " - " + _user.fullName(), CSS.CSSHeadline);
		
		printer.openCENTER();
		printer.linkWithOnClick("", "javascript :history.back(-1);", "Go back", "");
		printer.closeCENTER();
		
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);

		for(String dbName : _roleAssignment.get(_user.id()).keySet()) {
			printer.openTABLE(CSS.CSSFormFrame);
			printer.openTR();
			
			//user full name
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Database");
			printer.closeTH();
			
			//roles
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Roles");
			printer.closeTH();
			
			//Authorization
			printer.openTH(CSS.CSSFormLabel);
			printer.text("Authority");
			printer.closeTH();
			
			printer.closeTR();
			
			printer.openTR();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(dbName);
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			for(String roleName : _roleAssignment.get(_user.id()).get(dbName)) {
				printer.text(roleName + " <br>");
			}
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.UserAuthority + "&"
					+ RequestParameter.ParameterDBName + "=" + dbName + "&"
					+ RequestParameter.ParameterUserID + "=" + _user.id(), ">>>");
			printer.closeCENTER();
			printer.closeTD();
			
			printer.closeTR();
			printer.closeTABLE();
			
			printer.openPARAGRAPH(CSS.CSSButtonLine);
			printer.closePARAGRAPH();
		}
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
