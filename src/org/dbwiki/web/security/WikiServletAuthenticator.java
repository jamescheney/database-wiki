package org.dbwiki.web.security;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dbwiki.user.RegularUser;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.WikiServer;



/** A servlet-based authenticator class for DatabaseWiki 
 * 
 * @author o.cierny
 *
 */

public class WikiServletAuthenticator {

	private int _mode;
	private UserListing _users;
	
	public WikiServletAuthenticator(int mode, UserListing users) {
		_mode = mode;
		_users = users;
	}
	
	public boolean authenticate(HttpServletRequest request) {
		String path = request.getRequestURI();
		
		// File requests don't require authentication
		if(path.indexOf('.') != -1) {
			return true;
		}
		
		boolean needsAuth = this.isProtectedRequest(request);
		if(request.getUserPrincipal() == null) {
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways) || (_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
				return false;
			}
			else {
				return true;
			}
		}
		
		return true;
	}
	
	@Deprecated
	public boolean authenticate_old(HttpServletRequest request) {
		String path = request.getRequestURI();
		
		// File requests don't require authentication
		if(path.indexOf('.') != -1) {
			return true;
		}
		
		boolean needsAuth = this.isProtectedRequest(request);
		
		String auth = request.getHeader("Authorization");
		if(auth == null) {
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways)
					|| ((_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth)
					|| (path.equals(WikiServer.SpecialFolderLogin))) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			int sp = auth.indexOf(' ');
			if (sp == -1 || !auth.substring(0, sp).equals("Basic")) {
				return false;
			}
			byte[] b = new Base64().base64ToByteArray(auth.substring(sp + 1));
			String userpass = new String(b);
			int colon = userpass.indexOf(':');
			String name = userpass.substring(0, colon);
			String pass = userpass.substring(colon + 1);
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways)
					|| ((_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth)
					|| (path.equals(WikiServer.SpecialFolderLogin))) {
				if (checkCredentials(name, pass)) {
					if(request.getSession().getAttribute("user") == null) {
						request.getSession().setAttribute("user", name);
					}
					return true;
				} else {
					return false;
				}
			}
			else {
				if(request.getSession().getAttribute("user") == null) {
					request.getSession().setAttribute("user", name);
				}
				return true;
			}
		}
	}
	
	/**
	 * Check whether password supplied by user claiming to be username is correct.
	 * FIXME #security: Avoid plaintext passwords!
	 */
	private boolean checkCredentials(String username, String password) {
		if (!_users.isEmpty()) {
			if (_users.contains(username)) {
				return ((RegularUser)_users.get(username)).password().equals(password);
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	/** Checks whether a request accesses a protected resource 
	 * 
	 * @param request HttpServletRequest
	 * @return boolean
	 */
	private boolean isProtectedRequest(HttpServletRequest request) {
	    if ("GET".equalsIgnoreCase(request.getMethod())) {
	    	List<?> parameters = Collections.list((Enumeration<?>)request.getParameterNames());
			if (parameters != null) {
				if(parameters.size() == 0) {
					return false;
				} else if (parameters.contains(RequestParameter.ParameterActivate)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterCreate)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterCreateSchemaNode)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterDelete)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterEdit)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterLayout)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterPaste)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterReset)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterTemplate)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterStyleSheet)) {
					return true;
				} else {
					return false;
				}
			}
	    }
	    return false;
	}
	
	public int getAuthenticationMode() {
		return _mode;
	}
	
	public void setAuthenticationMode(int mode) {
		_mode = mode;
	}

}
