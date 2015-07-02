package org.dbwiki.web.ui.printer;

import java.net.URLEncoder;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;

public class LoginContentPrinter extends HtmlContentPrinter {

	private HttpRequest _request;

	public LoginContentPrinter(HttpRequest request) {
		_request = request;
	}
	
	@Override
	public void print(HtmlLinePrinter printer) throws WikiException {
		try {
			if (_request.user() != null) {
				String logoutRedirectLink = WikiServer.SpecialFolderLogout + "?" 
						+ RequestParameter.ParameterResource + "=" 
						+ URLEncoder.encode(_request.getRequestURI().toASCIIString(), "UTF-8");
				printer.openPARAGRAPH(CSS.CSSLogin);
				printer.add("You are currently logged in as");
				printer.addSPAN(_request.user().fullName(), CSS.CSSLogin);
				printer.link(logoutRedirectLink, "(Log out)", CSS.CSSLogin);
				printer.closePARAGRAPH();
							
			} else {
				String loginRedirectLink = WikiServer.SpecialFolderLogin + "?" 
											+ RequestParameter.ParameterResource + "=" 
											+ URLEncoder.encode(_request.getRequestURI().toASCIIString(), "UTF-8");
				printer.openPARAGRAPH(CSS.CSSLogin);
				printer.link(loginRedirectLink, "Login", CSS.CSSLogin);
				printer.closePARAGRAPH();
			}
		} catch (java.io.UnsupportedEncodingException uee) {
			throw(new WikiFatalException(uee));
		}
	}



}
