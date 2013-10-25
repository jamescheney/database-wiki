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
package org.dbwiki.web.ui.printer.visual;



import java.util.ArrayList;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.resource.SchemaNodeIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.SchemaNodeList;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.exception.WikiException;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.request.WikiVisualRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;
import org.dbwiki.web.ui.printer.SchemaNodeListIndex;

/** Prints schema types as HTML 
 * 
 * @author jcheney
 *
 */
public class VisualNodePrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private RequestParameterVersion _versionParameter;

	private SchemaNode _schemaNode;
	private DatabaseNode _node;
	@SuppressWarnings("rawtypes")
	private ArrayList[] _dataTable;
	private ArrayList<Integer> _schemaNum;
	
	/*
	 * Constructors
	 */
		
	public VisualNodePrinter(WikiVisualRequest request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		request.wri().databaseIdentifier();
		_layouter = layouter;
		_schemaNode = request.schema().parent();
		if(_schemaNode == null) {
			_schemaNode = request.wiki().database().getSchemaNode(new SchemaNodeIdentifier(0));
		}

		_node = request.node();
		_schemaNum = new ArrayList<Integer>();
		
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	public void printGroupSchemaNode(GroupSchemaNode schema, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter body)
	    throws org.dbwiki.exception.WikiException {

		SchemaNodeList children = schema.children();
		printEntitiesInGroupStyle(children, versionParameter, layout, body);
	}
	
	/*
	 * Private Methods
	 */
	
	private void printEntitiesInGroupStyle(SchemaNodeList list, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content)
		throws org.dbwiki.exception.WikiException {
		
		for (int i = 0; i < list.size(); i++) {
			SchemaNode schema = list.get(i);
			
			if (versionParameter.matches(schema)) {
				//content.openTR();
				//String target = linkTarget;
				//target = getEntityLink(entity, versionParameter);
				if (schema.isAttribute()) {
					AttributeSchemaNode attribute = (AttributeSchemaNode)schema;
					
					//add option
					content.addOPTION(attribute.path()+""+attribute.id(), ""+attribute.id(), false);
					_schemaNum.add(attribute.id());
				} else {
					printGroupSchemaNode((GroupSchemaNode)schema, versionParameter, layout, content);
				}
			}
		}
	}
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		
		//two parameters for chart visualisation
		String chart1 = "";
		String chart2 = "";
		
		//add form to print chart
		//body.openFORM("chartForm", "POST", _request.parameters().get(RequestParameter.ParameterResource).value());
		
		//add chart type drop-down list
		body.add("Choose a Chart Type : ");
		body.addLINE("<select name=\"chart_type\" id=\"chart_type\" onChange=\"changeSelect()\">");
		//body.openSELECT("chart_type");
		body.addOPTION("Line Chart (text, number)", "line", false);
		body.addOPTION("Bar Chart (text, number)", "bar", false);
		body.addOPTION("Column Chart (text, number)", "column", false);
		body.addOPTION("Pie Chart (text, number)", "pie", false);
		body.addOPTION("Scatter Chart (number, number)", "scatter", false);
		body.addOPTION("Intensity Map (location, number)", "intensity", false);
		body.addOPTION("Map (location)", "map", false);
		body.addOPTION("Map (latitude, longitude)", "latlng", false);
		body.closeSELECT();
		body.addBR();
		body.addBR();
		
		//add two drop-down lists depending on different chart type
		//drop-down list 1
		body.add("Parameter 1 : ");
		body.addLINE("<select name=\"para1\" id=\"para1\">");
		//body.openSELECT("para1");
		
		if (_versionParameter.matches(_schemaNode)) {
			if (_schemaNode.isAttribute()) {
				//add attributes into the entity path
				body.addOPTION(_schemaNode.path()+""+_schemaNode.id(), ""+_schemaNode.id(), false);
				_schemaNum.add(_schemaNode.id());
				//printAttributeEntity((AttributeEntity)_entity, target, _versionParameter, _layouter.get(_entity), body);
			} else {
				//add children into the entity 
				printGroupSchemaNode((GroupSchemaNode)_schemaNode, _versionParameter, _layouter.get(_schemaNode), body);
			}
		}
		body.closeSELECT();
		body.addBR();
		body.addBR();

		//drop-down list 2
		body.add("Parameter 2 : ");
		body.addLINE("<select name=\"para2\" id=\"para2\">");
		//body.openSELECT("para2");
		if (_versionParameter.matches(_schemaNode)) {
			if (_schemaNode.isAttribute()) {
				//add attributes into the entity path
				body.addOPTION(_schemaNode.path()+""+_schemaNode.id(), ""+_schemaNode.id(), false);
				_schemaNum.add(_schemaNode.id());
				//printAttributeEntity((AttributeEntity)_entity, target, _versionParameter, _layouter.get(_entity), body);
			} else {
				printGroupSchemaNode((GroupSchemaNode)_schemaNode, _versionParameter, _layouter.get(_schemaNode), body);
			}
		}
		body.closeSELECT();
		body.addBR();
		body.addBR();
		
		printChart(chart1, chart2, body);
	}
	
	private void printChart(String chart1, String chart2, HtmlLinePrinter content) throws WikiException {
		content.openCENTER();
		content.addBR();
		//content.addBUTTON("submit", "visualise", "Visualise", "#");
		
		//add button
		//content.linkWithOnClick("#", "getSelect()", "Visualise", _layouter.get(_schemaNode).getCSS(CSS.CSSContentValueActive));
		content.addLINE("<input type=\"submit\" name=\"visualise\" value=\"Visualize\" onclick=\"getSelect()\"/>");
		//content.linkWithOnClick("#", "drawChart()", "Visualise", _layouter.toString());
		content.openDIVID("chart_canvas", "height:400px");
		content.closeDIV();		
		
		//add a hidden field storing the data to be visualized
		printHiddenTable(content);
		
		content.closeCENTER();

	}
	
	private void printHiddenTable(HtmlLinePrinter content) throws WikiException {
		
		if(_schemaNum.isEmpty() == false) {
			//this num contains two parameters options
			int n = _schemaNum.size()/2;
			int m = _schemaNum.get(n-1);
			_dataTable = new ArrayList[m+1];
		}
		else {
			_dataTable = new ArrayList[0];
		}
		for(int i=0; i<_dataTable.length; i++) {
			_dataTable[i] = new ArrayList<String>();
		}
		//add required data into dataTable and return
		printData(content);
		
		for(int i=0; i<_schemaNum.size()/2; i++) {
			
			//n is the schema ids
			int n = _schemaNum.get(i);
			
			if(_dataTable[n].size()>0) {
				content.add("<table id=\"data"+ n +"\" style=\"display:none\">");
				for(int j=0; j<_dataTable[n].size(); j++) {
					content.openTR();
					content.openTD();
					content.text((String) _dataTable[n].get(j));
					content.closeTD();
					content.closeTR();				
				}
				content.add("</table>");
			}
			
			System.out.println("data table " + _schemaNum.get(i) + " size: " + _dataTable[n].size());
		}
	}
	
	private void printData(HtmlLinePrinter body) throws WikiException {
		if (_versionParameter.matches(_node)) {
			if (_node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)_node;
				if (element.isAttribute()) {
					printAttributeNode((DatabaseAttributeNode)_node, _versionParameter, body);
				} else {
					printGroupNode((DatabaseGroupNode)_node, _versionParameter, body);
				}
			} else {
				printTextNode((DatabaseTextNode)_node, body);
			}
		}		
	}
	
	
	@SuppressWarnings("unchecked")
	public void printAttributeNode(DatabaseAttributeNode node, RequestParameterVersion versionParameter, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		SchemaLayout layout = _layouter.get(node.schema());
		_dataTable[node.schema().id()].add(layout.getName());
		System.out.println("put:"+node.schema().id()+" value:"+layout.getName());
		printAttributeValue(node, versionParameter, layout, body);
	}
	
	public void printGroupNode(DatabaseGroupNode node, RequestParameterVersion versionParameter, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		
		SchemaNodeListIndex children = new SchemaNodeListIndex(node, _layouter);
		for (int i = 0; i < children.size(); i++) {
			// filtering of versions handled in getLinesForNodeList
			HtmlPage lines = getLinesForNodeList(children.get(i), versionParameter);
			if (lines.size() > 0) {
				body.add(lines);
			}
		}
		
	}
	
	public HtmlPage getLinesForNodeList(org.dbwiki.web.ui.printer.SchemaNodeList schemaNodeList, RequestParameterVersion versionParameter) throws org.dbwiki.exception.WikiException {
		HtmlLinePrinter content = new HtmlLinePrinter();
		boolean hasContent = false;
		
		SchemaLayout layout = _layouter.get(schemaNodeList.schema());

		hasContent = printNodesInGroupStyle(schemaNodeList, versionParameter, layout, content);		
		
		if (hasContent) {
			return content.lines();
		} else {
			return new HtmlPage();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printTextNode(DatabaseTextNode node, HtmlLinePrinter body) {
		_dataTable[node.parent().schema().id()].add(node.value());
		System.out.println("put:"+node.parent().schema().id()+" text value:"+node.value());
	}	
	
	@SuppressWarnings("unchecked")
	private boolean printAttributeValue(DatabaseAttributeNode attribute, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		int lineCount = 0;
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			DatabaseTextNode value = attribute.value().get(iValue);
			if (versionParameter.matches(value)) {
				lineCount++;
				_dataTable[attribute.schema().id()].add(value.text());
				System.out.println("put:"+attribute.schema().id()+" attribute value:"+value.text());
			}
		}
		return (lineCount > 0);
	}
	
	private boolean printGroupValue(DatabaseGroupNode group, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		if (versionParameter.matches(group)) {
			
			SchemaNodeListIndex children = new SchemaNodeListIndex(group, _layouter);
			for (int i = 0; i < children.size(); i++) {
				HtmlPage lines = getLinesForNodeList(children.get(i), versionParameter);
				if (lines.size() > 0) {
					content.add(lines);
				}
			}
			hasContent = true;
		}
		
		return hasContent;
	}
	
	private boolean printNodesInGroupStyle(org.dbwiki.web.ui.printer.SchemaNodeList schemaNodeList, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		
		for (int iNode = 0; iNode < schemaNodeList.size(); iNode++) {
			DatabaseElementNode element = schemaNodeList.get(iNode);
			if (versionParameter.matches(element)) {
				//String target = getNodeLink(element, versionParameter);
				if (schemaNodeList.schema().isAttribute()) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
					if (printAttributeValue(attribute, versionParameter, layout, content)) {
						hasContent = true;
					}
				} else {
					if (printGroupValue((DatabaseGroupNode)element, versionParameter, layout, content)) {
						hasContent = true;
					}
				}
			}
		}
		
		return hasContent;
	}	
	
}
