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
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.web.html.HtmlLinePrinter;


import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints a form for HTML update or inserting data.
 * See DatabaseWiki for where the form data is parsed.
 * @author jcheney
 *
 */
public class DataUpdateFormPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private WikiDataRequest<?> _request;
	
	
	/*
	 * Constructors
	 */
	
	public DataUpdateFormPrinter(WikiDataRequest<?> request, DatabaseLayouter layouter) {
		_request = request;
		_layouter = layouter;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		if (_request.parameters().hasParameter(RequestParameter.ParameterCreate)) {
			RequestParameter parameter = _request.parameters().get(RequestParameter.ParameterCreate);
			SchemaNode schema = null;
			if (parameter.hasValue()) {
				try {
					schema = _request.wiki().database().schema().get(Integer.parseInt(parameter.value()));
				} catch (NumberFormatException excpt) {
					throw new WikiRequestException(WikiRequestException.InvalidParameterValue, parameter.toString());
				}
			} else {
				schema = _request.wiki().database().schema().root();
			}
			body.paragraph(_layouter.get(schema).getName(), CSS.CSSHeadline);
			this.printInsertForm(_request, schema, body);
		} else if (_request.parameters().hasParameter(RequestParameter.ParameterEdit)) {
			body.paragraph("Edit", CSS.CSSHeadline);
			DatabaseNode node = _request.node();
			if (node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)node;
				this.printUpdateForm(_request, element, body);
			} else {
				DatabaseTextNode textNode = (DatabaseTextNode)node;
				this.printUpdateForm(_request, textNode, body);
			}
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	public String getTextareaLine(String name, String value, int height) {
		return "<textarea name=\"" + RequestParameter.TextFieldIndicator + name + "\" style=\"width: 99%; height: " + height + "px\">" + value + "</textarea>";		
	}

	public String getTextareaLine(SchemaNode schema, int height) {
		return this.getTextareaLine(Integer.toString(schema.id()), "", height);
	}

	private void printInsertForm(WikiDataRequest<?> request, SchemaNode schema, HtmlLinePrinter body) {
		body.openFORM("frmInsert", "POST", request.wri().getURL());
		body.addHIDDEN(RequestParameter.ActionValueSchemaNode, Integer.toString(schema.id()));

		this.printInsertLines(schema, body);

		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();

		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionInsert, "<img src=\"/pictures/button_save.gif\">");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		
		body.closePARAGRAPH();
		body.closeFORM();
	}
	
	public void printInsertLines(SchemaNode schema, HtmlLinePrinter body) {
		SchemaLayout layout = _layouter.get(schema);

		if (schema.isAttribute()) {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
			this.printInsertLine(schema, body);
			body.closeTABLE();
		} else {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrame));
			GroupSchemaNode groupSchemaNode = (GroupSchemaNode)schema;
			for (int iChild = 0; iChild < groupSchemaNode.children().size(); iChild++) {
				SchemaNode child = groupSchemaNode.children().get(iChild);
				// skip deleted entities
				if (!child.getTimestamp().isCurrent())
					continue;
				if (child.isAttribute()) {
					body.openTR();
					body.openTD(layout.getCSS(CSS.CSSObjectListing));
					body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
					this.printInsertLine(child, body);
					body.closeTABLE();
					body.closeTD();
					body.closeTR();
				} else if (_layouter.get(child).getEditWithParent()) {
					GroupSchemaNode groupChild = (GroupSchemaNode)child;
					body.openTR();
					body.openTD(layout.getCSS(CSS.CSSObjectListing));
					body.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
					body.openTR();
					body.openTD(layout.getCSS(CSS.CSSContentTopLabelActive));
					body.text(_layouter.get(groupChild).getName());
					body.closeTD();
					body.closeTR();
					body.openTR();
					body.openTD(CSS.CSSObjectListing);
					this.printInsertLines(groupChild, body);
					body.closeTD();
					body.closeTR();
					body.closeTABLE();
				}
			}
			body.closeTABLE();
		}
	}

	public void printInsertLine(SchemaNode schema, HtmlLinePrinter body) {
		SchemaLayout layout = _layouter.get(schema);
		
		body.openTR();
		body.openTD(layout.getCSS(CSS.CSSObjectLabelActive));
		body.text(layout.getName());
		body.closeTD();
		body.openTD(layout.getCSS(CSS.CSSObjectValueActive));
		body.text(this.getTextareaLine(schema, layout.getTextHeight()));		
		body.closeTD();
		body.closeTR();
	}

	private void printUpdateForm(WikiDataRequest<?> request, DatabaseNode node, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.openFORM("frmInsert", "POST", request.wri().getURL());
		
		if (node.isElement()) {
			body.addHIDDEN(RequestParameter.ActionValueSchemaNode, Integer.toString(((DatabaseElementNode)node).schema().id()));
		} else {
			body.addHIDDEN(RequestParameter.ActionValueSchemaNode, Integer.toString(((DatabaseTextNode)node).parent().schema().id()));
		}

		if (node.isElement()) {
			this.printUpdateLines((DatabaseElementNode)node, body);
		} else {
			SchemaLayout layout = _layouter.get(node.parent().schema());
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
			body.openTR();
			body.openTD(layout.getCSS(CSS.CSSObjectValueActive));
			this.printUpdateTextArea((DatabaseTextNode)node, body);
			body.closeTD();
			body.closeTR();
			body.closeTABLE();
		}

		body.openPARAGRAPH(CSS.CSSButtonLine);
		body.openCENTER();
		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionUpdate, "<img src=\"/pictures/button_save.gif\">");
		body.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		body.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		body.closeCENTER();
		body.closePARAGRAPH();
		body.closeFORM();
	}

	public void printUpdateLines(DatabaseElementNode element, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		SchemaLayout layout = _layouter.get(element.schema());
		if (element.isAttribute()) {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
			this.printUpdateLine((DatabaseAttributeNode)element, body);
			body.closeTABLE();
		} else {
			body.openTABLE(layout.getCSS(CSS.CSSObjectFrame));
			DatabaseGroupNode group = (DatabaseGroupNode)element;
			for (int iChild = 0; iChild < group.children().size(); iChild++) {
				DatabaseElementNode child = group.children().get(iChild);
				if (child.getTimestamp().isCurrent()) {
					if (child.isAttribute()) {
						body.openTR();
						body.openTD(layout.getCSS(CSS.CSSObjectListing));
						body.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
						this.printUpdateLine((DatabaseAttributeNode)child, body);
						body.closeTABLE();
						body.closeTD();
						body.closeTR();
					} else if (_layouter.get(child.schema()).getEditWithParent()) {
						DatabaseGroupNode groupChild = (DatabaseGroupNode)child;
						body.openTR();
						body.openTD(layout.getCSS(CSS.CSSObjectListing));
						body.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
						body.openTR();
						body.openTD(layout.getCSS(CSS.CSSContentTopLabelActive));
						body.text(_layouter.get(groupChild.schema()).getName());
						body.closeTD();
						body.closeTR();
						body.openTR();
						body.openTD(CSS.CSSObjectListing);
						this.printUpdateLines(groupChild, body);
						body.closeTD();
						body.closeTR();
						body.closeTABLE();
					}
				}
			}
			body.closeTABLE();
		}

	}
	public void printUpdateLine(DatabaseAttributeNode attribute, HtmlLinePrinter body) {
		SchemaLayout layout = _layouter.get(attribute.schema());
		
		DatabaseTextNode value = attribute.value().getCurrent();
		if (value != null) {
			body.openTR();
			body.openTD(layout.getCSS(CSS.CSSObjectLabelActive));
			body.text(layout.getName());
			body.closeTD();
			body.openTD(layout.getCSS(CSS.CSSObjectValueActive));
			this.printUpdateTextArea(value, body);		
			body.closeTD();
			body.closeTR();
		}
	}

	private void printUpdateTextArea(DatabaseTextNode value, HtmlLinePrinter body) {
		SchemaLayout layout = _layouter.get(value.parent().schema());

		String text = value.text();
		if (text == null) {
			text = "";
		}

		int height = layout.getTextHeight();
		height = Math.min(Math.max(((text.length() / 80) + 1) * 16, height), 600);
		
		body.text(this.getTextareaLine(value.identifier().toParameterString(), text, height));
	}
}
