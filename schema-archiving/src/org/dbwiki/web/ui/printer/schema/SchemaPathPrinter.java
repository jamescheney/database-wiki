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

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;


import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Prints the path of entity labels associated with a schema entity
 *  
 * @author jcheney
 *
 */
public class SchemaPathPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private WikiSchemaRequest _request;
	
	/*
	 * Constructors
	 */
	
	public SchemaPathPrinter(WikiSchemaRequest request, DatabaseLayouter layouter) {
		_request = request;
		_layouter = layouter;
	}
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));

		String line = null;
		
		Entity entity = _request.entity();
		while (entity != null) {
			if (entity.isGroup()) {
				GroupEntity group = (GroupEntity)entity;
				String target = _request.wri().databaseIdentifier().linkPrefix() + entity.identifier().toURLString();
				if (!versionParameter.versionCurrent()) {
					target = target + "?" + versionParameter.toURLString();
				}
				String link = entity.label();
				// FIXME:
				// perhaps this should use the layouter, but currently
				// the treatment of data nodes is hard-coded.
				// It isn't obvious what the code does or how to adapt it
				// to entities.
				//
				//_layouter.get(entity).getShortLabel(entity, versionParameter);
				link = "<a CLASS=\"" + CSS.CSSObjectPath + "\" HREF=\"" + target + "\">" + link + "</a>";
				if (line != null) {
					line = link + " &gt; " + line;
				} else {
					line = link;
				}
			}
			entity = entity.parent();
		}
		if (line != null) {
			body.paragraph(line, CSS.CSSObjectPath);
		}
	}
}
