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
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.time.Version;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
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
	
	private WikiDataRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public VersionIndexPrinter(WikiDataRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		UserListing user = _request.wiki().users();
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
		
		//start....
		
		body.addBR();
		body.addBR();
		body.add("<div>");
		body.add("<div class=\"DisplayProvenanceGraphs\">");
		body.add("<a class=\"ProvenanceStr\" onClick=\"ProvenanceGraphs()\">Provenance Graphs</a>");
		body.add("</div>");
		body.addBR();
		body.add("<div>");
		body.add("<table class=\"ProvenanceGraphTable\" border=\"1\" >");
		body.openTR();
		body.add("<td id=\"Provenanceusername\">");
		body.add("<select id=\"ProUserOptions\" onChange=\"ProvenanceSelectedUsername()\">");
		body.add("<option>All</option>");
		for (int iUser=1; iUser<=user.size();iUser++)
		{
			body.add("<option>"+ user.get(iUser).fullName() +"</option>");
		}
		body.add("</select>");
		body.closeTD();
		body.add("<td id=\"ProvenanceDates\">");
		body.openTABLE();
		body.openTR();
		body.openTD();
		body.add("Start Date: <input id=\"ProvenanceStartDate\" type=\"text\">");
		body.closeTD();
		body.closeTR();
		body.openTR();
		body.openTD();
		body.add("End Date: <input id=\"ProvenanceEndDate\" type=\"text\">");
		body.openTD();
		body.closeTR();
		body.closeTABLE();
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.add("<td id=\"ProvenancePieChart\">");
		body.closeTD();
		body.add("<td id=\"ProvenanceLineChart\">");
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.add("<td id=\"ProvenanceList\" colspan=\"2\">");
		body.closeTD();
		body.closeTR();
		
		body.closeTABLE();
		body.closeDIV();
		
		body.addBR();
		body.addBR();
		
		body.add("<div>");
		body.add("<div class=\"DisplayAnnotationGraphs\">");
		body.add("<a class=\"AnnotationStr\" onClick=\"AnnotationGraphs()\">Annotation Graphs</a>");
		body.add("</div>");
		body.addBR();
		body.add("<div>");
		body.add("<table class=\"AnnotationGraphTable\" border=\"1\" >");
		body.openTR();
		body.add("<td id=\"Annotationusername\">");
		body.add("<select id=\"AnnUserOptions\" onChange=\"AnnotationSelectedUsername()\">");
		body.add("<option>All</option>");
		for (int iUser=1; iUser<=user.size();iUser++)
		{
			body.add("<option>"+ user.get(iUser).fullName() +"</option>");
		}
		body.add("<option>Unknown</option>");
		body.add("</select>");
		//body.add("Username: <input type=\"text\" name=\"username\" size=\"40\">");
		body.closeTD();
		body.add("<td id=\"AnnotationDates\">");
		body.openTABLE();
		body.openTR();
		body.openTD();
		body.add("Start Date: <input id=\"AnnotationStartDate\" type=\"text\">");
		body.closeTD();
		body.closeTR();
		body.openTR();
		body.openTD();
		body.add("End Date: <input id=\"AnnotationEndDate\" type=\"text\">");
		body.openTD();
		body.closeTR();
		body.closeTABLE();
		body.closeTD();
		body.closeTR();
		
		body.openTR();
		body.add("<td id=\"AnnotationPieChart\">");
		body.closeTD();
		body.add("<td id=\"AnnotationLineChart\">");
		body.closeTD();
		body.closeTR();
	
		body.openTR();
		body.add("<td id=\"AnnotationList\" colspan=\"2\">");
		body.closeTD();
		body.closeTR();
		
		body.add("</table>");
		body.closeDIV();
		
	}
	
	
	/*
	 * Private Methods
	 */
	
	
		
	private void printDatabaseVersionIndex(WikiDataRequest request, String url, HtmlLinePrinter body) {
		System.out.println("in VersionIndexPrinter::printDatabaseVersionIndex"); //delete
		for (int iVersion = 0; iVersion < request.versionIndex().size(); iVersion++) {
			Version version = request.versionIndex().get(iVersion);
			this.printVersionLine(request, version, url, body);
		}
	}
	
	private void printNodeVersionIndex(WikiDataRequest request, String url, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		System.out.println("in VersionIndexPrinter::printNodeVersionIndex"); //delete
		Vector<Version> versions = request.versionIndex().getNodeChanges(request.node());
		for (int iVersion = 0; iVersion < versions.size(); iVersion++) {
			Version version = versions.get(iVersion);
			this.printVersionLine(request, version, url, body);
		}
	}
	
	private void printVersionLine(WikiDataRequest request, Version version, String url, HtmlLinePrinter body) {
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
