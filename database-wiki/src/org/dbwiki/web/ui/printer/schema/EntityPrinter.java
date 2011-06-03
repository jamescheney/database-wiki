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
import org.dbwiki.data.resource.EntityIdentifier;

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.EntityList;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.EntityLayout;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints schema types as HTML 
 * 
 * @author jcheney
 *
 */
public class EntityPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseIdentifier _databaseIdentifier;
	private DatabaseLayouter _layouter;
	
	private RequestParameterVersion _versionParameter;
	
	private Entity _entity;
	
	/*
	 * Constructors
	 */
		
	public EntityPrinter(WikiSchemaRequest request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		_databaseIdentifier = request.wri().databaseIdentifier();
		_layouter = layouter;
		_entity = request.entity();
		if(_entity == null) {
			_entity = request.wiki().database().getEntity(new EntityIdentifier(0));
		}
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	public void printGroupEntity(GroupEntity entity, String target, RequestParameterVersion versionParameter, EntityLayout layout, HtmlLinePrinter body)
	    throws org.dbwiki.exception.WikiException {
		
		body.openTABLE(layout.getCSS(CSS.CSSObjectFrame));
		
		body.openTR();
		body.openTD(layout.getCSS(CSS.CSSObjectListing));
		if (entity.getTimestamp().isCurrent()) {
			body.linkWithTitle(target, entity.getTimestamp().toPrintString(), entity.label(), layout.getCSS(CSS.CSSContentValueActive));
		} else {
			body.linkWithTitle(target, entity.getTimestamp().toPrintString(), entity.label(), layout.getCSS(CSS.CSSContentValueInactive));
		}
		body.closeTD();
		body.closeTR();

		EntityList children = entity.children();
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
	
	private String getEntityLink(Entity entity, RequestParameterVersion versionParameter) {
		String target = _databaseIdentifier.linkPrefix() + entity.identifier().toURLString();
		if (!versionParameter.versionCurrent()) {
			target = target + "?" + versionParameter.toURLString();
		}
		return target;
	}
	
	private boolean printAttributeEntity(AttributeEntity attribute, String linkTarget, RequestParameterVersion versionParameter,
					EntityLayout layout, HtmlLinePrinter content) throws org.dbwiki.exception.WikiException {
		int lineCount = 0;
		String label = attribute.label();

		if (versionParameter.matches(attribute)) {
			lineCount = 1;
			String target = linkTarget;
			//if (target == null)  {
				target = getEntityLink(attribute, versionParameter);
			//}
			if (attribute.getTimestamp().isCurrent()) {
				content.linkWithTitle(target, attribute.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueActive));
			} else {
				content.linkWithTitle(target, attribute.getTimestamp().toPrintString(), label, layout.getCSS(CSS.CSSContentValueInactive));
			}
		}

		return (lineCount > 0);
	}
	
	private void printEntitiesInGroupStyle(EntityList list, String linkTarget, RequestParameterVersion versionParameter, EntityLayout layout, HtmlLinePrinter content)
		throws org.dbwiki.exception.WikiException {
			
		content.openTABLE(layout.getCSS(CSS.CSSContentFrameActive));
	
		content.openTR();
		content.openTD(layout.getCSS(CSS.CSSContentValue));
		content.openTABLE(layout.getCSS(CSS.CSSContentValueListing));
		
		for (int i = 0; i < list.size(); i++) {
			Entity entity = list.get(i);
			if (versionParameter.matches(entity)) {
				content.openTR();
				String target = linkTarget;
				target = getEntityLink(entity, versionParameter);
				if (entity.isAttribute()) {
					AttributeEntity attribute = (AttributeEntity)entity;
					if (attribute.getTimestamp().isCurrent()) {
						content.openTD(layout.getCSS(CSS.CSSContentValueActive));
					} else {
						content.openTD(layout.getCSS(CSS.CSSContentValueInactive));
					}
					printAttributeEntity(attribute, target, versionParameter, layout, content);

					content.closeTD();
				} else {
					printGroupEntity((GroupEntity)entity, target, versionParameter, layout, content);
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
		String target = getEntityLink(_entity, _versionParameter);
		
		if (_versionParameter.matches(_entity)) {
			if (_entity.isAttribute()) {
				printAttributeEntity((AttributeEntity)_entity, target, _versionParameter, _layouter.get(_entity), body);
			} else {
				printGroupEntity((GroupEntity)_entity, target, _versionParameter, _layouter.get(_entity), body);
			}
		}
	}
}
