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
package org.dbwiki.web.ui.printer.data;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;


import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints the path of node labels associated with a node
 *  
 * @author jcheney
 *
 */
public class NodePathPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private WikiDataRequest<?> _request;
	
	
	/*
	 * Constructors
	 */
	
	public NodePathPrinter(WikiDataRequest<?>  request, DatabaseLayouter layouter) {
		_request = request;
		_layouter = layouter;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));

		String line = null;
		System.out.println("Parents: ");
		DatabaseNode node = _request.node();
		while (node != null) {
			if (node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)node;
				String target = _request.wri().databaseIdentifier().linkPrefix() + element.identifier().toURLString();
				if (!versionParameter.versionCurrent()) {
					target = target + "?" + versionParameter.toURLString();
				}
				String link = _layouter.get(element.schema()).getShortLabel(element, versionParameter);
				if (link.equals("")) {
					link = "Parent of";
				}
				link = "<a CLASS=\"" + CSS.CSSObjectPath + "\" HREF=\"" + target + "\">" + link + "</a>";
				if (line != null) {
					System.out.println(link + " >> -" + _layouter.get(element.schema()).getShortLabel(element, versionParameter) + "-");
					line = link + " &gt; " + line;
				} else {
					line = link;
				}
			}
			node = node.parent();
		}
		if (line != null) {
			body.paragraph(line, CSS.CSSObjectPath);
		}
	}
}
