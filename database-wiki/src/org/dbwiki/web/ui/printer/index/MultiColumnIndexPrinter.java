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

import java.util.Vector;

import org.dbwiki.data.index.ContentIterator;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.DatabaseEntry;

import org.dbwiki.data.resource.DatabaseIdentifier;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.ui.CSS;
/** 
 * Print entries in multiple columns, by default 2
 * @author jcheney
 *
 */
public class MultiColumnIndexPrinter extends IndexContentPrinter {
	/*
	 * Private Constants
	 */
	
	private int _columns = 2;
	
	
	/*
	 * Constructors
	 */
	
	public MultiColumnIndexPrinter(WikiRequest<?> request, DatabaseContent content) {
		super(request, content);
	}

	public MultiColumnIndexPrinter(WikiRequest<?> request, DatabaseContent content, int columns) {
		super(request, content);
		_columns=columns;
	}

	
	/*
	 * Public Methods
	 */

	public void listContent(DatabaseIdentifier databaseIdentifier, ContentIterator iterator, WikiRequest<?> request, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		
		DatabaseEntry entry = null;
		
		Vector<DatabaseEntry> entries = new Vector<DatabaseEntry>();
		while ((entry = iterator.next()) != null) {
			entries.add(entry);
		}
		
		int entriesPerColumn = entries.size() / _columns;
		if ((entries.size() % _columns) != 0) {
			entriesPerColumn++;
		}
		
		RequestParameterVersion versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));

		body.openTABLE(CSS.CSSIndexContent);
		body.openTR();
		
		int index = 0;
		for (int iColumn = 0; iColumn < _columns; iColumn++) {
			body.openTD(CSS.CSSIndexContent);
			for (int iRow = 0; iRow < entriesPerColumn; iRow++) {
				if (index < entries.size()) {
					entry = entries.get(index);
					body.text(databaseIdentifier.getLink(entry, versionParameter) + "<br/>");
				}
				index++;
			}
			body.closeTD();
		}
		
		body.closeTR();
		body.closeTABLE();
	}
}
