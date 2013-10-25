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

import java.util.Vector;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.exception.WikiException;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;


import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;

/** 
 * Printer that emits object annotations along with a mini-form for adding annotations
 * FIXME #annotations: This might be better handled in Annotation class
 * @author jcheney
 *
 */
public class ObjectMapPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiDataRequest _request;	

	private DatabaseIdentifier _databaseIdentifier;
	private DatabaseLayouter _layouter;
	private DatabaseNode _node;
	private RequestParameterVersion _versionParameter;
	/*
	 * Constructors
	 */
	
	public ObjectMapPrinter(WikiDataRequest request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		_request = request;
		
		_databaseIdentifier = request.wri().databaseIdentifier();
		_layouter = layouter;
		
		_node = request.node();
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		//body.openDIVID("map_label", "");
		//body.closeDIV();
		
		body.openDIVID("map_canvas", "height:400px;");
		body.closeDIV();
		
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
	
	/** Generates HTML lines corresponding to the nodes in list.
	 * versionParameter says which version(s) to use.
	 */
	public HtmlPage getLinesForNodeList(SchemaNodeList list, RequestParameterVersion versionParameter) throws org.dbwiki.exception.WikiException {
		HtmlLinePrinter content = new HtmlLinePrinter();
		boolean hasContent = false;
		
		SchemaLayout layout = _layouter.get(list.schema());

		// Dispatching based on layout style for this node's type
		if ((layout.getDisplayStyle().isGroupStyle()) || ((layout.getDisplayStyle().isTableStyle()) && (list.schema().isAttribute()))) {
			hasContent = printNodesInGroupStyle(list, versionParameter, layout, content);
		} else if (layout.getDisplayStyle().isListStyle()) {
			hasContent = printNodesInListStyle(list, versionParameter, layout, content);
		} else if (layout.getDisplayStyle().isTableStyle()) {
			hasContent = printNodesInTableStyle(list, versionParameter, layout, content);
		}
		
		if (hasContent) {
			return content.lines();
		} else {
			return new HtmlPage();
		}
	}

	public void printAttributeNode(DatabaseAttributeNode node, RequestParameterVersion versionParameter, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		SchemaLayout layout = _layouter.get(node.schema());
		
		boolean active = node.getTimestamp().isCurrent();

		if (active) {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
		} else {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameInactive));
		}
		
		body.openTR();
		
		if (active) {
			body.openTD(layout.getCSS(CSS.CSSObjectLabelActive));
		} else {
			body.openTD(layout.getCSS(CSS.CSSObjectLabelInactive));
		}
		body.text(layout.getName());
		body.closeTD();
		
		if (active) {
			body.openTD(layout.getCSS(CSS.CSSObjectValueActive));
		} else {
			body.openTD(layout.getCSS(CSS.CSSObjectValueInactive));
		}
		
		printAttributeValue(node, versionParameter, layout, body);
		
		body.closeTD();
		
		body.closeTR();
		
		body.closeTABLE();
	}

	public void printGroupNode(DatabaseGroupNode node, RequestParameterVersion versionParameter, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		
		SchemaLayout layout = _layouter.get(node.schema());		
		//body.add("<p CLASS=\"" + CSS.CSSMapLink + "\"><a CLASS=\"" + CSS.CSSMapLink + "\" HREF=\"#\" ONCLICK=\"dspToggleMapForm(); return false;\">Choose an attribute</a></p>");
		body.add("<p CLASS=\"" + CSS.CSSMapLink + "\">Choose a value for map rendering:</p>");
		
		//print a copy of the content
		body.openTABLE(layout.getCSS(CSS.CSSObjectFrame));
		SchemaNodeListIndex children = new SchemaNodeListIndex(node, _layouter);
		for (int iEntity = 0; iEntity < children.size(); iEntity++) {
			// filtering of versions handled in getLinesForNodeList
			
			//children.get(iEntity).get(0) is the value of each row			
			HtmlPage lines = getLinesForNodeList(children.get(iEntity), versionParameter);
			if (lines.size() > 0) {
				body.openTR();
				body.openTD(layout.getCSS(CSS.CSSObjectListing));
				body.add(lines);
				body.closeTD();
				body.closeTR();
			}
		}
		body.closeTABLE();
		
	}

	public void printTextNode(DatabaseTextNode node, HtmlLinePrinter body) {
		SchemaLayout layout = _layouter.get(node.parent().schema());
		
		boolean active = node.getTimestamp().isCurrent();
		if (active) {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
		} else {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameInactive));
		}
		body.openTR();
		if (active) {
			body.openTD(layout.getCSS(CSS.CSSObjectValueActive));
		} else {
			body.openTD(layout.getCSS(CSS.CSSObjectValueInactive));
		}
		body.linkWithOnClick("#", "codeAddress(this)", node.value(), layout.getCSS(CSS.CSSContentValueActive));
		//body.text(node.value());
		if (node.hasAnnotation()) {
			addAnnotationIndicator(body);
		}
		body.closeTD();
		body.closeTR();
		body.closeTABLE();
	}
	
	
	/*
	 * Private Methods
	 */
	
	/** Gets a URL that links to the given node at the given version
	 * 
	 */
	private String getNodeLink(DatabaseNode node, RequestParameterVersion versionParameter) {
		String target = _databaseIdentifier.linkPrefix() + node.identifier().toURLString();
		if (!versionParameter.versionCurrent()) {
			target = target + "?" + versionParameter.toURLString();
		}
		return target;
	}
	
	private boolean printAttributeValue(DatabaseAttributeNode attribute, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		int lineCount = 0;
		
		//value is the value of each attribute
		for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
			DatabaseTextNode value = attribute.value().get(iValue);
			if (versionParameter.matches(value)) {
				lineCount++;
				if (lineCount > 1) {
					content.add("<br/>");
				}
				String target = getNodeLink(value, versionParameter);
				
				//add a parameter of address to the url
				//target = target + "?address='" + value.text().trim() + "'";
				
				if (value.getTimestamp().isCurrent()) {
					content.linkWithOnClick("#", "codeAddress(this)", value.text(), layout.getCSS(CSS.CSSContentValueActive));
				} else {
					content.linkWithOnClick("#", "codeAddress(this)", value.text(), layout.getCSS(CSS.CSSContentValueInactive));
				}
				if ((attribute.hasAnnotation()) || (value.hasAnnotation())) {
					addAnnotationIndicator(content);
				}
			}
		}
		return (lineCount > 0);
	}

	private boolean printGroupValue(DatabaseGroupNode group, String linkTarget, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		
		if (versionParameter.matches(group)) {
			if (group.getTimestamp().isCurrent()) {
				content.openTD(layout.getCSS(CSS.CSSContentValueActive));
				//content.linkWithTitle(linkTarget, group.getTimestamp().toPrintString(), "foo1"+layout.getLabel(group, versionParameter), layout.getCSS(CSS.CSSContentValueActive));
			} else {
				content.openTD(layout.getCSS(CSS.CSSContentValueInactive));
				//content.linkWithTitle(linkTarget, group.getTimestamp().toPrintString(), "foo1"+layout.getLabel(group, versionParameter), layout.getCSS(CSS.CSSContentValueInactive));
			}
			if (group.hasAnnotation()) {
				addAnnotationIndicator(content);
			}

			if (layout.getShowContent() == true) {
				content.openTABLE(layout.getCSS(CSS.CSSContentFrame));
				
				// Display the children
				SchemaNodeListIndex children = new SchemaNodeListIndex(group, _layouter);
				for (int iEntity = 0; iEntity < children.size(); iEntity++) {
					HtmlPage lines = getLinesForNodeList(children.get(iEntity), versionParameter);
					if (lines.size() > 0) {
						content.openTR();
						content.openTD(layout.getCSS(CSS.CSSContentListing));
						content.add(lines);
						content.closeTD();
						content.closeTR();
					}
				}
				
				content.closeTABLE();
			}
			hasContent = true;
			content.closeTD();
		}
		
		return hasContent;
	}
	
	private boolean printNodesInGroupStyle(SchemaNodeList list, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		
		boolean active = list.isActive();
		if (active) {
			content.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
		} else {
			content.openTABLE(layout.getCSS(CSS.CSSContentFrameInactive));
		}
		
		
		content.openTR();
		
		if (layout.getLabelAlignment().isTopAlign()) {
			if (active) {
				content.openTD(layout.getCSS(CSS.CSSContentTopLabelActive));
			} else {
				content.openTD(layout.getCSS(CSS.CSSContentTopLabelInactive));
			}
			content.text(layout.getName());
			content.closeTD();
			content.closeTR();
			content.openTR();
		} else if (layout.getLabelAlignment().isLeftAlign()) {
			if (active) {
				content.openTD(layout.getCSS(CSS.CSSContentLeftLabelActive));
			} else {
				content.openTD(layout.getCSS(CSS.CSSContentLeftLabelInactive));
			}
			content.text(layout.getName());
			content.closeTD();
		}

		content.openTD(layout.getCSS(CSS.CSSContentValue));
		content.openTABLE(layout.getCSS(CSS.CSSContentValueListing));
		
		for (int iNode = 0; iNode < list.size(); iNode++) {
			DatabaseElementNode element = list.get(iNode);
			if (versionParameter.matches(element)) {
				content.openTR();
				String target = getNodeLink(element, versionParameter);
				if (list.schema().isAttribute()) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
					if (attribute.getTimestamp().isCurrent()) {
						content.openTD(layout.getCSS(CSS.CSSContentValueActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentValueInactive));
					}
					if (printAttributeValue(attribute, versionParameter, layout, content)) {
						hasContent = true;
					}
					content.closeTD();
				} else {
					if (printGroupValue((DatabaseGroupNode)element, target, versionParameter, layout, content)) {
						hasContent = true;
					}
				}
				content.closeTR();
			}
		}

		content.closeTABLE();
		content.closeTD();
			
		content.closeTR();
		content.closeTABLE();
		
		return hasContent;
	}
	
	private boolean printNodesInListStyle(SchemaNodeList list, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		
		content.openTABLE(layout.getCSS(CSS.CSSContentFrameListing));

		for (int iNode = 0; iNode < list.size(); iNode++) {
			DatabaseElementNode element = list.get(iNode);
			if (versionParameter.matches(element)) {
				boolean active = element.getTimestamp().isCurrent();
				content.openTR();
				content.openTD(layout.getCSS(CSS.CSSContentFrameListing));
				
				String target = getNodeLink(element, versionParameter);
				if (element.getTimestamp().isCurrent()) {
					content.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
				} else {
					content.openTABLE(layout.getCSS(CSS.CSSContentFrameInactive));
				}
				content.openTR();
				if (layout.getLabelAlignment().isTopAlign()) {

					if (active) {
						content.openTD(layout.getCSS(CSS.CSSContentTopLabelActive));
						//content.link(target, layout.getLabel(element, versionParameter), layout.getCSS(CSS.CSSContentLabelActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentTopLabelInactive));
						//content.link(target, layout.getLabel(element, versionParameter), layout.getCSS(CSS.CSSContentLabelInactive));
					}
					content.text(layout.getName());
					if (element.hasAnnotation()) {
						addAnnotationIndicator(content);
					}
					content.closeTD();
					content.closeTR();
					content.openTR();
				} else if (layout.getLabelAlignment().isLeftAlign()) {
					if (active) {
						content.openTD(layout.getCSS(CSS.CSSContentLeftLabelActive));
						
						//map rendering->attribute link
						//content.link("#", layout.getLabel(element, versionParameter), layout.getCSS(CSS.CSSContentLabelActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentLeftLabelInactive));
						//content.link("#", layout.getLabel(element, versionParameter), layout.getCSS(CSS.CSSContentLabelInactive));
					}
					content.text(layout.getName());
					if (element.hasAnnotation()) {
						addAnnotationIndicator(content);
					}
					content.closeTD();
				}
				content.openTD(layout.getCSS(CSS.CSSContentValue));
				content.openTABLE(layout.getCSS(CSS.CSSContentValueListing));
				content.openTR();
				if (list.schema().isAttribute()) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
					if (attribute.getTimestamp().isCurrent()) {
						content.openTD(layout.getCSS(CSS.CSSContentValueActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentValueInactive));
					}
					if (printAttributeValue(attribute, versionParameter, layout, content)) {
						hasContent = true;
					}
					content.closeTD();
				} else {
					if (printGroupValue((DatabaseGroupNode)element, target, versionParameter, layout, content)) {
						hasContent = true;
					}
				}
				content.closeTR();
				content.closeTABLE();
				content.closeTD();
				content.closeTR();
				content.closeTABLE();
				content.closeTD();
				content.closeTR();
			}
		}
		
		content.closeTABLE();

		return hasContent;
	}

	private boolean printNodesInTableStyle(SchemaNodeList list, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		boolean hasContent = false;
		
		boolean active = list.isActive();
		
		if (active) {
			content.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
		} else {
			content.openTABLE(layout.getCSS(CSS.CSSContentFrameInactive));
		}
	
		content.openTR();
		if (layout.getLabelAlignment().isTopAlign()) {
			if (active) {
				content.openTD(layout.getCSS(CSS.CSSContentTopLabelActive));
			} else {
				content.openTD(layout.getCSS(CSS.CSSContentTopLabelInactive));
			}
			content.text(layout.getName());
			content.closeTD();
			content.closeTR();
			content.openTR();
		} else if (layout.getLabelAlignment().isLeftAlign()) {
			if (active) {
				content.openTD(layout.getCSS(CSS.CSSContentLeftLabelActive));
			} else {
				content.openTD(layout.getCSS(CSS.CSSContentLeftLabelInactive));
			}
			content.text(layout.getName());
			content.closeTD();
		}

		content.openTD(layout.getCSS(CSS.CSSContentValue));
		
		GroupSchemaNode schemaNode = (GroupSchemaNode)list.schema();
		
		content.openTABLE(layout.getCSS(CSS.CSSContentTable));
			
		content.openTR();
		for (int iColumn = 0; iColumn < schemaNode.children().size(); iColumn++) {
			SchemaNode columnSchemaNode = schemaNode.children().get(iColumn);
			// only display columns for entities that have not been deleted
			if (versionParameter.matches(columnSchemaNode)) {
				SchemaLayout schemaLayout = _layouter.get(columnSchemaNode);
				if (active) {
					content.openTH(layout.getCSS(CSS.CSSContentCellActive));
				} else {
					content.openTH(layout.getCSS(CSS.CSSContentCellInactive));
				}
				content.text(schemaLayout.getName());
				content.closeTH();
			}
		}
		content.closeTR();
		
		for (int iNode = 0; iNode < list.size(); iNode++) {
			DatabaseGroupNode groupNode = (DatabaseGroupNode)list.get(iNode);
			// only display nodes that have not been deleted
			if (versionParameter.matches(groupNode)) {
				content.openTR();
				for (int iColumn = 0; iColumn < schemaNode.children().size(); iColumn++) {
					DatabaseElementList nodes = groupNode.children().get(schemaNode.children().get(iColumn));
					if (groupNode.getTimestamp().isCurrent()) {
						content.openTD(layout.getCSS(CSS.CSSContentCellActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentCellInactive));
					}
					boolean hasAnnotation = groupNode.hasAnnotation();
					for (int iElement = 0; iElement < nodes.size(); iElement++) {
						DatabaseElementNode element = nodes.get(iElement);
						hasAnnotation = (hasAnnotation || element.hasAnnotation());
						String target = getNodeLink(element, versionParameter);
						if (element.isAttribute()) {
							printAttributeValue((DatabaseAttributeNode)element, versionParameter, layout, content);
						} else {
							String label = _layouter.get(element.schema()).getLabel(element, versionParameter);
							
							if (element.getTimestamp().isCurrent()) {
								content.linkWithTitle(target, element.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueActive));
							} else {
								content.linkWithTitle(target, element.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueInactive));
							}
						}
					}
					if (hasAnnotation) {
						addAnnotationIndicator(content);
					}
					content.closeTD();
				}
				content.closeTR();
				hasContent = true;
			}
		}
		
		content.closeTABLE();
		content.closeTD();

		content.closeTR();
		content.closeTABLE();

		return hasContent;
	}
	
	private void addAnnotationIndicator(HtmlLinePrinter content) {
		content.addIMG("/pictures/annotation.gif");
	}
}
