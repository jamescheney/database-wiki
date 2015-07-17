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

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.data.security.Policy;
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
       
    private String _realm;
    private UserListing _users;
    private File _formTemplate = null;
       
    private Policy _policy;
    

    /*
     * Constructors
     */
       
    public WikiAuthenticator(String realm, UserListing users, File template, Policy policy) {
	    _realm = realm;
	    _users = users;
	    _formTemplate = template;
	    _policy = policy;
    }
       
       
    /*
     * Public Methods
     */


    
    public boolean isControlledRequest(Exchange <?> exchange) {
    	return _policy.isControlledRequest(exchange) 
    			|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin));
    }
    
    public synchronized Result authenticate(HttpExchange exchange) {
    	return authenticate (new HttpExchangeWrapper(exchange));
    }
       

    /** 
     *  Authenticate and check the request.
     * @param exchange
     * @return
     */
    public Result authenticate(Exchange<HttpExchange> exchange) {
        // FIXME: #security: If the request is to log in then we should check the username and password no matter what!
        // Currently we don't if we happen to be at a page that doesn't require authentication.
           
        if (_policy.allowedFileRequest(exchange)) {
            return accessGranted(exchange,User.UnknownUserName); 
        }
           
        Headers rmap = exchange.get().getRequestHeaders();
           
       
        String auth = rmap.getFirst("Authorization");
        if (auth == null) {
            if (isControlledRequest(exchange) ) {
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
            User user = _users.get(uname);
            String pass = userpass.substring(colon + 1);
            if (isControlledRequest(exchange)) {
                if (checkCredentials(uname, pass)) {
                	if (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin)) {
                        return accessGranted(exchange, uname);
                    } else if(exchange.getRequestURI().toString().contains("edit")) {
                    	return accessGranted(exchange, uname);
                    } else if (_policy.checkRequest(user, exchange)) {
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

}
