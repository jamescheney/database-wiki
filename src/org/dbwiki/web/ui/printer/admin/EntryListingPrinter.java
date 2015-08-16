package org.dbwiki.web.ui.printer.admin;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

public class EntryListingPrinter extends HtmlContentPrinter {
	private String _headline;
	private DatabaseWiki _wiki;
	
	public EntryListingPrinter(String headline, DatabaseWiki wiki) {
		this._headline = headline;
		this._wiki = wiki;
	}
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline, CSS.CSSHeadline);

		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();
		
		// Entries
		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		printer.openTABLE(CSS.CSSFormFrame);
		
		//entry name
		printer.openTR();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Entry ID" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Entry Name" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Authorized&nbsp;Role" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.openTH(CSS.CSSFormLabel);
		printer.text("Authorized&nbsp;User" + "&nbsp;&nbsp;&nbsp;");
		printer.closeTH();
		
		printer.closeTR();
		
		
		DatabaseContent entries = _wiki.database().content();
		
		for(int i = 0; i < entries.size(); i++){
			String entry_name = entries.getByIndex(i).label();
			int entry_id = entries.getByIndex(i).id();
			
			printer.openTR();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entry_id+"");
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.text(entry_name);
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryAuthorizedRole + "&" 
					+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
					+ RequestParameter.ParameterEntryID + "=" + entry_id, ">>>");
			printer.closeCENTER();
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.openCENTER();
			printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.EntryAuthorizedUser + "&" 
					+ RequestParameter.ParameterDBName + "=" + _wiki.name() + "&" 
					+ RequestParameter.ParameterEntryID + "=" + entry_id, ">>>");
			printer.closeCENTER();
			printer.closeTD();
			
			
			printer.closeTR();
		}
		
		printer.closeTABLE();
		
		printer.openCENTER();
		printer.link("?" + RequestParameter.ParameterType + "=" + RequestParameter.RoleManagement + "&" 
				+ RequestParameter.ParameterDBName + "=" + _wiki.name(), "Go back");
		printer.closeCENTER();


		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();

	
	}
}
