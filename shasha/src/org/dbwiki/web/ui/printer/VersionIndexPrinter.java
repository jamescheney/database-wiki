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

/*
import java.util.Hashtable;
import java.util.Iterator;
*/

import java.util.Vector;

/*
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.Timestamp;
import org.dbwiki.data.time.VersionIndex;
*/
import org.dbwiki.data.time.Version;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;


import org.dbwiki.web.ui.CSS;

/** Prints out a version index as a form that can be used to visit past versions
 * 
 * @author jcheney
 *
 */
public class VersionIndexPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiDataRequest<?>  _request;
	
	
	/*
	 * Constructors
	 */
	
	public VersionIndexPrinter(WikiDataRequest<?>  request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		String url = _request.wri().getURL() + "?" +  RequestParameter.ParameterVersion + "=";
		if (_request.parameters().hasParameter(RequestParameter.ParameterChangesSince)) {
			url = url + RequestParameterVersion.VersionChanges;
			body.paragraph("Show changes since ...", CSS.CSSHeadline);
		} else {
			body.paragraph("Show previous version", CSS.CSSHeadline);
		}
		
		body.openTABLE(CSS.CSSList);
		body.openTR();
		body.openTH(CSS.CSSList);
		body.text("Version");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("User");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("Action");
		body.closeTH();
		body.closeTR();
		
		if (_request.isRootRequest()) {
			this.printDatabaseVersionIndex(_request, url, body);
		} else {
			this.printNodeVersionIndex(_request, url, body);
		}
		
		body.closeTABLE();
	}
	
	
	/*
	 * Private Methods
	 */
	
	
		
	private void printDatabaseVersionIndex(WikiDataRequest<?>  request, String url, HtmlLinePrinter body) {
		for (int iVersion = 0; iVersion < request.versionIndex().size(); iVersion++) {
			Version version = request.versionIndex().get(iVersion);
			this.printVersionLine(request, version, url, body);
		}
	}
	
	private void printNodeVersionIndex(WikiDataRequest<?>  request, String url, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		Vector<Version> versions = request.versionIndex().getNodeChanges(request.node());
		for (int iVersion = 0; iVersion < versions.size(); iVersion++) {
			Version version = versions.get(iVersion);
			this.printVersionLine(request, version, url, body);
		}
	}
	
	private void printVersionLine(WikiDataRequest<?>  request, Version version, String url, HtmlLinePrinter body) {
		String target = url + version.number();

		body.openTR();
		
		body.openTD(CSS.CSSList);
		body.link(target, version.name(), CSS.CSSList);
		body.closeTD();
		body.openTD(CSS.CSSList);
		if (version.provenance().user() != null) {
			body.text(version.provenance().user().fullName());
		} else {
			body.text(User.UnknownUserName);
		}
		body.closeTD();
		body.openTD(CSS.CSSList);
		if (version.provenance().identifier() != null) {
			body.link(request.wri().databaseIdentifier().linkPrefix() + version.provenance().identifier().toURLString(), version.provenance().name(), CSS.CSSList);
		} else {
			body.text(version.provenance().name());
		}
		body.closeTD();
		
		body.closeTR();
	}
}
