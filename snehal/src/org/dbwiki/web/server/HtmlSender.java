package org.dbwiki.web.server;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.dbwiki.web.html.HtmlPage;
import org.json.*;

/** A class providing static methods for sending HtmlPages to HttpExchanges.
 * 
 * @author jcheney
 *
 */
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
	
	/*
	 * Added new method to send SQL results
	 */
	public static void sendSQLData(String result, HttpExchange exchange) throws java.io.IOException {
		sendSQLData(result, exchange, HttpURLConnection.HTTP_OK);
	}
	
	public static void sendSQLData(String result, HttpExchange exchange, int responseCode) throws java.io.IOException {
		Headers responseHeaders = exchange.getResponseHeaders();
    	responseHeaders.set("Content-Type", "text/html");
    	exchange.sendResponseHeaders(responseCode, 0);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
    		out.write(result);
    		out.newLine();
    	out.close();
	}
	
	/*
	 * Added new method to send results in JSON format
	 */
	public static void sendJSON(JSONObject result, HttpExchange exchange) throws java.io.IOException {
		sendJSON(result, exchange, HttpURLConnection.HTTP_OK);
	}
	
	public static void sendJSON(JSONObject result, HttpExchange exchange, int responseCode) throws java.io.IOException {
		Headers responseHeaders = exchange.getResponseHeaders();
    	responseHeaders.set("Content-Type", "application/json");
    	exchange.sendResponseHeaders(responseCode, 0);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
    		out.write(result.toString());
    		out.newLine();
    	out.close();
	}
}
