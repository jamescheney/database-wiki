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
package org.dbwiki.web.ui.printer;

import org.dbwiki.data.annotation.AnnotationList;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;


import org.dbwiki.web.ui.CSS;

/** 
 * Printer that emits object annotations along with a mini-form for adding annotations
 * FIXME #annotations: This might be better handled in Annotation class
 * @author jcheney
 *
 */
public class ObjectAnnotationPrinter extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiDataRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public ObjectAnnotationPrinter(WikiDataRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		AnnotationList annotations = _request.node().annotation();
		
		if (annotations.size() > 0) {
			body.openTABLE(CSS.CSSAnnotationListing);
			body.openTR();
			body.openTH(CSS.CSSAnnotationListing);
			body.text("Comment");
			body.closeTH();
			body.openTH(CSS.CSSAnnotationListing);
			body.text("User");
			body.closeTH();
			body.openTH(CSS.CSSAnnotationListing);
			body.text("Date");
			body.closeTH();
			body.closeTR();
			for (int iAnnotation = 0; iAnnotation < annotations.size(); iAnnotation++) {
				body.openTR();
				String css = null;
				if ((iAnnotation % 2) == 0) {
					css = CSS.CSSAnnotationRowEven;
				} else {
					css = CSS.CSSAnnotationRowOdd;
				}
				body.openTD(css);
				body.add(annotations.get(iAnnotation).text());
				body.closeTD();
				body.openTD(css);
				if (annotations.get(iAnnotation).user() != null) {
					body.add(annotations.get(iAnnotation).user().fullName());
				} else {
					body.add(User.UnknownUserName);
				}
				body.closeTD();
				body.openTD(css);
				body.add(annotations.get(iAnnotation).date());
				body.closeTD();
				body.closeTR();
			}
			body.closeTABLE();
		} else {
			body.openTABLE(CSS.CSSAnnotationEmpty);
			body.openTR();
			body.openTD(CSS.CSSAnnotationEmpty);
			body.text("There are currently no annotations for this object");
			body.closeTD();
			body.closeTR();
			body.closeTABLE();
		}
		
		body.add("<p CLASS=\"" + CSS.CSSAnnotationLink + "\"><a CLASS=\"" + CSS.CSSAnnotationLink + "\" HREF=\"#\" ONCLICK=\"dspToggleAnnotationForm(); return false;\">Add your comment ...</a></p>");
		
		body.add("<div id=\"annotation_form\"  style=\"display:none\">");
		body.add("	<form name=\"frmAnnotation\" method=\"POST\" action=\"" + _request.wri().getURL() + "\">");
		body.add("		<table width=\"100%\"><tr><td>");
		body.add("		<textarea name=\"" + RequestParameter.ActionValueAnnotation +"\" cols=\"100\" rows=\"5\"></textarea>");
		body.add("		</td><td align=\"left\">");
		body.add("		<button style=\"background:none; border:none\" type=\"submit\" + name=\"action\" value=\"" + RequestParameterAction.ActionAnnotate + "\">");
		body.add("      	<img src=\"/pictures/button_save.gif\">");
		body.add("		</button>");		
		body.add("		</td></tr></table>");
		body.add("	</form>");
		body.add("</div>");
	}
}
