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

import static org.parboiled.errors.ErrorUtils.printParseErrors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.query.visual.VisualisationNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.data.WikiQueryException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionCurrent;
import org.dbwiki.web.request.parameter.RequestParameterVersionTimestamp;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.SchemaNodeList;
import org.dbwiki.web.ui.printer.page.PageContentPrinter;

import org.parboiled.Parboiled;
import org.parboiled.support.ParsingResult;
import org.pegdown.ExtendedPrinter;
import org.pegdown.Extensions;
import org.pegdown.Parser;
import org.pegdown.Printer;

public class QueryNode extends Node {
	private static int counter = 0;
	private ArrayList<String> _path = new ArrayList<String>();
	
	private enum ChartType {Column, Pie, Map, Geo, Line, Area, Scatter, ITMap, Candlestick, Gauge, Treemap, Combo}; 
	
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

    /**
     * Concatenate the collection of strings ss using delimiter delim.
     * 
     * @param ss
     * @param delim
     * @return
     */
    private String stringConcat(Collection<String> ss, String delim) { 
    	Iterator<String> p = ss.iterator();
    	return stringConcat(p, delim);
    }

    /**
     * Concatenate the collection of strings starting at p using delimiter delim.
     * 
     * @param p
     * @param delim
     * @return
     */
    private String stringConcat(Iterator<String> p, String delim) {
    	StringBuffer buf = new StringBuffer();
     	if(p.hasNext()) {
    		buf.append(p.next());
    		while(p.hasNext()) {
    			buf.append(delim);
    			buf.append(p.next());
    		}
    	}
    	return buf.toString();
    }  
    
    /**
     * Escape a string for JavaScript output.
     *  
     * @param s
     * @return
     */
    private String escapeString(String s) {
    	return s.replace("'", "\\'");
    }
    
    /**
     * Return an ArrayList<String> containing the labels from the schema of
     * the result set.
     * 
     * @param rs
     * @return
     */
    private ArrayList<String> resultLabels(QueryResultSet rs) {
    	org.dbwiki.data.schema.SchemaNodeList schemaChildren = ((GroupSchemaNode)rs.schema()).children();
    	ArrayList<String> labels = new ArrayList<String>(schemaChildren.size());
    	for(int i = 0; i < schemaChildren.size(); i++) {
    		labels.add(schemaChildren.get(i).label());
    	}
    	return labels;
    }
    
    /**
     * Output HTML / JavaScript to draw a chart
     * using the supplied query result set
     * which should be a list of (string, number, ..., number) tuples.
     * 
     * This function won't necessarily do the right thing if
     * multiple results are returned for the same column (as is allowed
     * by the system).
     * 
     * The function relies on the schema labels appearing in the
     * correct order. But it does not rely on the result columns
     * appearing in the correct order - they sometimes don't
     * because DatabaseElementLists are not lists!
     * 
     * @param rs
     * @param body
     */
    private void drawChart(ChartType type, String xSize, String ySize, QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
       	int schemaSize = labels.size();
    
       	if(schemaSize < 2) {
    		body.add("<p>Error: a chart needs at least two data columns</p>");
    		return;
    	}
    	
    	ArrayList<String> escapedLabels = new ArrayList<String>(schemaSize);
    	for(String l : labels)
    		escapedLabels.add("'" + escapeString(l) + "'");
    	
    	Iterator<String> p = escapedLabels.iterator();
    	p.next();
    	String xl = escapedLabels.get(0);
    	String yl = "[" + stringConcat(p, ", ") + "]";
		
    	String typeString = "column";
    	switch (type) {
    	
    	
    	case Combo:
			   typeString = "combo";
			    break;
    	case Treemap:
			   typeString = "treemap";
			    break;	
    	case Gauge:
			   typeString = "gauge";
			    break;	
    	case Candlestick:
    			typeString = "candlestick";
    			break;
    		case Column:
    			typeString = "column";
    			break;
    		case Line:
    			typeString = "line";
    			break;
    		case Scatter:
    			typeString = "scatter";
    			break;
    		case Area:
    			typeString = "area";
    			break;
    		case Pie:
    			typeString = "pie";
    			break;
    		case Geo:
    			typeString = "geo";
    			break;
    		case ITMap:
    			typeString = "itmap";
    			break;
    	};
    	
		String chartId = freshName("chart");
		body.add("<div id=\"" + chartId + "\", style=\"width:" +
				                 xSize + "px; height:" + ySize + "px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
						"', " + xl + ", " + yl + ",\n[");
		System.out.println("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
				"', " + xl + ", " + yl + ",\n[");
		//System.out.println(",\n[");
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);

