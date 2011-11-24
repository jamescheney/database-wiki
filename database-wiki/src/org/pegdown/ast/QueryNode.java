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
import org.dbwiki.data.database.DatabaseElementList;
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
	private static int counter = 0;
	private ArrayList<String> _path = new ArrayList<String>();
	
	private String freshName(String prefix) {
		counter++;
		return prefix + counter;
	}
	
	private String _queryString;
	
	public QueryNode(String queryString) {
		_queryString = queryString;
	}
	
    public QueryNode(Node path) {
        for(Node n : path.getChildren())
        	_path.add(n.getText());
    }
    
    private String stringConcat(String[] ss, String delim) {
    	StringBuffer buf = new StringBuffer();
    	if(ss.length > 0) {
    		buf.append(ss[0]);
    		for(int i = 1; i < ss.length; i++) {
    			buf.append(delim);
    			buf.append(ss[i]);
    		}
    	}
    	return buf.toString();
    }
    
    private String escapeString(String s) {
    	return s.replace("'", "\\'");
    }
    
    /**
     * Output HTML / JavaScript to draw a chart
     * using the supplied query result set
     * which should be a list of (string, number) pairs
     * 
     * This function won't necessarily do the right thing if
     * multiple results are returned for the same column (as is allowed
     * by the system).
     * 
     * @param rs
     * @param body
     */
    private void drawChart(QueryResultSet rs, HtmlLinePrinter body) {
    	org.dbwiki.data.schema.SchemaNodeList schemaChildren = ((GroupSchemaNode)rs.schema()).children();
    	
    	int schemaSize = schemaChildren.size();

		String xlabel = "'" + escapeString(schemaChildren.get(0).label()) + "'";
		
		String[] ylabels = new String[schemaSize-1];
		for(int i = 1; i < schemaSize; i++) {
			ylabels[i-1] = schemaChildren.get(i).label();
		}
		
		String escapedylabels[] = new String[schemaSize-1];
		for(int i = 0; i < schemaSize-1; i++) {
			escapedylabels[i] = "'" + escapeString(ylabels[i]) + "'";
		}
		String ylabel = "[" + stringConcat(escapedylabels, ", ") + "]";
				
		String chartId = freshName("chart");
		body.add("<div id=\"" + chartId + "\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawColumnChart('" + chartId +
						"', 'Some chart', " + xlabel + ", " + ylabel + " , [");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);
			try {
				DatabaseElementList children = r.children();
				if(children.size() < 2)
					continue;

				DatabaseAttributeNode rx = (DatabaseAttributeNode)children.get(0);
				String x = "'" + escapeString(rx.value().getCurrent().value()) + "'";
				
				int n = children.size();
				
				String yvalues[] = new String[n-1];
				
				for (int j = 0; j < schemaSize-1; j++) {
					DatabaseElementList matches = children.get(ylabels[j]);
					String y = ((DatabaseAttributeNode)(matches.get(0))).value().getCurrent().value();

					if (y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
						yvalues[j] = y;
					} else {
						yvalues[j] = "";
					}
				}
				
				String y = "[" + stringConcat(yvalues, ", ") + "]";

				String prefix;
				if(nonEmpty) prefix = ", ";
				else prefix = "";

				body.add(prefix + "{x : " + x + ", y : " + y + "}");
				nonEmpty = true;
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
    	String mapId = freshName("map");
		body.add("<div id=\"" + mapId +"\" style=\"width:400px; height:300px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawMap('"+ mapId +"', [");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);
			try {
				DatabaseElementList children = r.children();
				if(children.size() == 0)
					continue;
				
				int n = children.size();

				String location = ((DatabaseAttributeNode)children.get(0)).value().getCurrent().value();
				for(int j = 1; j < n; j++) {
					location = location + ", " + ((DatabaseAttributeNode)children.get(j)).value().getCurrent().value();
				}

				String prefix;
				if(nonEmpty) prefix = ",\n";
				else prefix = "";

				body.add(prefix + "'" + escapeString(location) + "'");
				nonEmpty = true;
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
