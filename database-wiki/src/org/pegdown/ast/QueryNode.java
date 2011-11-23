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
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.request.parameter.RequestParameterVersionCurrent;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.SchemaNodeList;
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
    
    /**
     * Output HTML / JavaScript to draw a chart
     * using the supplied query result set
     * which should be a list of (string, number) pairs
     * 
     * @param rs
     * @param body
     */
    private void drawChart(QueryResultSet rs, HtmlLinePrinter body) {
		String xlabel = ((GroupSchemaNode)rs.schema()).children().get(0).label();
		String ylabel = ((GroupSchemaNode)rs.schema()).children().get(1).label();
		
		body.add("<div id=\"chart\" style=\"width:400px; height:300px;\"/>");
		body.add("<script>");
		body.add("  drawColumnChart('Some chart', '" + xlabel + "', '" + ylabel + "' , [");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);
			try {
				DatabaseAttributeNode rx = (DatabaseAttributeNode)r.children().get(0);
				DatabaseAttributeNode ry = (DatabaseAttributeNode)r.children().get(1);
				
				String x = rx.value().getCurrent().value();
				x = x.replace("'", "\\'");
				String y = ry.value().getCurrent().value();

				if (y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
					String prefix;
					if(nonEmpty) prefix = ", ";
					else prefix = "";

					body.add(prefix + "{x : '" + x + "', y : " + y + "}");
					nonEmpty = true;
				} else {  
					// not a number
					continue;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
		}
		
		body.add("])");
		body.add("</script>");
    }
    
    /**
     * Output HTML / JavaScript to draw a map
     * using the supplied query result set
     * which should be a list of locations
     * 
     * @param rs
     * @param body
     */
    private void drawMap(QueryResultSet rs, HtmlLinePrinter body) {
		body.add("<div id=\"map\" style=\"width:400px; height:300px;\"/>");
		body.add("<script>");
		body.add("  drawMap([");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);
			try {
				DatabaseAttributeNode rl = (DatabaseAttributeNode)r.children().get(0);
				try {
					String location = rl.value().getCurrent().value();
					location = location.replace("'", "\\'");
					
					String prefix;
					if(nonEmpty) prefix = ",\n";
					else prefix = "";

					body.add(prefix + "'" + location + "'");
					nonEmpty = true;
				} catch (NumberFormatException e) {
					continue;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
		}
		
		body.add("])");
		body.add("</script>");
    }
    
    /**  FIXME #wiki: Clean this up and make queries independent of database.
     * Implements printing queries by finding the database associated with the printer and querying it.
     */
    @Override
    public void print(Printer printer) {
    	HtmlLinePrinter body = new HtmlLinePrinter();
    	
        PageContentPrinter contentPrinter = (PageContentPrinter)((ExtendedPrinter) printer).getExtension();
        Database database = contentPrinter.getDatabase();
    	
    	try {
    		boolean drawChart = false;
    		boolean drawMap = false;
    		
    		String query = _queryString;
    		if(query.toLowerCase().startsWith("chart:")) {
    			drawChart = true;
    			query = query.substring("chart:".length());
    		} else if(query.toLowerCase().startsWith("map:")) {
    			drawMap = true;
    			query = query.substring("map:".length());
    		}
    		    		
			QueryResultSet rs = database.query(query);
			if (!rs.isEmpty()) {
				body.openPARAGRAPH(CSS.CSSPageText);
				if(drawChart) {
					drawChart(rs, body);
				} else if (drawMap) {
					drawMap(rs, body);
				} else if (rs.isElement()) {
					body.add(contentPrinter.getLinesForNodeList(new SchemaNodeList(rs), new RequestParameterVersionCurrent()));
				} else {
					for (int i = 0; i < rs.size(); i++) {
						contentPrinter.printTextNode((DatabaseTextNode)rs.get(i), body);
					}
				}
			}
		} catch (org.dbwiki.exception.data.WikiQueryException queryException) {
			queryException.printStackTrace();
			body.paragraph("<b> " + queryException.toString() + "</b>", CSS.CSSPageText);
		} catch (WikiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		HtmlPage lines = body.lines();
		for(int i = 0; i < lines.size(); i++)
			printer.print(lines.get(i));
    }
}
