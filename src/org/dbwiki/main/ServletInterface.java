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
package org.dbwiki.main;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbwiki.web.server.WikiServerServlet;

import com.google.appengine.api.utils.SystemProperty;

/** Servlet interface for the database wiki
 * 
 * @author o.cierny
 *
 */
@SuppressWarnings("serial")
public class ServletInterface extends HttpServlet {
	
	private WikiServerServlet _server;
	
	/*
	 * Public Methods
	 */
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);		
		try {
			String settings = null;
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
				settings = "/resources/configuration/server/config.engine";
			} else {
				settings = "/resources/configuration/server/config.local";
			}
			_server = new WikiServerServlet(getServletContext().getRealPath("/"), org.dbwiki.lib.IO.loadProperties(new File(getServletContext().getRealPath(settings))));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		request.getSession(true);
		_server.handle(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		
	}

}
