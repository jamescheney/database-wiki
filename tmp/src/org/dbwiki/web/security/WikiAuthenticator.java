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

import java.net.URLDecoder;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;

import org.dbwiki.web.server.WikiServer;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class WikiAuthenticator extends Authenticator {
	/*
	 * Public Constants
	 */
	
	public static final int AuthenticateAlways = 0;
	public static final int AuthenticateNever = 1;
	public static final int AuthenticateWriteOnly = 2;
	
	

	
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
	
	public synchronized Result authenticate(HttpExchange exchange) {
		// If the request is for a file (currently indicated by
		// a '.' in the request path), then no authorization is
		// required.
		if (exchange.getRequestURI().getPath().indexOf('.') != -1) {
			return new Authenticator.Success(new HttpPrincipal("", _realm));
		}
		
		Headers rmap = (Headers) exchange.getRequestHeaders();
		
		boolean isProtectedRequest = this.isProtectedRequest(exchange);
		
		String auth = rmap.getFirst("Authorization");
		if (auth == null) {
			if ((_mode == AuthenticateAlways)
					|| ((_mode == AuthenticateWriteOnly) && (isProtectedRequest))
					|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
				Headers map = (Headers) exchange.getResponseHeaders();
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
			if ((_mode == AuthenticateAlways) || ((_mode == AuthenticateWriteOnly) && (isProtectedRequest))) {
				if (checkCredentials(uname, pass)) {
					return new Authenticator.Success(new HttpPrincipal(uname, _realm));
				} else {
					Headers map = (Headers) exchange.getResponseHeaders();
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
	
	private boolean isProtectedRequest(HttpExchange exchange) {
	    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
			String rawQuery = exchange.getRequestURI().getRawQuery();
			if (rawQuery != null) {
				RequestParameterList parameters;
				try {
					parameters = new RequestParameterList(URLDecoder.decode(rawQuery, "UTF-8"));
				} catch (java.io.UnsupportedEncodingException uee) {
					return true;
				}
				if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreateEntity)) {
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
