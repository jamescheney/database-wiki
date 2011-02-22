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
package org.dbwiki.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;

import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.web.html.FatalExceptionPage;

import org.dbwiki.web.log.ServerLog;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Deprecated
public abstract class HttpRequestHandler implements HttpHandler {
	/*
	 * Private Variables
	 */
	
	private File _directory;
	private ServerLog _serverLog;
	
	
	
	/*
	 * Constructors
	 */
	
	public HttpRequestHandler(File directory, ServerLog serverLog) {
		_directory = directory;
		_serverLog = serverLog;
	}
	
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract void respondTo(HttpExchange exchange) throws java.io.IOException, org.dbwiki.exception.WikiException;
	public abstract void respondTo(HttpExchange exchange, WikiException exception) throws java.io.IOException;	
	
	/*
	 * Public Methods
	 */
	

	public File directory() {
		return _directory;
	}
	
	public void handle(HttpExchange exchange) throws java.io.IOException {
		try {
			String filename = exchange.getRequestURI().getPath();
			int pos = filename.lastIndexOf('.');
			if (pos != -1) {
				String suffix = filename.substring(pos);
				if (suffix.equalsIgnoreCase(".uu")) {
					this.sendFile(exchange, "application/octet-stream");
				} else if (suffix.equalsIgnoreCase(".exe")) {
					this.sendFile(exchange, "application/octet-stream");
				} else if (suffix.equalsIgnoreCase(".ps")) {
					this.sendFile(exchange, "application/postscript");
				} else if (suffix.equalsIgnoreCase(".zip")) {
					this.sendFile(exchange, "application/zip");
				} else if (suffix.equalsIgnoreCase(".sh")) {
					this.sendFile(exchange, "application/x-shar");
				} else if (suffix.equalsIgnoreCase(".tar")) {
					this.sendFile(exchange, "application/x-tar");
				} else if (suffix.equalsIgnoreCase(".snd")) {
					this.sendFile(exchange, "audio/basic");
				} else if (suffix.equalsIgnoreCase(".au")) {
					this.sendFile(exchange, "audio/basic");
				} else if (suffix.equalsIgnoreCase(".wav")) {
					this.sendFile(exchange, "audio/x-wav");
				} else if (suffix.equalsIgnoreCase(".gif")) {
					this.sendFile(exchange, "image/gif");
				} else if (suffix.equalsIgnoreCase(".jpg")) {
					this.sendFile(exchange, "image/jpeg");
				} else if (suffix.equalsIgnoreCase(".jpeg")) {
					this.sendFile(exchange, "image/jpeg");
				} else if (suffix.equalsIgnoreCase(".htm")) {
					this.sendFile(exchange, "text/html");
				} else if (suffix.equalsIgnoreCase(".html")) {
					this.sendFile(exchange, "text/html");
				} else if (suffix.equalsIgnoreCase(".text")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".c")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".cc")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".css")) {
					this.sendFile(exchange, "text/css");
				} else if (suffix.equalsIgnoreCase(".c++")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".h")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".pl")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".txt")) {
					this.sendFile(exchange, "text/plain");
				} else if (suffix.equalsIgnoreCase(".java")) {
					this.sendFile(exchange, "text/plain");
				} else {
					this.sendFile(exchange, "content/unknown");
				}
			} else {
				if (this.serverLog() != null) {
				//	this.serverLog().logRequest(exchange);
				}
				this.respondTo(exchange);
			}
		} catch (org.dbwiki.exception.WikiException wikiException) {
			wikiException.printStackTrace();
			this.respondTo(exchange, wikiException);
		} catch (Exception exception) {
			exception.printStackTrace();
			new FatalExceptionPage(exception).send(exchange);
		}
	}

	public ServerLog serverLog() {
		return _serverLog;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void sendFile(HttpExchange exchange, String contentType) throws java.io.IOException, org.dbwiki.exception.WikiException {
		File file = new File(this.directory().getAbsoluteFile() + exchange.getRequestURI().getPath());
		if ((file.exists()) && (!file.isDirectory())) {
	    	Headers responseHeaders = exchange.getResponseHeaders();
	    	responseHeaders.set("Content-Type", contentType);
	    	exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
	    	OutputStream os = exchange.getResponseBody();
	    	InputStream is = new FileInputStream(file.getAbsolutePath());
	    	int n;
	    	byte[] buf = new byte[2048];
	    	while ((n = is.read(buf)) > 0) {
	    		os.write(buf, 0, n);
	    	}
	    	is.close();
	    	os.close();
		} else {
			throw new WikiRequestException(WikiRequestException.FileNotFound, exchange.getRequestURI().getPath());
		}
	}
}

