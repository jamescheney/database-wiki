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
package org.dbwiki.web.ui.printer.index;

import org.dbwiki.data.index.ChangesSinceContentIterator;
import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.CurrentContentIterator;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.FullContentIterator;
import org.dbwiki.data.index.VersionContentIterator;

import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionChanges;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Abstract base class for printers that print indexes of entries
 * Printing takes versioning into account.
 * @author jcheney
 *
 */
public abstract class IndexContentPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseContent _content;
	private WikiRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public IndexContentPrinter(WikiRequest request, DatabaseContent content) {
		_request = request;
		_content = content;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract void listContent(DatabaseIdentifier databaseIdentifier, ContentIterator iterator, WikiRequest request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException;
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		ContentIterator iterator = null;
		
		
		RequestParameterVersion version = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));
		if (version.versionAll()) {
			iterator = new FullContentIterator(_content);
		} else if (version.versionChangesSince()) {
			iterator = new ChangesSinceContentIterator(_content, ((RequestParameterVersionChanges)version).versionNumber());
		} else if (version.versionCurrent()) {
			iterator = new CurrentContentIterator(_content);
		} else if (version.versionSingle()) {
			iterator = new VersionContentIterator(_content, ((RequestParameterVersionSingle)version).versionNumber());
		}
		
		if (iterator != null) {
			this.listContent(_request.wri().databaseIdentifier(), iterator, _request, body);
		}
	}
}
