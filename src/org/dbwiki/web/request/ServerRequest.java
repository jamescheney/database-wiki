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

import org.dbwiki.user.User;
import org.dbwiki.web.server.WikiServer;



public class ServerRequest<T> extends HttpRequest<T> {
	/*
	 * Private Variables
	 */
	
	private WikiServer _server;
	private User _user;
	
	/*
	 * Constructors
	 */
	
	public ServerRequest(WikiServer server, Exchange<T> exchange) throws org.dbwiki.exception.WikiException {
		super(new RequestURL<T>(exchange, ""), server.users());
		
		_server = server;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public WikiServer server() {
		return _server;
	}
}
