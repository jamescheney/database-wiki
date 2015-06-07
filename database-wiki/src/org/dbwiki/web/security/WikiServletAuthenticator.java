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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.ServletExchangeWrapper;



/** A servlet-based authenticator class for DatabaseWiki
 *
 * @author o.cierny
 *
 */

public class WikiServletAuthenticator {

    private UserListing _users;
    private SimplePolicy _policy;
    
    // TODO: Get rid of this?
    private DatabaseWiki _wiki;
    
    
    public WikiServletAuthenticator(String realm, UserListing users, DatabaseWiki wiki, SimplePolicy policy) {
        _users = users;
        _policy = policy;
        _wiki = wiki;
    }
       

    public synchronized boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        return authenticate (new ServletExchangeWrapper(request,response));
        
    }
    
    public boolean authenticate(Exchange<HttpServletRequest> exchange) {
        
    	if (SimplePolicy.allowedFileRequest(exchange)) {
    		return true;
    	}
    	
        if(exchange.get().getUserPrincipal() == null) {
            if (isControlledRequest(exchange)) {
                return false;
            }
            else {
                return true;
            }
        } else if (isControlledRequest(exchange)) {
            String uname = exchange.get().getUserPrincipal().getName();
            User user = _users.get(uname);
            // check if user is administrator
            return _policy.checkRequest(user, _wiki, exchange);
            
        } else {
            return true;
        }
    }
       
    private boolean isControlledRequest(Exchange<HttpServletRequest> exchange) {
		return _policy.isControlledRequest(exchange);
	}


	
  
}
