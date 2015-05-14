/*
    BEGIN LICENSE BLOCK
    Copyright 2010-2014, Heiko Mueller, Sam Lindley, James Cheney, 
    Ondrej Cierny, Mingjun Han, and
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

import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.lib.None;
import org.dbwiki.lib.Option;
import org.dbwiki.lib.Some;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.data.security.Authorization;
import org.dbwiki.data.security.DBPolicy;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.Entry;
import org.dbwiki.web.server.ServletExchangeWrapper;
import org.dbwiki.web.server.WikiServer;



/** A servlet-based authenticator class for DatabaseWiki
 *
 * @author o.cierny
 *
 */

public class WikiServletAuthenticator {

    private int _mode;
    private UserListing _users;
    private String _realm;
    private Vector<Authorization> _authorizationListing;
    
    
    // TODO: Get rid of this?
    private WikiServer _server;
    private static String[] fileMatches = { 
		".*/html/.*\\.html",
		".*/html/style/.*\\.css",
		".*/js/.*\\.js",
		".*/.*\\.txt",
		".*/.*\\.ico",
		".*/pictures/.*\\.gif",
		};
    
    public WikiServletAuthenticator(int mode, String realm, UserListing users, Vector<Authorization> authorizationListing, WikiServer server) {
        _mode = mode;
        _users = users;
        _realm = realm;
        _authorizationListing = authorizationListing;
        _server = server;
    }
       
    
    private boolean allowedFileRequest(String requestURI) {
        // If the request is for a file (as specified by the 
    	// regular expressions above), then no authorization is
        // required.
    	for (String m : fileMatches) {
    		if (requestURI.matches(m)) 
    			return true;
    	}
    	return false;
    }

    public boolean authenticate(HttpServletRequest request) {
        
    	if (allowedFileRequest(request.getRequestURI())) {
    		return true;
    	}
        Exchange<HttpServletRequest> exchange = new ServletExchangeWrapper(request,null);
        boolean needsAuth = this.isProtectedRequest(exchange);
        if(request.getUserPrincipal() == null) {
            if ((_mode == DatabaseWikiProperties.AuthenticateAlways) ||
                (_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
                return false;
            }
            else {
                return true;
            }
        } else if ((_mode == DatabaseWikiProperties.AuthenticateAlways) ||
            (_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
            String uname = request.getUserPrincipal().getName();
            // check if user is administrator
            if(_users.get(uname).is_admin()) {
                return true;
            } else {
                Map<Integer, Map<Integer,DBPolicy>> policyListing = new HashMap<Integer, Map<Integer,DBPolicy>>();
                for(int i = 0; i<_authorizationListing.size(); i++) {
                    int user_id = _authorizationListing.get(i).user_id();
                    String database_name = _authorizationListing.get(i).database_name();
                    String dbname = "/" + database_name;
                    String user_login = _users.get(user_id).login();
                    if(user_login.equals(uname) && dbname.equals(_realm)) {
                        //get the access permissions in the database
                        boolean isRead = _authorizationListing.get(i).capability().isRead();
                        boolean isInsert = _authorizationListing.get(i).capability().isInsert();
                        boolean isDelete = _authorizationListing.get(i).capability().isDelete();
                        boolean isUpdate = _authorizationListing.get(i).capability().isUpdate();
 
                    	Option<Integer> entryIdOpt = isEntryLevelRequest(URI.create(request.getRequestURI())); 
                        policyListing = _server.getDBPolicyListing(database_name, user_id);
                        // check what kind of request it is
                        if(needsAuth) {
                            // insert request
                            if(isInsertRequest(exchange) && isInsert == true) {
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isInsert();
                                    }
                                }
                                return true;
                            // delete request
                            } else if(isDeleteRequest(exchange) && isDelete == true) {
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isDelete();
                                    }
                                }
                                return true;
                            // update request
                            } else if(isUpdateRequest(exchange) && isUpdate == true){
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isUpdate();
                                    }
                                }
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            // read request
                            if(isRead == true) {
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isRead();
                                    }
                                }
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }
       
    /** Checks whether we have entry-level policies for the specific request
     *
     * @param exchange HttpExchange
     * @return boolean
     */
    private boolean havePolicies(Map<Integer, Map<Integer,DBPolicy>> listing, int userId, int entryId) {
        if(listing != null) {
            if(listing.get(userId) != null) {
                if(listing.get(userId).get(entryId) != null) {
                    return true;
                }
            }
        }
        return false;
    }
       
    /** Checks whether a request accesses a protected resource
     *
     * @param exchange HttpExchange
     * @return boolean
     */
    private boolean isProtectedRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                    RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
	                    return true;
	                } else if(parameters.hasParameter(RequestParameter.ParameterAllUsers)){
	                    return true;
	                } else if(parameters.hasParameter(RequestParameter.ParameterAuthorization)){
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
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return true;
                }
                
            }
        }
        return false;
    }
    
    private boolean isInsertRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
	                    return true;
	                }
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isDeleteRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
	                    return true;
	                } 
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isUpdateRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
	                    return true;
	                } 
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }

    
    /** Checks whether a request is an entry-level request
     *
     * @param uri the request uri
     * @return boolean
     * @throws SQLException
     * @throws WikiException
     */
    private Option<Integer> isEntryLevelRequest(URI uri) {
    	String path = uri.getPath();
        if (uri.getRawQuery() != null) {
        path = path + "?" + uri.getRawQuery();
        }
        String[] items = path.split("/");
        if(items.length >= 3) {
            if(items[2].contains("?")) {
                if(items[2].split("\\?")[0].length() == 0) {
                    return new None<Integer>();
                } else {
                    try {
                        return new Some<Integer>(Integer.parseInt(items[2].split("\\?")[0],16));
                    } catch (NumberFormatException e) {
                        return new None<Integer>();
                    }
                    
                }
            } else {
                try {
                    Map<Integer, Entry> entryListing = _server.getEntryListing(_realm.substring(1));
                    try {
                        int entry = Integer.parseInt(items[2],16);
                        if(entryListing.get(entry)!=null) {
                            return new Some<Integer>(entry);
                        }
                    } catch (NumberFormatException e) {
                        return new None<Integer>();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return new None<Integer>();
    }
       
    public int getAuthenticationMode() {
        return _mode;
    }
       
    public void setAuthenticationMode(int mode) {
        _mode = mode;
    }
       
    public void updateAuthorizationListing(Vector<Authorization> auth) {
        _authorizationListing = auth;
    }

}
