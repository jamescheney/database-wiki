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
package org.dbwiki.web.security;


import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;

import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.WikiServer;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

/** An authenticator class for a DatabaseWiki 
 * 
 * @author jcheney
 *
 */

@SuppressWarnings("restriction")
public class WikiAuthenticator extends Authenticator {
	/*
	 * Public Constants
	 */
	
	

	
	/*
	 * Private Variables
	 */
	
	private int _mode;
	private String _realm;
	private UserListing _users;
	
	
	/*
	 * Constructors
	 */
	
	public WikiAuthenticator(String realm, int mode, UserListing users) {
		_mode = mode;
		_realm = realm;
		_users = users;
	}
	
	
	/*
	 * Public Methods
	 */
	
	private boolean allowedFileRequest(String path ) {
		// If the request is for a file (currently indicated by
		// a '.' in the request path), then no authorization is
		// required.
		// FIXME #security: Security hole? Generalize the test for whether something is a file.
		// System.err.println(path);
		return path.indexOf('.') != -1;
	}
	
	public synchronized Result authenticate(HttpExchange exchange) {

		
		// FIXME: #security: If the request is to log in then we should check the username and password no matter what!
		// Currently we don't if we happen to be at a page that doesn't require authentication.
		
		if (allowedFileRequest(exchange.getRequestURI().getPath())) {
			return new Authenticator.Success(new HttpPrincipal("", _realm));
		}
		
		Headers rmap = exchange.getRequestHeaders();
		
		boolean isProtectedRequest = this.isProtectedRequest(exchange);
		
		String auth = rmap.getFirst("Authorization");
		if (auth == null) {
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways)
					|| ((_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && (isProtectedRequest))
					|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
				Headers map = exchange.getResponseHeaders();
				map.set("WWW-Authenticate", "Basic realm=" + "\"" + _realm + "\"");
				return new Authenticator.Retry(401);
			} else {
				return new Authenticator.Success(new HttpPrincipal(User.UnknownUserName, _realm));
			}
		} else {
			int sp = auth.indexOf(' ');
			if (sp == -1 || !auth.substring(0, sp).equals("Basic")) {
				return new Authenticator.Failure(401);
			}
			byte[] b = new Base64().base64ToByteArray(auth.substring(sp + 1));
			String userpass = new String(b);
			int colon = userpass.indexOf(':');
			String uname = userpass.substring(0, colon);
			String pass = userpass.substring(colon + 1);
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways) 
					|| ((_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && (isProtectedRequest))
					|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
				if (checkCredentials(uname, pass)) {
					return new Authenticator.Success(new HttpPrincipal(uname, _realm));
				} else {
					Headers map = exchange.getResponseHeaders();
					map.set("WWW-Authenticate", "Basic realm=" + "\"" + _realm	+ "\"");
					return new Authenticator.Failure(401);
				}
			} else {
				return new Authenticator.Success(new HttpPrincipal(uname, _realm));
			}
		}
	}

	public synchronized int getAuthenticationMode() {
		return _mode;
	}
	
	public String getRealm() {
		return _realm;
	}
	
	public synchronized void setAuthenticationMode(int value) {
		_mode = value;
	}
	
	
	/*
	 * Private Methods
	 */
	
	/**
	 * Check whether password supplied by user claiming to be username is correct.
	 * FIXME #security: Avoid plaintext passwords!
	 */
	private boolean checkCredentials(String username, String password) {
		if (!_users.isEmpty()) {
			if (_users.contains(username)) {
				return _users.get(username).password().equals(password);
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	/** Checks whether a request accesses a protected resource 
	 * 
	 * @param exchange HttpExchange
	 * @return boolean
	 */
	private boolean isProtectedRequest(HttpExchange exchange) {
	    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
			String rawQuery = exchange.getRequestURI().getRawQuery();
			if (rawQuery != null) {
				RequestParameterList parameters;
				try {
					parameters = new RequestParameterList(rawQuery);
				} catch (WikiFatalException e) {
					e.printStackTrace();
					
					// is this really what we want to do?
					return true;
				} 
				if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterLayout)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterPaste)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterReset)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterTemplate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterStyleSheet)) {
					return true;
				} else {
					return false;
				}
			}
	    }
	    return false;
	}
}
