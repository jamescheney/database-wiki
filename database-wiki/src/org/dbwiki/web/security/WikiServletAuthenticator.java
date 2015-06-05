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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbwiki.exception.WikiException;
import org.dbwiki.lib.None;
import org.dbwiki.lib.Option;
import org.dbwiki.lib.Some;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.DBPolicy;
import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.ServletExchangeWrapper;



/** A servlet-based authenticator class for DatabaseWiki
 *
 * @author o.cierny
 *
 */

public class WikiServletAuthenticator {

    private UserListing _users;
    private String _realm;
    //private Vector<Authorization> _authorizationListing;
    private SimplePolicy _policy;
    
    // TODO: Get rid of this?
    //private WikiServer _server;
    private DatabaseWiki _wiki;
    
    private static String[] fileMatches = { 
		".*/html/.*\\.html",
		".*/html/style/.*\\.css",
		".*/js/.*\\.js",
		".*/.*\\.txt",
		".*/.*\\.ico",
		".*/pictures/.*\\.gif",
		};
    
    public WikiServletAuthenticator(String realm, UserListing users, DatabaseWiki wiki, SimplePolicy policy) {
        _users = users;
        _realm = realm;
        _policy = policy;
        _wiki = wiki;
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

    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        return authenticate (new ServletExchangeWrapper(request,response));
        
    }
    
    public boolean authenticate(Exchange<HttpServletRequest> exchange) {
        
    	if (allowedFileRequest(exchange.get().getRequestURI())) {
    		return true;
    	}
        boolean needsAuth = SimplePolicy.isProtectedRequest(exchange);
        if(exchange.get().getUserPrincipal() == null) {
            if ((_policy._mode == DatabaseWikiProperties.AuthenticateAlways) ||
                (_policy._mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
                return false;
            }
            else {
                return true;
            }
        } else if ((_policy._mode == DatabaseWikiProperties.AuthenticateAlways) ||
            (_policy._mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
            String uname = exchange.get().getUserPrincipal().getName();
            // check if user is administrator
            if(_users.get(uname).is_admin()) {
                return true;
            } else {
                Map<Integer, Map<Integer,DBPolicy>> policyListing = new HashMap<Integer, Map<Integer,DBPolicy>>();
                for(int i = 0; i<_policy._authorizationListing.size(); i++) {
                    int user_id = _policy._authorizationListing.get(i).user_id();
                    String database_name = _policy._authorizationListing.get(i).database_name();
                    String dbname = "/" + database_name;
                    String user_login = _users.get(user_id).login();
                    if(user_login.equals(uname) && dbname.equals(_realm)) {
                        //get the access permissions in the database
                        boolean isRead = _policy._authorizationListing.get(i).capability().isRead();
                        boolean isInsert = _policy._authorizationListing.get(i).capability().isInsert();
                        boolean isDelete = _policy._authorizationListing.get(i).capability().isDelete();
                        boolean isUpdate = _policy._authorizationListing.get(i).capability().isUpdate();
 
                    	Option<Integer> entryIdOpt = isEntryLevelRequest(URI.create(exchange.get().getRequestURI())); 
                        policyListing = _wiki.getDBPolicyListing(user_id);
                        // check what kind of request it is
                        if(needsAuth) {
                            // insert request
                            if(SimplePolicy.isInsertRequest(exchange) && isInsert == true) {
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isInsert();
                                    }
                                }
                                return true;
                            // delete request
                            } else if(SimplePolicy.isDeleteRequest(exchange) && isDelete == true) {
                                if(entryIdOpt.exists()) {
                                	int entryId = entryIdOpt.elt();    
                                    if(havePolicies(policyListing, user_id,entryId)) {
                                        return policyListing.get(user_id).get(entryId).capability().isDelete();
                                    }
                                }
                                return true;
                            // update request
                            } else if(SimplePolicy.isUpdateRequest(exchange) && isUpdate == true){
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
                    //Map<Integer, Entry> entryListing = _server.getEntryListing(_realm.substring(1));
                    DatabaseContent entries = _wiki.database().content();
                    try {
                        int entry = Integer.parseInt(items[2],16);
                        if(entries.get(entry)!=null) {
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
       


}
