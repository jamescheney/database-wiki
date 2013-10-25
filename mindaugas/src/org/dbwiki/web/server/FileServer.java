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

import org.dbwiki.web.html.FileNotFoundPage;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Configures a Web server/HttpHandler that responds to requests to a given directory and serves requested files from it. 
 * 
 * @author jcheney
 *
 */
public abstract class FileServer implements HttpHandler {
	/*
	 * Private Variables
	 */
	
	private File _directory;
	
	
	/*
	 * Constructors
	 */
	
	public FileServer(File directory) {
		_directory = directory;
	}
	
	
	/*
	 * Public Methods
	 */
	
	private String contentType(HttpExchange exchange) {
		String filename = exchange.getRequestURI().getPath();
		
		int pos = filename.lastIndexOf('.');
		if (pos != -1) {
			String suffix = filename.substring(pos);
			if (suffix.equalsIgnoreCase(".uu")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".exe")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".ps")) {
				return "application/postscript";
			} else if (suffix.equalsIgnoreCase(".zip")) {
				return "application/zip";
			} else if (suffix.equalsIgnoreCase(".sh")) {
				return "application/x-shar";
			} else if (suffix.equalsIgnoreCase(".tar")) {
				return "application/x-tar";
			} else if (suffix.equalsIgnoreCase(".snd")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".au")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".wav")) {
				return "audio/x-wav";
			} else if (suffix.equalsIgnoreCase(".gif")) {
				return "image/gif";
			} else if (suffix.equalsIgnoreCase(".jpg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".jpeg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".htm")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".html")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".text")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".c")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".cc")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".css")) {
				return "text/css";
			} else if (suffix.equalsIgnoreCase(".c++")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".h")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".pl")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".txt")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".java")) {
				return "text/plain";
			} else {
				return "content/unknown";
			}
		} else {
			return "content/unknown";
		}
	}
	
	public File directory() {
		return _directory;
	}

	public void sendData(HttpExchange exchange, String contentType, InputStream is) throws java.io.IOException {
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", contentType);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
		OutputStream os = exchange.getResponseBody();
		int n;
		byte[] buf = new byte[2048];
		while ((n = is.read(buf)) > 0) {
			os.write(buf, 0, n);
		}
		is.close();
		os.close();
	}
	
	public void sendFile(HttpExchange exchange) throws java.io.IOException {
		String contentType = this.contentType(exchange);
		
		String path = exchange.getRequestURI().getPath();
		
		File file = new File(_directory.getAbsolutePath() + path);
		if ((file.exists()) && (!file.isDirectory())) {
			this.sendData(exchange, contentType, new FileInputStream(file));
		} else {
			System.out.println("File Not Found: " + path);
			HtmlSender.send(new FileNotFoundPage(path),exchange);
		}
	}
	
	public void sendXML(HttpExchange exchange, InputStream is) throws java.io.IOException {
		this.sendData(exchange, "application/xml", is);
	}
	
	public void sendTXT(HttpExchange exchange, InputStream is) throws java.io.IOException {
		this.sendData(exchange, "text/plain", is);
	}
	
	public void sendJSON(HttpExchange exchange, InputStream is) throws java.io.IOException {
		this.sendData(exchange, "application/json", is);
	}
}