			DatabaseElementList children = r.children();
			if(children.size() < 2)
				continue;

			DatabaseElementList matches = children.get(labels.get(0));
			if(matches.size() == 0)
				continue;
			
			// take the first value (the system allows multiple values)
			DatabaseAttributeNode rx = (DatabaseAttributeNode)matches.get(0);
			//String x = "'" + escapeString(rx.value().getCurrent().value()) + "'";
			String x = null;
			if (typeString.equals("scatter"))
			{
				x= escapeString(rx.value().getCurrent().value());
			 }
			else
			{
				x= "'" + escapeString(rx.value().getCurrent().value()) + "'";
			}

			ArrayList<String> yvalues = new ArrayList<String>(schemaSize-1);

			for (int j = 1; j < schemaSize; j++) {
				// Add the empty string if there are no matches
				// or the result isn't a number.
				// Otherwise add the first matching number.

				matches = children.get(labels.get(j));
				if(matches.size() == 0) {
					yvalues.add("");
					continue;
				}

				String y = ((DatabaseAttributeNode)(matches.get(0))).value().getCurrent().value();
				if (!y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					y = "";
				yvalues.add(y);
			}

			String y = "[" + stringConcat(yvalues, ", ") + "]";

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";
			
			body.add(prefix + "{x : " + x + ", y : " + y + "}");
			System.out.println(prefix + "{x : " + x + ", y : " + y + "}");
			nonEmpty = true;
		}
		
