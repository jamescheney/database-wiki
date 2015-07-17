package org.dbwiki.web.ui.printer.admin;

import java.util.ArrayList;

import org.dbwiki.data.security.Pair;
import org.dbwiki.data.security.RolePolicy;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;


public class DatabaseWikiSetMutexRolePairsPrinter extends HtmlContentPrinter{
	private String _headline;
	private DatabaseWiki _wiki;
	RolePolicy _rolePolicy;
	
	public DatabaseWikiSetMutexRolePairsPrinter(String headline, DatabaseWiki wiki) {
		_headline = headline;
		_wiki = wiki;
		_rolePolicy = _wiki.rolePolicy();
	}

	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		
		printer.paragraph(_headline, CSS.CSSHeadline);
		
		// Exit button
		printer.openFORM("BackToRoleManagement", "POST", "/");
		printer.openCENTER();
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionBackToRoleManagement, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closeFORM();
		

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		
		printer.openTH();
		printer.text("Role1-ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH();
		printer.text("Role1-Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH();
		printer.text("Role2-ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH();
		printer.text("Role2-Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH();
		printer.text("Delete");
		printer.closeTH();
		printer.closeTR();
		
		
		ArrayList<Pair<Integer, Integer>> conflictRolePairs = _wiki.rolePolicy().getMutexRolePairs();
		if(conflictRolePairs != null) {
			for(Pair<Integer, Integer> pair : conflictRolePairs) {	
				printer.openTR();
				int role1ID = pair.getLeft(), role2ID = pair.getRight();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(role1ID + "");
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(_wiki.rolePolicy().getRole(role1ID).getName());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(role2ID + "");
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.text(_wiki.rolePolicy().getRole(role2ID).getName());
				printer.closeTD();
				
				printer.openTD(CSS.CSSFormText);
				printer.openFORM("DeleteMutexRolePair_" + role1ID + "_" + role2ID, "POST", "/");
				printer.addHIDDEN("role1_id", role1ID + "");
				printer.addHIDDEN("role2_id", role2ID + "");
				printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
				printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionDeleteMutex, "<img src=\"/pictures/button_ok.gif\">");
				printer.closeFORM();
				printer.closeTD();
				
				
				printer.closeTR();
			}
		}

		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
		
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		printer.openFORM("AddMutex", "POST", "/");
		
		// add mutex head
		printer.openTR();
		
		// role1 head
		printer.openTH();
		printer.text("Role1-ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		// role2 head
		printer.openTH();
		printer.text("Role1-Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		// add button head
		printer.openTH();
		printer.text("Add" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.closeTR();
		
		// add mutex content
		printer.openTR();
		
		// role1
		printer.openTH();
		printer.openSELECT("select_role1");
		for(int roleID : _rolePolicy.getRoleIDListing()) {
			if(roleID > 0) {
				printer.addOPTION(_rolePolicy.getRole(roleID).getName(), roleID+"", false);
			}
		}
		printer.closeSELECT();
		printer.closeTH();
		
		// role2
		printer.openTH();
		printer.openSELECT("select_role2");
		for(int roleID : _rolePolicy.getRoleIDListing()) {
			if(roleID > 0) {
				printer.addOPTION(_rolePolicy.getRole(roleID).getName(), roleID+"", false);
			}
		}
		printer.closeSELECT();
		printer.closeTH();
		
		printer.openTH();
		
		printer.addHIDDEN(WikiServer.ParameterName, _wiki.name());
		printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionAddMutex, "<img src=\"/pictures/button_ok.gif\">");
		
		printer.closeTH();
		
		printer.closeTR();
		printer.closeFORM();
		printer.closeTABLE();
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
