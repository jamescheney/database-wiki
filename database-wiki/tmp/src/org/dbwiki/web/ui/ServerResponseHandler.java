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
package org.dbwiki.web.ui;

import java.util.Vector;

import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.request.ServerRequest;

public class ServerResponseHandler extends HtmlContentGenerator {
	/*
	 * Private Variables
	 */
	
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public ServerResponseHandler(ServerRequest request, String title) {
		super(request);
		
		_title = title;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(String key, Vector<String> args, HtmlPage page, String indention) throws org.dbwiki.exception.WikiException {
		if (key.equals(ContentTitle)) {
			if (args != null) {
				page.add(args.get(0));
			} else {
				page.add(_title);
			}
		} else {
			super.print(key, args, page, indention);
		}
	}
}
