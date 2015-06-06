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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.security.DBPolicy;
import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.lib.Option;
import org.dbwiki.lib.Some;
import org.dbwiki.lib.None;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.HttpExchangeWrapper;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.ServerResponseHandler;
import org.dbwiki.web.ui.printer.server.DatabaseAccessDeniedPrinter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

/** An authenticator class for a DatabaseWiki
 *
 * @author jcheney,o.cierny
 * TODO: Refactor this and WikiServletAuthenticator to share authentication logic
 */

@SuppressWarnings("restriction")
public class WikiAuthenticator extends Authenticator {
    /*
     * Public Constants
     */
       
       
       

       
    /*
     * Private Variables
     */
       
    //private int _mode;
    private String _realm;
    private UserListing _users;
    //private Vector<Authorization> _authorizationListing;
    private File _formTemplate = null;
       
    // TODO: Get rid of this?
    //private WikiServer _server;
    private DatabaseWiki _wiki = null;  // optional for now
    private SimplePolicy _policy;
    

    /*
     * Constructors
     */
       
    public WikiAuthenticator(String realm, UserListing users, File template, SimplePolicy policy) {
	    _realm = realm;
	    _users = users;
	    _formTemplate = template;
	    _policy = policy;
    }
       
    public WikiAuthenticator(String realm, UserListing users, File template, DatabaseWiki wiki, SimplePolicy policy) {
	    _realm = realm;
	    _users = users;
	    _formTemplate = template;
	    _wiki = wiki;
	    _policy = policy;
    }
       
    /*
     * Public Methods
     */

     
    private boolean allowedFileRequest(String requestURI) {
        // If the request is for a file (as specified by the 
    	// regular expressions above), then no authorization is
        // required.
    	for (String m : SimplePolicy.fileMatches) {
    		if (requestURI.matches(m)) 
    			return true;
    	}
    	return false;
    }
       
    
    public synchronized Result authenticate(HttpExchange exchange) {
    	return authenticate (new HttpExchangeWrapper(exchange));
    
    }
       

    // TODO: Further simplify this and factor into authentication part (establishing user identity) 
    // and authorization part (checking request against policy once identity is established)
    
    public Result authenticate(Exchange<HttpExchange> exchange) {
        // FIXME: #security: If the request is to log in then we should check the username and password no matter what!
        // Currently we don't if we happen to be at a page that doesn't require authentication.
           
        if (allowedFileRequest(exchange.getRequestURI().getPath())) {
            return accessGranted(exchange,User.UnknownUserName); 
        }
           
        Headers rmap = exchange.get().getRequestHeaders();
           
        boolean isProtectedRequest = SimplePolicy.isProtectedRequest(exchange);
           
        String auth = rmap.getFirst("Authorization");
        if (auth == null) {
            if ((_policy._mode == DatabaseWikiProperties.AuthenticateAlways)
                    || ((_policy._mode == DatabaseWikiProperties.AuthenticateWriteOnly) && (isProtectedRequest))
                    || (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
                return retryAccess(exchange);
                
            } else {
                return accessGranted(exchange,User.UnknownUserName);
                
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
            if ((_policy._mode == DatabaseWikiProperties.AuthenticateAlways)
                    || ((_policy._mode == DatabaseWikiProperties.AuthenticateWriteOnly) && (isProtectedRequest))
                    || (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
                if (checkCredentials(uname, pass)) {
                	if (checkRequest(uname, isProtectedRequest, exchange)) {
                		return accessGranted(exchange, uname);
                	} else {
                		return accessDenied(exchange);
                	}
                } else {
                    return retryAccess(exchange);
                }
            } else {
                return accessGranted(exchange,uname); 
                   
            }
        }
    }
    
    /** Check a request by user uname to do request specified by exchange */
    private boolean checkRequest(String uname, boolean isProtectedRequest, Exchange<HttpExchange> exchange) {
    	
    	if(_users.get(uname).is_admin()){
            return true; 
        } else if (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin)) {
            return true; 
        } else {
        	
            Map<Integer,Map<Integer,DBPolicy>> policyListing = new HashMap<Integer,Map<Integer,DBPolicy>>();
            for(int i = 0;i<_policy._authorizationListing.size();i++){
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
                    
                    URI uri = exchange.getRequestURI();
                	Option<Integer> entryIdOpt = isEntryLevelRequest(uri);
                	policyListing = _wiki.getDBPolicyListing(user_id);

                	// check what kind of request it is
                    if (isProtectedRequest) {
                    	//insert request
                        if (SimplePolicy.isInsertRequest(exchange) && isInsert == true){
                        	// if this is an entry-level request
                            if(entryIdOpt.exists()) {
                            	int entryId = entryIdOpt.elt();
                            	// and we have an entry-level policy
                                if(havePolicies(policyListing, user_id,entryId)) {
                                    if(policyListing.get(user_id).get(entryId).capability().isInsert()==true){
                                    	// then use it to grant access
                                        return true; 
                                    } else {
                                        return false; 
                                    }
                                }
                            } // otherwise allow it
                            return true; 
                        //delete request
                        } else if (SimplePolicy.isDeleteRequest(exchange) && isDelete == true){
                            if(entryIdOpt.exists()) {
                            	int entryId = entryIdOpt.elt();    
                                if(havePolicies(policyListing, user_id,entryId)) {
                                    if(policyListing.get(user_id).get(entryId).capability().isDelete()==true){
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                            return true; 
                        //update request
                        } else if (SimplePolicy.isUpdateRequest(exchange) && isUpdate == true){
                            if(entryIdOpt.exists()){
                            	int entryId = entryIdOpt.elt();    
                                if(havePolicies(policyListing, user_id,entryId)) {
                                    if(policyListing.get(user_id).get(entryId).capability().isUpdate()==true){
                                        return true; 
                                    }else{
                                        return false;
                                    }
                                }
                            }
                            return true;
                        } else {
                            return false;
                        }
                    } else { // !isProtectedRequest
                        //read request
                        if (isRead == true) {
                            if (entryIdOpt.exists()){
                            	int entryId = entryIdOpt.elt();    
                                if(havePolicies(policyListing, user_id,entryId)) {
                                    if (policyListing.get(user_id).get(entryId).capability().isRead()==true){
                                        return true; 
                                    } else {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        } else { // !isRead
                            return false;
                        }
                    }
                }
            }
        }
    	// if no rule matches, deny access
        return false; 
    }
    
    private Result accessGranted(Exchange<?> exchange, String user) {
    	
    	return new Authenticator.Success(new HttpPrincipal(user, _realm));
    }
    
    private Result retryAccess(Exchange<HttpExchange> exchange) {
    	Headers map = exchange.get().getResponseHeaders();
        map.set("WWW-Authenticate", "Basic realm=" + "\"" + _realm + "\"");
        return new Authenticator.Retry(401);
    }
    
    private Result accessDenied(Exchange<HttpExchange> exchange)  {
        try {
        	ServerResponseHandler responseHandler = new ServerResponseHandler(new HttpRequest(new RequestURL(exchange, ""), _users), "Access Denied");
        	responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseAccessDeniedPrinter());
        	exchange.send(HtmlTemplateDecorator.decorate(new BufferedReader(new FileReader(_formTemplate)), responseHandler));
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return new Authenticator.Failure(401);

    }
    
       
    public String getRealm() {
        return _realm;
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
                	// TODO: Avoid server dependence here
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
