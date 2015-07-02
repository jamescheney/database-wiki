package org.dbwiki.web.ui.printer;

import java.util.Vector;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.ui.CSS;

public class DatabaseLinkPrinter  extends HtmlContentPrinter {
	private String _title;
	private DatabaseIdentifier _dbIdentifier;
	public DatabaseLinkPrinter(String title,  DatabaseIdentifier dbIdentifier ) {
		_title = title;
		_dbIdentifier = dbIdentifier;
	}
	
	@Override
	public void print(HtmlLinePrinter printer, Vector<String> args) throws WikiException {
		String title = null;
		if (args != null) {
			if (args.size() > 0) {
				title = args.get(0);
			} else {
				title = "";
			}
		} else {
			title = _title;
		}
		
		
		printer.openPARAGRAPH(CSS.CSSDatabaseHomeLink);
		printer.link(_dbIdentifier.databaseHomepage(), title, CSS.CSSDatabaseHomeLink);  
		printer.closePARAGRAPH();
	}

	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		print(printer,null);		
	}	

}
