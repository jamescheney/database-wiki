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
package org.dbwiki.web.log;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;


//import com.sun.net.httpserver.Headers;
//import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.net.InetSocketAddress;

import javax.servlet.http.HttpServletRequest;

/** An abstract class for logs used to report server events.
 * 
 * @author jcheney
 *
 */
public abstract class ServerLog {
	/*
	 * Abstract Methods
	 */
	
	public abstract void closeLog() throws java.io.IOException;
	public abstract void openLog() throws java.io.IOException;
	public abstract void writeln(String line) throws java.io.IOException;
	
	// TODO: Make logging a request a metnod of Exchange<T>?
	
	/*
	 * Public Methods
	 */
	
	/*
	 * public synchronized void logRequest(HttpExchange exchange) {
	 
		try {
			this.openLog();
			this.writeln("Date = [" + new java.util.Date() + "]");
			this.writeln("URI = [" + exchange.getRequestURI().toString() + "]");
			this.writeln("Remote-IP = [" + exchange.getRemoteAddress().toString() + "]");
	    	Headers requestHeaders = exchange.getRequestHeaders();
	    	Set<String> keySet = requestHeaders.keySet();
	    	Iterator<String> iter = keySet.iterator();
	    	while (iter.hasNext()) {
	    		String key = iter.next();
	    		List<String> values = requestHeaders.get(key);
	    		this.writeln(key + " = " + values.toString());
	    	}
	    	this.writeln("--");
	    	this.closeLog();
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
	}
	*/
	
	/** Logs a server request @requestURI targeting @remoteAddress and with @requestHeaders, a map from strings to lists of strings. 
	 * @param requestURI - the request URI
	 * @param remoteAddress - the remote IP address
	 * @param requestHeaders - the headers passed in the HTTP request 
	 */
	public synchronized void logRequest(URI requestURI, InetSocketAddress remoteAddress, Map<String,List<String>> requestHeaders) {
		try {
			this.openLog();
			this.writeln("Date = [" + new java.util.Date() + "]");
			this.writeln("URI = [" + requestURI.toString() + "]");
			this.writeln("Remote-IP = [" + remoteAddress.toString() + "]");
	    	Set<String> keySet = requestHeaders.keySet();
	    	Iterator<String> iter = keySet.iterator();
	    	while (iter.hasNext()) {
	    		String key = iter.next();
	    		List<String> values = requestHeaders.get(key);
	    		this.writeln(key + " = " + values.toString());
	    	}
	    	this.writeln("--");
	    	this.closeLog();
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
	}
	
	/** Servlet request log wrapper
	 * @param request
	 */
	public synchronized void logRequest(HttpServletRequest request) {
		try {
			this.openLog();
			this.writeln("Date = [" + new java.util.Date() + "]");
			this.writeln("URI = [" + request.getRequestURI() + "]");
			this.writeln("Remote-IP = [" + request.getRemoteAddr() + ":" + request.getRemotePort() + "]");
	    	Enumeration<?> names = request.getHeaderNames();
	    	while (names.hasMoreElements()) {
	    		String key = names.nextElement().toString();
	    		Enumeration<?> values = request.getHeaders(key);
	    		this.writeln(key + " = " + values.toString());
	    	}
	    	this.writeln("--");
	    	this.closeLog();
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
	}
}
