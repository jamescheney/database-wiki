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
package org.pegdown.ast;

import java.util.ArrayList;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.LineSet;
import org.dbwiki.web.request.parameter.RequestParameterVersionCurrent;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.EntityNodeList;
import org.dbwiki.web.ui.printer.page.PageContentPrinter;

import org.pegdown.ExtendedPrinter;
import org.pegdown.Printer;

public class QueryNode extends Node {
	private ArrayList<String> _path = new ArrayList<String>();
	
	private String _queryString;
	
	public QueryNode(String queryString) {
		_queryString = queryString;
	}
	
    public QueryNode(Node path) {
        for(Node n : path.getChildren())
        	_path.add(n.getText());
    }
    
    @Override
    public void print(Printer printer) {
    	HtmlLinePrinter body = new HtmlLinePrinter();
    	
        PageContentPrinter contentPrinter = (PageContentPrinter)((ExtendedPrinter) printer).getExtension();
        Database database = contentPrinter.getDatabase();
    	
    	try {
			QueryResultSet rs = database.query(_queryString);
			if (!rs.isEmpty()) {
				body.openPARAGRAPH(CSS.CSSPageText);
				if (rs.isElement()) {
					body.add(contentPrinter.getLinesForNodeList(new EntityNodeList(rs), null, new RequestParameterVersionCurrent()));
				} else {
					for (int iNode = 0; iNode < rs.size(); iNode++) {
						contentPrinter.printTextNode((DatabaseTextNode)rs.get(iNode), body);
					}
				}
				body.closePARAGRAPH();
			}
		} catch (org.dbwiki.exception.data.WikiQueryException queryException) {
			queryException.printStackTrace();
			body.paragraph("<b> " + queryException.toString() + "</b>", CSS.CSSPageText);
		} catch (WikiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		LineSet lines = body.lines();
		for(int i = 0; i < lines.size(); i++)
			printer.print(lines.get(i));
    }
}