		body.add("])");
		System.out.println("])");
		body.add("</script>");
    }
    
    /*
     *   To draw charts for version comparing
     */
    private void drawVersionCompareChart(ChartType type, String xSize, String ySize, QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
       	int schemaSize = labels.size();
    
       	if(schemaSize < 2) {
    		body.add("<p>Error: a chart needs at least two data columns</p>");
    		return;
    	}
    	
    	ArrayList<String> escapedLabels = new ArrayList<String>(schemaSize);
    	for(String l : labels)
    		escapedLabels.add("'" + escapeString(l) + "'");
    	
    	Iterator<String> p = escapedLabels.iterator();
    	p.next();
    	String xl = escapedLabels.get(0);
    	String yl = "[" + stringConcat(p, ", ") + "]";


		
    	String typeString = "column";
    	switch (type) {
    		case Column:
    			typeString = "column";
    			break;
    		case Line:
    			typeString = "line";
    			break;
    	};
    	
		String chartId = freshName("chart");
		body.add("<div id=\"" + chartId + "\", style=\"width:" +
				                 xSize + "px; height:" + ySize + "px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
						"', " + xl + ", " + yl + ",\n[");
		
		DatabaseGroupNode r1 = (DatabaseGroupNode)rs.get(0);
		DatabaseElementList children1 = r1.children();
		DatabaseElementList matches1 = children1.get(labels.get(0));
		
		// The number of versions
		int childnodeSize = ((DatabaseAttributeNode)(children1.get(1))).value().size();
		//System.out.println("childnodeSize: "+childnodeSize);

				
		boolean nonEmpty = false;
		
		for (int i = 0; i < childnodeSize; i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(0);

			DatabaseElementList children = r.children();
			if(children.size() < 2)
				continue;

			DatabaseElementList matches = children.get(labels.get(0));
			if(matches.size() == 0)
				continue;
			
			// take the first value (the system allows multiple values)
			DatabaseAttributeNode rx = (DatabaseAttributeNode)matches.get(0);
			//String x = "'" + escapeString(rx.value().get(0).value()) + "'";
			int _start = rs.getTimestamp().get(0).start()+i;
			String x = "'" + "Version"+_start + "'";
			
		

			ArrayList<String> yvalues = new ArrayList<String>(schemaSize-1);

			for (int j = 1; j < schemaSize; j++) {
				// Add the empty string if there are no matches
				// or the result isn't a number.
				// Otherwise add the first matching number.

				matches = children.get(labels.get(j));
				if(matches.size() == 0) {
					yvalues.add("");
					continue;
				}

				String y = ((DatabaseAttributeNode)(matches.get(0))).value().get(i).value();
				if (!y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					y = "";
				yvalues.add(y);
			}

			String y = "[" + stringConcat(yvalues, ", ") + "]";

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";
			body.add(prefix + "{x : " + x + ", y : " + y + "}");
			System.out.println(prefix + "{x : " + x + ", y : " + y + "}");
			nonEmpty = true;
		}
		
		
		body.add("])");
		body.add("</script>");
    }
    
    /**
     * To draw charts to compare data by date
     * 
     */
    
    private void drawChartVersion(ChartType type, String xSize, String ySize, QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
       	int schemaSize = labels.size();
    
       	if(schemaSize < 2) {
    		body.add("<p>Error: a chart needs at least two data columns</p>");
    		return;
    	}
    	
    	ArrayList<String> escapedLabels = new ArrayList<String>(schemaSize);
    	for(String l : labels)
    		escapedLabels.add("'" + escapeString(l) + "'");
    	
    	Iterator<String> p = escapedLabels.iterator();
    	p.next();
    	String xl = escapedLabels.get(1);
    	p.next();
    	String yl = "[" + stringConcat(p, ", ") + "]";


		
    	String typeString = "column";
    	switch (type) {
    		case Column:
    			typeString = "column";
    			break;
    		case Line:
    			typeString = "line";
    			break;
    		case Area:
    			typeString = "area";
    			break; 
    		case Scatter:
    			typeString = "scatter";
    			break;
    	};
    	
		String chartId = freshName("chart");
		body.add("<div id=\"" + chartId + "\", style=\"width:" +
				                 xSize + "px; height:" + ySize + "px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
						"', " + xl + ", " + yl + ",\n[");
		
		DatabaseGroupNode r1 = (DatabaseGroupNode)rs.get(0);
		DatabaseElementList children1 = r1.children();
		DatabaseElementList matches1 = children1.get(labels.get(0));
		
		// The number of versions
		int childnodeSize = ((DatabaseAttributeNode)(children1.get(1))).value().size();
		//System.out.println("childnodeSize: "+childnodeSize);

				
		boolean nonEmpty = false;
		
		for (int i = 0; i < childnodeSize; i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(0);

			DatabaseElementList children = r.children();
			if(children.size() < 2)
				continue;

			DatabaseElementList matches = children.get(labels.get(1));
			if(matches.size() == 0)
				continue;
			
			// take the first value (the system allows multiple values)
			DatabaseAttributeNode rx = (DatabaseAttributeNode)matches.get(0);
			
			String x = null;
			
			//If the typeString equals scatter, remove ' '
			if (typeString.equals("scatter"))
			{
				 x = escapeString(rx.value().get(i).value());
			} else
				{	
				 x = "'" + escapeString(rx.value().get(i).value()) + "'";
				}
			
			int _start = rs.getTimestamp().get(0).start()+i;
			//String newX = "'" + ((DatabaseAttributeNode)(matches.get(0))).value().get(i).value()+"'";
			//String x = "'" + ((DatabaseAttributeNode)(children.get(1))).value().get(i).value()+"'";
		

			ArrayList<String> yvalues = new ArrayList<String>(schemaSize-1);
			double Sy = 0.0;

			for (int j = 2; j < schemaSize; j++) {
				// Add the empty string if there are no matches
				// or the result isn't a number.
				// Otherwise add the first matching number.

				matches = children.get(labels.get(j));
				if(matches.size() == 0) {
					yvalues.add("");
					continue;
				}

				String y = ((DatabaseAttributeNode)(matches.get(0))).value().get(i).value();
				if (!y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					y = "";
				yvalues.add(y);
				if(typeString.equals("scatter"))
				{Sy = Double.parseDouble(y);}
			}

			String y = "[" + stringConcat(yvalues, ", ") + "]";

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";
			body.add(prefix + "{x : " + x + ", y : " + y + "}");
			System.out.println(prefix + "{x : " + x + ", y : " + y + "}");
			
			nonEmpty = true;
		}
		
		
		body.add("])");
		body.add("</script>");
    }
  
    /**
     *  Draw line, column, area, and scatter charts by date
     */
    private void drawNumericChart(ChartType type, String xSize, String ySize, QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
       	int schemaSize = labels.size();
    
       	if(schemaSize < 2) {
    		body.add("<p>Error: a chart needs at least two data columns</p>");
    		return;
    	}
    	
    	ArrayList<String> escapedLabels = new ArrayList<String>(schemaSize);
    	for(String l : labels)
    		escapedLabels.add("'" + escapeString(l) + "'");
    	
    	Iterator<String> p = escapedLabels.iterator();
    	p.next();
    	String xl = escapedLabels.get(0);
    	String yl = "[" + stringConcat(p, ", ") + "]";
		
    	String typeString = "column";
    	switch (type) {
    		case Column:
    			typeString = "column";
    			break;
    		case Area:
    			typeString = "area";
    			break;
    		case Line:
    			typeString = "line";
    			break;
    		case Pie:
    			typeString = "pie";
    			break;
    		case Scatter:
    			typeString = "scatter";
    			break;
    		case Geo:
    			typeString = "geo";
    			break;
    	};
    	
		String chartId = freshName("chart");
		body.add("<div id=\"" + chartId + "\", style=\"width:" +
				                 xSize + "px; height:" + ySize + "px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
						"', " + xl + ", " + yl + ",\n[");
		System.out.println("  drawChart('" + typeString + "', '" + chartId + "', '', '" + xSize + "', '"+ ySize +
				"', " + xl + ", " + yl + ",\n[");
		//System.out.println(",\n[");
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);

			DatabaseElementList children = r.children();
			if(children.size() < 2)
				continue;

			DatabaseElementList matches = children.get(labels.get(1));
			if(matches.size() == 0)
				continue;
			
			// take the first value (the system allows multiple values)
			DatabaseAttributeNode rx = (DatabaseAttributeNode)matches.get(0);
			
			String x = null;
			
			// if the typeString equals scatter, cast type from String to double
			if (typeString.equals("scatter"))
			{
				x= escapeString(rx.value().getCurrent().value());
			 }
			else
			{
				x= "'" + escapeString(rx.value().getCurrent().value()) + "'";
			}
			
			ArrayList<String> yvalues = new ArrayList<String>(schemaSize-1);

			for (int j = 2; j < schemaSize; j++) {
				// Add the empty string if there are no matches
				// or the result isn't a number.
				// Otherwise add the first matching number.

				matches = children.get(labels.get(j));
				if(matches.size() == 0) {
					yvalues.add("");
					continue;
				}

				String y = ((DatabaseAttributeNode)(matches.get(0))).value().getCurrent().value();
				if (!y.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					y = "";
				yvalues.add(y);

			}

			String y = "[" + stringConcat(yvalues, ", ") + "]";

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";
			
			body.add(prefix + "{x : " + x + ", y : " + y + "}");
			System.out.println(prefix + "{x : " + x + ", y : " + y + "}");
			nonEmpty = true;
		}
		
		body.add("])");
		System.out.println("])");
		body.add("</script>");
    }
    
    /**
     * Output HTML / JavaScript to draw a map
     * using the supplied query result set
     * which should be a list of locations
     * 
     * This function supports multiple columns in the result set.
     * Multiple columns are concatenated together (delimited by commas)
     * before geocoding.
     * 
     * It relies on the schema columns appearing in the correct order, but
     * does not rely on the data columns being in the correct order.
     * 
     * @param rs
     * @param body
     */
    private void drawMap(QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
    	
    	if(labels.size() == 0) {
    		body.add("<p>Error: a map needs at least one data column</p>");
    		return;
    	}
    	
    	String mapId = freshName("map");
		body.add("<div id=\"" + mapId +"\" style=\"width:400px; height:300px;\">&nbsp;</div>");
		body.add("<script>");
		body.add("  drawMap('"+ mapId +"',\n[");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);

			DatabaseElementList children = r.children();
			if(children.size() == 0)
				continue;

			ArrayList<String> components = new ArrayList<String>();
			for(int j = 0; j < labels.size(); j++) {
				DatabaseElementList matches = children.get(labels.get(j));
				for (int k = 0; k < matches.size(); k++) {
					components.add(((DatabaseAttributeNode)(matches.get(k))).value().getCurrent().value());
				}
			}
			String location = stringConcat(components, ", ");

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";

			body.add(prefix + "'" + escapeString(location) + "'");
			System.out.println(prefix + "'" + escapeString(location) + "'");
			nonEmpty = true;
		}
		
		body.add("])");
		body.add("</script>");
    }
  
    /**
     * Draw map test
     * @param source
     * @return
     */
