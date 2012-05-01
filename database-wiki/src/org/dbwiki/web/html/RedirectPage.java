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
package org.dbwiki.web.html;

import org.dbwiki.data.resource.ResourceIdentifier;

import org.dbwiki.web.request.WikiRequest;

/** An HtmlPage that redirects to a given resource.
 * 
 * @author jcheney
 *
 */
public class RedirectPage extends HtmlPage {
	/*
	 * Constructors
	 */
	
	/** Redirects to a given resource identifier under the prefix of the wiki request */
	public RedirectPage(WikiRequest<?> request, ResourceIdentifier identifier) {
		this(request.wri().databaseIdentifier().linkPrefix() + identifier.toURLString());
	}
	
	/** Redirects to a string @link */
	public RedirectPage(String link) {
		this.add("<html><head><title>Redirect page</title></head><body>");
		this.add("<p>If you are not redirected automatically within a few seconds then please click on the link below.</p>");
		this.add("<p><a HREF=\"" + link + "\">" + link + "</a></p>");
		this.add("<script type=\"text/javascript\"><!--");
		this.add("setTimeout('Redirect()',0);");
		this.add("function Redirect()");
		this.add("{");
		this.add("  location.href = '" + link + "';");
		this.add("}");
		this.add("// --></script>");
		this.add("</body></html>");
	}
}
