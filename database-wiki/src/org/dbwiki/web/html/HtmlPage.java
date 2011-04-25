/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.web.html;

/*
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
*/

// This is basically just a renaming of LineSet; its functionality has been moved 
// to static methods in org.dbwiki.web.server.HtmlSender, in order to avoid 
// dependencies on com.sun.net.httpserver -- jcheney

public class HtmlPage extends LineSet {
	/*
	 * Public Methods
	 */
	/*
	public void send(HttpExchange exchange) throws java.io.IOException {
		this.send(exchange, HttpURLConnection.HTTP_OK);
	}
	
	public void send(HttpExchange exchange, int responseCode) throws java.io.IOException {
    	Headers responseHeaders = exchange.getResponseHeaders();
    	responseHeaders.set("Content-Type", "text/html");
    	exchange.sendResponseHeaders(responseCode, 0);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
    	for (int iLine = 0; iLine < this.size(); iLine++) {
    		out.write(this.get(iLine));
    		out.newLine();
    	}
    	out.close();
	}
	*/
}
