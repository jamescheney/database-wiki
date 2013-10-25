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
package org.dbwiki.web.ui.printer.schema;



import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.SchemaNodeIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.SchemaNodeList;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints schema types as HTML 
 * 
 * @author jcheney
 *
 */
public class SchemaNodePrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseIdentifier _databaseIdentifier;
	private DatabaseLayouter _layouter;
	
	private RequestParameterVersion _versionParameter;
	
	private SchemaNode _schemaNode;
	
	/*
	 * Constructors
	 */
		
	public SchemaNodePrinter(WikiSchemaRequest request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		_databaseIdentifier = request.wri().databaseIdentifier();
		_layouter = layouter;
		_schemaNode = request.schema();
		if(_schemaNode == null) {
			_schemaNode = request.wiki().database().getSchemaNode(new SchemaNodeIdentifier(0));
		}
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	public void printGroupSchemaNode(GroupSchemaNode schema, String target, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter body)
	    throws org.dbwiki.exception.WikiException {
		
		body.openTABLE(layout.getCSS(CSS.CSSObjectFrame));
		
		body.openTR();
		body.openTD(layout.getCSS(CSS.CSSObjectListing));
		if (schema.getTimestamp().isCurrent()) {
			body.linkWithTitle(target, schema.getTimestamp().toPrintString(), schema.label(), layout.getCSS(CSS.CSSContentValueActive));
		} else {
			body.linkWithTitle(target, schema.getTimestamp().toPrintString(), schema.label(), layout.getCSS(CSS.CSSContentValueInactive));
		}
		body.closeTD();
		body.closeTR();

		SchemaNodeList children = schema.children();
		body.openTR();
		body.openTD(layout.getCSS(CSS.CSSObjectListing));
		printEntitiesInGroupStyle(children, target, versionParameter, layout, body);
		body.closeTD();
		body.closeTR();

		body.closeTABLE();
	}
	
	/*
	 * Private Methods
	 */
	
	private String getSchemaNodeLink(SchemaNode schema, RequestParameterVersion versionParameter) {
		String target = _databaseIdentifier.linkPrefix() + schema.identifier().toURLString();
		if (!versionParameter.versionCurrent()) {
			target = target + "?" + versionParameter.toURLString();
		}
		return target;
	}
	
	private boolean printAttributeSchemaNode(AttributeSchemaNode attribute, String linkTarget, RequestParameterVersion versionParameter,
					SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		int lineCount = 0;
		String label = attribute.label();

		if (versionParameter.matches(attribute)) {
			lineCount = 1;
			String target = linkTarget;
			//if (target == null)  {
				target = getSchemaNodeLink(attribute, versionParameter);
			//}
			if (attribute.getTimestamp().isCurrent()) {
				content.linkWithTitle(target, attribute.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueActive));
			} else {
				content.linkWithTitle(target, attribute.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueInactive));
			}
		}

		return (lineCount > 0);
	}
	
	private void printEntitiesInGroupStyle(SchemaNodeList list, String linkTarget, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content)
		throws org.dbwiki.exception.WikiException {
			
		content.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
	
		content.openTR();
		content.openTD(layout.getCSS(CSS.CSSContentValue));
		content.openTABLE(layout.getCSS(CSS.CSSContentValueListing));
		
		for (int i = 0; i < list.size(); i++) {
			SchemaNode schema = list.get(i);
			if (versionParameter.matches(schema)) {
				content.openTR();
				String target = linkTarget;
				target = getSchemaNodeLink(schema, versionParameter);
				if (schema.isAttribute()) {
					AttributeSchemaNode attribute = (AttributeSchemaNode)schema;
					if (attribute.getTimestamp().isCurrent()) {
						content.openTD(layout.getCSS(CSS.CSSContentValueActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentValueInactive));
					}
					printAttributeSchemaNode(attribute, target, versionParameter, layout, content);

					content.closeTD();
				} else {
					printGroupSchemaNode((GroupSchemaNode)schema, target, versionParameter, layout, content);
				}
				content.closeTR();
			}
		}

		content.closeTABLE();
		content.closeTD();
			
		content.closeTR();
		content.closeTABLE();
	}
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		String target = getSchemaNodeLink(_schemaNode, _versionParameter);
		
		if (_versionParameter.matches(_schemaNode)) {
			if (_schemaNode.isAttribute()) {
				printAttributeSchemaNode((AttributeSchemaNode)_schemaNode, target, _versionParameter, _layouter.get(_schemaNode), body);
			} else {
				printGroupSchemaNode((GroupSchemaNode)_schemaNode, target, _versionParameter, _layouter.get(_schemaNode), body);
			}
		}
	}
}
