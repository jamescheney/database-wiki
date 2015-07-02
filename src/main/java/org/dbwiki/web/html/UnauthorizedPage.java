package org.dbwiki.web.html;

import java.io.IOException;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServerConstants;
import org.dbwiki.web.ui.DatabaseWikiContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.printer.ExceptionPrinter;

public class UnauthorizedPage {
	
	public static void send(DatabaseWiki wiki, Exchange<?> exchange) throws IOException, WikiException {
		DatabaseWikiContentGenerator generator = new DatabaseWikiContentGenerator(new WikiDataRequest(wiki, 
				new RequestURL(exchange, wiki.identifier().linkPrefix())), wiki.getTitle(), wiki.cssLinePrinter());
		generator.put(DatabaseWikiContentGenerator.ContentContent, new ExceptionPrinter("Insufficient privileges to perform the requested operation"));
		exchange.send(HtmlTemplateDecorator.decorate(wiki.getContent(WikiServerConstants.RelConfigFileColFileTypeValTemplate),
				generator));
	}

}
