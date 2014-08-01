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
package org.dbwiki.web.request;

import java.util.StringTokenizer;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.request.parameter.RequestParameterList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;

public class HttpRequest {
	/*
	 * Public Constants
	 */
	
	public static final String CookiePropertyCopyBuffer = "Copy-URL";
	
	
	/*
	 * Private Variables
	 */

	private String _copyBuffer = null;
	private RequestType _type;
	private RequestURL _url;
	private User _user;

	
	/*
	 * Constructors
	 */
	
	public HttpRequest(RequestURL url, UserListing users) {
		_type = new RequestType(url);
		
		_url = url;

    	if (_url.getUsername() != null) {
    		_user = users.get(_url.getUsername());
    	} else {
    		_user = null;
    	}

		String cookie = _url.getCookie();
		if (cookie != null) {
			StringTokenizer tokens = new StringTokenizer(cookie, ";");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken().trim();
				if (token.startsWith(CookiePropertyCopyBuffer + "=")) {
					_copyBuffer = token.substring(CookiePropertyCopyBuffer.length() + 1);
				}
			}
		}
	}

	
	/*
	 * Public Methods
	 */
	
	public String copyBuffer() {
		return _copyBuffer;
	}

	public RequestParameterList parameters() {
		return _url.parameters();
	}
	
	public String toString() {
		return getRequestURI().toASCIIString();
	}
	
	public URI getRequestURI() {
		return _url.getRequestURI();
	}

	public RequestType type() {
		return _type;
	}
	
	public User user() {
		return _user;
	}
}
