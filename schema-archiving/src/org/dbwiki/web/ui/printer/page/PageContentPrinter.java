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
package org.dbwiki.web.ui.printer.page;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.wiki.DatabaseWikiPage;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiPageRequest;

import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.printer.data.DataNodePrinter;

import org.pegdown.ExtendedPegDownProcessor;

/** Prints wiki page content as HTML
 * FIXME #query: Make wiki page query evaluation independent of particular Database
 * @author jcheney
 *
 */
public class PageContentPrinter extends DataNodePrinter {
	/*
	 * Private Variables
	 */
	
	private WikiPageRequest _request;
	
	private ExtendedPegDownProcessor _pegDownProcessor;
	
	
	/*
	 * Constructors
	 */
	
	public PageContentPrinter(WikiPageRequest request, DatabaseLayouter layouter) {
		super(request.wiki().database().identifier(), layouter);
		
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		DatabaseWikiPage page = _request.page();
		body.paragraph(page.getName(), CSS.CSSHeadline);
		
		body.openTABLE(CSS.CSSPageFrame);
		body.openTR();
		body.openTD(CSS.CSSPageContent);

		if(_pegDownProcessor == null)
		  _pegDownProcessor = new ExtendedPegDownProcessor();
		
		String content = _pegDownProcessor.markdownToHtml(page.getContent(), this);
		body.add(content);
				
		body.closeTD();
		body.closeTR();
		body.closeTABLE();
		
		String username = User.UnknownUserName;
		if(page.getUser() != null) {
			username = page.getUser().fullName();
		}
		
		String timestamp = new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new Date(page.getTimestamp()));
		
		body.add("<div><i>Modified by " + username + " " +
				"at " + timestamp +
				"</i></div>");	
	}
	
	public Database getDatabase() {
		return _request.wiki().database();
	}
	
	/*
	 * Private Methods
	 */

}
