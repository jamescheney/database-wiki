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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.data.wiki.DatabaseWikiPage;
import org.dbwiki.web.ui.CSS;


import org.dbwiki.web.ui.printer.HtmlContentPrinter;


/** Prints history of a wiki page
 * 
 * @author jcheney
 *
 */
public class PageHistoryPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiPageRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public PageHistoryPrinter(WikiPageRequest request) {	
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void print(HtmlLinePrinter body) throws WikiException {
		body.paragraph("Page History", CSS.CSSHeadline);
		
		printPageVersions(body);
	}
	
	/*
	 * Private Methods
	 */
	
	private void printPageVersions( HtmlLinePrinter body) {
		List<DatabaseWikiPage> versions = null;
		try {
			versions = _request.versions();
		} catch (WikiException e) {
			e.printStackTrace();
		}
		
		body.openTABLE(CSS.CSSObjectFrame);
		body.openTR();
		body.openTD(CSS.CSSObjectListing);
		body.openTABLE(CSS.CSSList);
		
		body.openTR();
		body.openTH(CSS.CSSList);
		body.add("Version ID");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.add("Timestamp");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.add("User");
		body.closeTH();
		body.closeTR();

		String baseURL = _request.wri().getURL();
		
		// Versions should never be null
		assert(versions != null);
		
		for(DatabaseWikiPage p : versions) {
			
			/*
			The problem with the strange letters is because of the encoding type.
			So maybe the best way to fix this could be to not use the names of the months
			but instead to represent the date as numerics and to save it 
			in the begining using timezones.
			After that, whoever needs the time for something, the code should just compare 
			the users time zone to UTC and add the hours so that the user can see the timestamp
			in his time zone.
			*/
			
			Calendar calendar=new GregorianCalendar();
			calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			String dateString=calendar.getTime().toString();
			
//			Date d = new Date(p.getTimestamp());
//			String dateString = new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(d);
			String username = User.UnknownUserName;
			if(p.getUser() != null) {
				username = p.getUser().fullName();
			}
			
			body.openTR();
			body.openTD(CSS.CSSList);
			body.link(baseURL + "?version=" + Long.toString(p.getTimestamp()),
				      Integer.toString(p.getID()),
				      CSS.CSSList);
			body.closeTD();
			body.openTD(CSS.CSSList);
			body.add(dateString);
			body.closeTD();
			body.openTD(CSS.CSSList);
			body.add(username);
			body.closeTD();
			body.closeTR();
		}
		
		body.closeTABLE();
		body.closeTD();
		body.closeTR();
		body.closeTABLE();
	}
}
