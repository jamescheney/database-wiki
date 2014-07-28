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
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.time.TimestampPrinter;

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
	private TimestampPrinter _timestampPrinter;
	private RequestParameterVersion _versionParameter;
	
	private SchemaNode _schemaNode;
	
	/*
	 * Constructors
	 */
		
	public SchemaNodePrinter(WikiSchemaRequest<?>  request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		_databaseIdentifier = request.wri().databaseIdentifier();
		_layouter = layouter;
		_schemaNode = request.schema();
		if(_schemaNode == null) {
			_schemaNode = request.wiki().database().getSchemaNode(new SchemaNodeIdentifier(0));
		}
		_timestampPrinter = new TimestampPrinter(request.wiki().database().versionIndex());
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		
		if (_versionParameter.matches(_schemaNode)) {
			if (_schemaNode.isAttribute()) {
				printAttributeSchemaNode((AttributeSchemaNode)_schemaNode, _versionParameter, _layouter.get(_schemaNode), body);
			} else {
				printGroupSchemaNode((GroupSchemaNode)_schemaNode, _versionParameter, _layouter.get(_schemaNode), body);
			}
		}
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
	
	private void printAttributeSchemaNode(AttributeSchemaNode attribute, RequestParameterVersion versionParameter,
					SchemaLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {

		boolean active = attribute.getTimestamp().isCurrent();

		if (active) {
			content.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
		} else {
			content.openTABLE(layout.getCSS(CSS.CSSObjectFrameInactive));
		}
		
		String target = getSchemaNodeLink(attribute, versionParameter);

		content.openTR();
		
		//if (active) {
		//	content.openTD(layout.getCSS(CSS.CSSContentLeftLabelActive));
		//	content.link(target, "ATTRIBUTE", layout.getCSS(CSS.CSSContentLabelActive));
		//} else {
		//	content.openTD(layout.getCSS(CSS.CSSContentLeftLabelInactive));
		//	content.link(target, "ATTRIBUTE", layout.getCSS(CSS.CSSContentLabelInactive));
		//}
		//content.closeTD();
		
		if (active) {
			content.openTD(layout.getCSS(CSS.CSSObjectValueActive));
			content.linkWithTitle(target, _timestampPrinter.toPrintString(attribute.getTimestamp()), attribute.label(), layout.getCSS(CSS.CSSContentValueActive));
		} else {
			content.openTD(layout.getCSS(CSS.CSSObjectValueInactive));
			content.linkWithTitle(target, _timestampPrinter.toPrintString(attribute.getTimestamp()), attribute.label(), layout.getCSS(CSS.CSSContentValueInactive));
		}
		
		content.closeTD();
		
		content.closeTR();
		
		content.closeTABLE();
	}
	
	public void printGroupSchemaNode(GroupSchemaNode group, RequestParameterVersion versionParameter, SchemaLayout layout, HtmlLinePrinter content)
		    throws org.dbwiki.exception.WikiException {

		boolean active = group.getTimestamp().isCurrent();

		if (active) {
			content.openTABLE(layout.getCSS(CSS.CSSObjectFrameActive));
		} else {
			content.openTABLE(layout.getCSS(CSS.CSSObjectFrameInactive));
		}
		
		String target = getSchemaNodeLink(group, versionParameter);

		content.openTR();
		
		if (active) {
			content.openTD(layout.getCSS(CSS.CSSContentLeftLabelActive));
			content.link(target, group.label(), layout.getCSS(CSS.CSSContentLabelActive));
		} else {
			content.openTD(layout.getCSS(CSS.CSSContentLeftLabelInactive));
			content.link(target, group.label(), layout.getCSS(CSS.CSSContentLabelInactive));
		}
		content.closeTD();
		
		content.openTD(layout.getCSS(CSS.CSSObjectValueActive));
		
		for (int iChild = 0; iChild < group.children().size(); iChild++) {
			content.openTABLE(CSS.CSSContentFrameListing);
			content.openTR();
			content.openTD(CSS.CSSContentFrameListing);
			SchemaNode child = group.children().get(iChild);
			if (child.isAttribute()) {
				printAttributeSchemaNode((AttributeSchemaNode)child, _versionParameter, _layouter.get(child), content);
			} else {
				printGroupSchemaNode((GroupSchemaNode)child, _versionParameter, _layouter.get(child), content);
			}
			content.closeTD();
			content.closeTR();
			content.closeTABLE();
		}
		
		content.closeTD();
		
		content.closeTR();
		
		content.closeTABLE();
		
		}
}
