package org.dbwiki.web.server;

import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletResponse;

import org.dbwiki.web.html.HtmlPage;

/** 
 * A class providing static methods for sending HtmlPages through HttpServlet.
 * @author o.cierny
 * 
 */

public class HtmlServletSender {
	public static void send(HtmlPage page, HttpServletResponse response) throws java.io.IOException {
		send(page, response, HttpURLConnection.HTTP_OK);
	}
	
	public static void send(HtmlPage page, HttpServletResponse response, int responseCode) throws java.io.IOException {
		response.setContentType("text/html");
    	response.setStatus(responseCode);
    	PrintWriter out = response.getWriter();
    	for (int iLine = 0; iLine < page.size(); iLine++) {
    		out.println(page.get(iLine));
    	}
	}
	
}
