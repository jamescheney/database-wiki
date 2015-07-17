package org.dbwiki.web.ui.printer.server;


import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/**
 * An error page for ordinary users who make administrator request
 */

public class DatabaseAccessDeniedPrinter extends HtmlContentPrinter {

    @Override
    public void print(HtmlLinePrinter printer) throws WikiException {
        printer.paragraph("Denied", CSS.CSSExceptionHeadline);
        printer.openFORM("", "POST", "/");
        printer.paragraph("Insufficient privileges to perform the requested operation", CSS.CSSExceptionText);
        printer.openCENTER();
        printer.addRealBUTTON("submit", "action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
        printer.closeCENTER();
        printer.closeFORM();
    }

}

