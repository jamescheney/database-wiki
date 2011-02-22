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

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

import org.dbwiki.web.ui.layout.DatabaseLayouter;


public class DataValuePrinter extends DataNodePrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseNode _node;
	private RequestParameterVersion _versionParameter;
	
	/*
	 * Constructors
	 */
	
	public DataValuePrinter(WikiDataRequest request, DatabaseLayouter layouter) throws org.dbwiki.exception.WikiException {
		super(request.wri().databaseIdentifier(), layouter);
		
		_node = request.node();
		_versionParameter = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		if (_versionParameter.matches(_node)) {
			if (_node.isElement()) {
				DatabaseElementNode element = (DatabaseElementNode)_node;
				if (element.isAttribute()) {
					this.printAttributeNode((DatabaseAttributeNode)_node, _versionParameter, body);
				} else {
					this.printGroupNode((DatabaseGroupNode)_node, _versionParameter, body);
				}
			} else {
				this.printTextNode((DatabaseTextNode)_node, body);
			}
		}
	}
}