/*    
    private void drawMap(QueryResultSet rs, HtmlLinePrinter body) {
    	ArrayList<String> labels = resultLabels(rs);
    	
    	if(labels.size() == 0) {
    		body.add("<p>Error: a map needs at least one data column</p>");
    		return;
    	}
    	
    	String mapId = freshName("map");
		body.add("<div id=\"" + mapId +"\" style=\"width:400px; height:300px;\">&nbsp;</div>");	
		body.add("<script>");
		body.add("  drawMap()\n");
		body.add("  drawMap('"+ mapId +"',\n[");
		
		boolean nonEmpty = false;
		for (int i = 0; i < rs.size(); i++) {
			DatabaseGroupNode r = (DatabaseGroupNode)rs.get(i);

			DatabaseElementList children = r.children();
			if(children.size() == 0)
				continue;

			ArrayList<String> components = new ArrayList<String>();
			for(int j = 0; j < labels.size(); j++) {
				DatabaseElementList matches = children.get(labels.get(j));
				for (int k = 0; k < matches.size(); k++) {
					components.add(((DatabaseAttributeNode)(matches.get(k))).value().getCurrent().value());
				}
			}
			String location = stringConcat(components, ", ");

			String prefix;
			if(nonEmpty) prefix = ",\n";
			else prefix = "";

			body.add(prefix + "'" + escapeString(location) + "'");
			nonEmpty = true;
		}
	
		body.add("])");

		body.add("window.onload = drawmap");
		body.add("</script>");		
    }
*/   
    private VisualisationNode parseVisualisationQuery(String source) {
    	org.dbwiki.data.query.visual.Parser parser =
    		Parboiled.createParser(org.dbwiki.data.query.visual.Parser.class, Extensions.NONE);
        ParsingResult<org.dbwiki.data.query.visual.Node> result = parser.parse(source);
        if (result.hasErrors()) {
            throw new RuntimeException("Internal error during markdown parsing:\n--- ParseErrors ---\n" +
                    printParseErrors(result)
            );
        }

        return (VisualisationNode)(result.resultValue);
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
    		String query = _queryString;
    		if(query.toLowerCase().startsWith("ignore:")) {
    			return;
    		}else if(query.toLowerCase().startsWith("candlestick:") || query.toLowerCase().startsWith("candlestick(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("candlestick".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Candlestick, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("combo:") || query.toLowerCase().startsWith("combo(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("combo".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Combo, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("treemap:") || query.toLowerCase().startsWith("treemap(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("treemap".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Treemap, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("gauge:") || query.toLowerCase().startsWith("gauge(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("gauge".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Gauge, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("chart:") || query.toLowerCase().startsWith("chart(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("chart".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Column, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("chartversion:") || query.toLowerCase().startsWith("chartversion(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("chartversion".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChartVersion(ChartType.Column, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("scatter:") || query.toLowerCase().startsWith("scatter(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("scatter".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Scatter, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("scatterversion:") || query.toLowerCase().startsWith("scatterversion(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("scatterversion".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChartVersion(ChartType.Scatter, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("line:") || query.toLowerCase().startsWith("line(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("line".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Line, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("lineversion:") || query.toLowerCase().startsWith("lineversion(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("lineversion".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChartVersion(ChartType.Line, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("area:") || query.toLowerCase().startsWith("area(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("area".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Area, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("compareversionchart:") || query.toLowerCase().startsWith("compareversionchart(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("compareversionchart".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawVersionCompareChart(ChartType.Column, xSize, ySize, rs, body);
    		}   else if(query.toLowerCase().startsWith("compareversionline:") || query.toLowerCase().startsWith("compareversionline(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("compareversionline".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawVersionCompareChart(ChartType.Line, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("chartbydate:") || query.toLowerCase().startsWith("chartbydate(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("chartbydate".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawNumericChart(ChartType.Column, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("linebydate:") || query.toLowerCase().startsWith("linebydate(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("linebydate".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawNumericChart(ChartType.Line, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("numbricscatter:") || query.toLowerCase().startsWith("numbricscatter(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("numbricscatter".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawNumericChart(ChartType.Scatter, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("areabydate:") || query.toLowerCase().startsWith("areabydate(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("areabydate".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);
    				
    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);
    				
    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawNumericChart(ChartType.Area, xSize, ySize, rs, body);
    		}else if(query.toLowerCase().startsWith("pie:") || query.toLowerCase().startsWith("pie(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("pie".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);

    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);

    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Pie, xSize, ySize, rs, body);
    		} else if(query.toLowerCase().startsWith("geo:") || query.toLowerCase().startsWith("geo(")) {
    			// FIXME: should parse the arguments to charts in a more
    			// sensible scalable way.
    			query = query.substring("geo".length());
    			String xSize = "800";
    			String ySize = "600";
    			if(query.startsWith("(")) {
    				int comma = query.indexOf(",");
    				int closeParen = query.indexOf(")", comma);
    				int colon = query.indexOf(":", closeParen);

    				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
    					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);

    				xSize = query.substring(1, comma);
    				ySize = query.substring(comma+1, closeParen);
    				query = query.substring(colon+1);
    			} else {
    				query = query.substring(1);
    			}
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawChart(ChartType.Geo, xSize, ySize, rs, body);
    		 }  else if(query.toLowerCase().startsWith("itmap:") || query.toLowerCase().startsWith("itmap(")) {
     			// FIXME: should parse the arguments to charts in a more
     			// sensible scalable way.
     			query = query.substring("itmap".length());
     			String xSize = "800";
     			String ySize = "600";
     			if(query.startsWith("(")) {
     				int comma = query.indexOf(",");
     				int closeParen = query.indexOf(")", comma);
     				int colon = query.indexOf(":", closeParen);

     				if(comma == -1 || closeParen == -1 || colon != closeParen+1)
     					throw new WikiQueryException(WikiQueryException.UnknownQueryFormat, _queryString);

     				xSize = query.substring(1, comma);
     				ySize = query.substring(comma+1, closeParen);
     				query = query.substring(colon+1);
     			} else {
     				query = query.substring(1);
     			}
     			QueryResultSet rs = database.query(query);
     			body.openPARAGRAPH(CSS.CSSPageText);
     			drawChart(ChartType.ITMap, xSize, ySize, rs, body);
     		 }else if(query.toLowerCase().startsWith("map:")) {
    			query = query.substring("map:".length());
    			QueryResultSet rs = database.query(query);
    			body.openPARAGRAPH(CSS.CSSPageText);
    			drawMap(rs, body);
    		 }	else if(query.toLowerCase().startsWith("heatmap:")) {
        			query = query.substring("heatmap:".length());
        			QueryResultSet rs = database.query(query);
        			body.openPARAGRAPH(CSS.CSSPageText);
        			drawMap(rs,body);
    		 } else {
    			QueryResultSet rs = database.query(query);
    			if (!rs.isEmpty()) {
    				body.openPARAGRAPH(CSS.CSSPageText);
    				if (rs.isElement()) {
    					RequestParameterVersion versionParameter = null;
    					if (rs.hasTimestamp()) {
    						versionParameter = new RequestParameterVersionTimestamp(rs.getTimestamp());
    					} else {
    						versionParameter = new RequestParameterVersionCurrent();
    					}
    					body.add(contentPrinter.getLinesForNodeList(new SchemaNodeList(rs),
    																versionParameter));
    				} else {
    					for (int i = 0; i < rs.size(); i++) {
    						contentPrinter.printTextNode((DatabaseTextNode)rs.get(i), body);
    					}
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
