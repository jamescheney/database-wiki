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

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionChanges;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;


import org.dbwiki.web.ui.CSS;

public class TimemachinePrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiDataRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public TimemachinePrinter(WikiDataRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		String currentVersion = "<a CLASS=\"" + CSS.CSSTimemachineTab + "\" HREF=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterVersion + "=" + RequestParameterVersion.VersionCurrent + "\">Current version</a>";
		String fullHistory = "<a CLASS=\"" + CSS.CSSTimemachineTab + "\" HREF=\"" + _request.wri().getURL()  + "?" + RequestParameter.ParameterVersion + "=" + RequestParameterVersion.VersionAll + "\">Full history</a>";
		String previousVersion = "<a CLASS=\"" + CSS.CSSTimemachineTab + "\" HREF=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterPreviousVersion + "\">Previous version</a>";
		String changesSince = "<a CLASS=\"" + CSS.CSSTimemachineTab + "\" HREF=\"" + _request.wri().getURL() + "?" + RequestParameter.ParameterChangesSince + "\">Changes since ...</a>";
		
		if (_request.parameters().hasParameter(RequestParameter.ParameterChangesSince)) {
			changesSince = "Changes since ...";
		} else if (_request.parameters().hasParameter(RequestParameter.ParameterPreviousVersion)) {
			previousVersion = "Previous version";
		} else {
			RequestParameterVersion versionParameter = RequestParameter.versionParameter(_request.parameters().get(RequestParameter.ParameterVersion));
			if (versionParameter.versionAll()) {
				fullHistory = "Full history";
			} else if (versionParameter.versionChangesSince()) {
				changesSince = changesSince + "<br/>" + _request.versionIndex().getByNumber(((RequestParameterVersionChanges)versionParameter).versionNumber()).name();
			} else if (versionParameter.versionCurrent()) {
				currentVersion = "Current version";
			} else if (versionParameter.versionSingle()) {
				previousVersion = previousVersion + "<br/>" + _request.versionIndex().getByNumber(((RequestParameterVersionSingle)versionParameter).versionNumber()).name();
			}
		}

		body.openTABLE(CSS.CSSTimemachineTab);
		body.openTR();
		body.openTD(CSS.CSSTimemachineTab);
		body.text(currentVersion);
		body.closeTD();
		body.openTD(CSS.CSSTimemachineTab);
		body.text(fullHistory);
		body.closeTD();
		body.openTD(CSS.CSSTimemachineTab);
		body.text(previousVersion);
		body.closeTD();
		body.openTD(CSS.CSSTimemachineTab);
		body.text(changesSince);
		body.closeTD();
		body.closeTR();
		body.closeTABLE();
	}
}
