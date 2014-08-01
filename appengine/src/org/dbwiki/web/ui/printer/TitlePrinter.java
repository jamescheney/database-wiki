package org.dbwiki.web.ui.printer;

import java.util.Vector;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;

public class TitlePrinter extends HtmlContentPrinter {
	private String _title;
	
	public TitlePrinter(String title) {
		_title = title;
	}
	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.add(_title);
	}
	
	public void print(HtmlLinePrinter printer,Vector<String> args) throws WikiException {
		if (args != null) {
		printer.add(args.get(0) + " - " + _title);
		} else {
			print(printer);
		}
	}

}
