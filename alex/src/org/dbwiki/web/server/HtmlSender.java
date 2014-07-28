package org.dbwiki.web.server;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import org.dbwiki.web.html.HtmlPage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

/** A class providing static methods for sending HtmlPages to HttpExchanges.
 *  TODO: Remove this in favor of Exchange.send.
 * @author jcheney
 *
 */
@SuppressWarnings("restriction") 
public class HtmlSender {
	public static void send(HtmlPage page, HttpExchange exchange) throws java.io.IOException {
		send(page, exchange, HttpURLConnection.HTTP_OK);
	}
	
	public static void send(HtmlPage page, HttpExchange exchange, int responseCode) throws java.io.IOException {
    	Headers responseHeaders = exchange.getResponseHeaders();
    	responseHeaders.set("Content-Type", "text/html");
    	exchange.sendResponseHeaders(responseCode, 0);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
    	for (int iLine = 0; iLine < page.size(); iLine++) {
    		out.write(page.get(iLine));
    		out.newLine();
    	}
    	out.close();
	}


	
}
