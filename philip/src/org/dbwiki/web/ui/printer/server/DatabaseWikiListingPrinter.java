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
package org.dbwiki.web.ui.printer.server;


import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Generates the list of database wikis.
 * 
 * @author jcheney
 *
 */
public class DatabaseWikiListingPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	//private List<DatabaseWikiProperties> _wikilist;
	private WikiServer _server;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWikiListingPrinter(WikiServer server) {
		_server = server;
		
	}
	
	
	
	/*
	 * Public Methods
	 */

	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.openLIST(CSS.CSSDatabase);
		
		for (int iWiki = 0; iWiki < _server.size(); iWiki++) {
			DatabaseWiki wiki = _server.get(iWiki);
			printer.listITEM(wiki.database().identifier().databaseHomepage(), wiki.getTitle(), CSS.CSSDatabase);
		}
		
		
		
		printer.closeLIST();
	}
}
