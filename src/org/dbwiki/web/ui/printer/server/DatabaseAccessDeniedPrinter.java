package org.dbwiki.web.ui.printer.server;


import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/**
 * An error page for ordinary users who make administrator request
 */

public class DatabaseAccessDeniedPrinter extends HtmlContentGenerator implements HtmlContentPrinter{

	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		// TODO Auto-generated method stub
		printer.paragraph("Denied.", CSS.CSSHeadline);
		printer.openFORM("", "POST", "/");
		printer.paragraph("Sorry, you are not an administrator. :(", CSS.CSSPageContent);
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeFORM();
	}

}
