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

import java.util.Vector;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseNodeChildFinder;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.provenance.ProvenanceCopy;
import org.dbwiki.data.provenance.ProvenanceImport;

import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;

import org.dbwiki.user.User;
import org.dbwiki.web.html.HtmlLinePrinter;


import org.dbwiki.web.request.WikiDataRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;

public class ObjectProvenancePrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private DatabaseLayouter _layouter;
	private WikiDataRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public ObjectProvenancePrinter(WikiDataRequest request, DatabaseLayouter layouter) {
		_request = request;
		_layouter = layouter;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.openTABLE(CSS.CSSProvenanceListing);								
		body.openTR();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("Version");
		body.closeTH();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("User");
		body.closeTH();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("Action");
		body.closeTH();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("Object");
		body.closeTH();
		body.closeTR();

		Vector<Version> versionVec =  _request.versionIndex().getNodeChanges(_request.node());
		for (int iVersion = 0; iVersion < versionVec.size(); iVersion++) {
			Version version = versionVec.get(iVersion);
			String css = CSS.CSSProvenanceNeutral;
			if(version.provenance().isDelete()) {
				css = CSS.CSSProvenanceMinus;
			} else if (! version.provenance().isUpdate()) {
				css = CSS.CSSProvenancePlus;
			}
			this.printProvenanceLine(version, css, body);
		}
		body.closeTABLE();		
	}
	
	
	private void printProvenanceLine(Version version, String css, HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		String targetURL = null;
		String targetName = null;
		try {
			targetURL = _request.wri().databaseIdentifier().linkPrefix() 
				+ version.provenance().identifier().toURLString() 
				+ "?" +  RequestParameter.ParameterVersion 
				+ "=" + version.number();
			if (_request.node().identifier().equals(version.provenance().identifier())) {
				targetName = "this";
			} else {
				DatabaseNode targetNode = new DatabaseNodeChildFinder().find(_request.node(), version.provenance().identifier());
				if (targetNode != null) {
					RequestParameterVersionSingle versionParameter = new RequestParameterVersionSingle(version.number());
					DatabaseNode node = targetNode;
					while (!node.identifier().equals(_request.node().identifier())) {
						if (node.isElement()) {
							DatabaseElementNode element = (DatabaseElementNode)node;
							String elementName = _layouter.get(element.entity()).getShortLabel(element, versionParameter);
							if (targetName != null) {
								targetName = elementName + "/" + targetName;
							} else {
								targetName = elementName;
							}
						}
						node = node.parent();
					}
					if ((targetName == null) && (targetNode.isText())) {
						targetName = "\'" + ((DatabaseTextNode)targetNode).getValue() + "\'";
						//targetName = "this";
					}
				} else {
					targetName = "Target";
				}
			}
		} catch (NullPointerException e) {
			targetURL = targetName = null;
		}
		
		body.openTR();
		body.openTD(css);
		body.text(version.name());
		body.closeTD();
		body.openTD(css);
		if (version.provenance().user() != null) {
			body.text(version.provenance().user().fullName());
		} else {
			body.text(User.UnknownUserName);
		}
		body.closeTD();
		body.openTD(css);
		if (version.provenance().isCopy()) {
			body.text(version.provenance().name() + " (");
			body.link(((ProvenanceCopy)version.provenance()).sourceURL(), "Source", CSS.CSSLinkActive);
			body.text(")");
		} else if (version.provenance().isImport()) {
			body.text(version.provenance().name() + " (");
			body.link(((ProvenanceImport)version.provenance()).sourceURL(), "Source", CSS.CSSLinkActive);
			body.text(")");
		} else {
			body.text(version.provenance().name());
		}
		body.closeTD();
		body.openTD(css);
		if (targetURL != null) {
			body.link(targetURL, targetName, CSS.CSSLinkActive);
		}
		body.closeTD();
		body.closeTR();
	}

	public void printOld(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		body.openTABLE(CSS.CSSProvenanceListing);								
		body.openTR();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("Version");
		body.closeTH();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("User");
		body.closeTH();
		body.openTH(CSS.CSSProvenanceListing);
		body.text("Action");
		body.closeTH();
		body.closeTR();
		VersionIndex versionIndex = _request.versionIndex();
		TimeInterval[] intervals = _request.node().getTimestamp().intervals();
		for (int iInterval = 0; iInterval < intervals.length; iInterval++) {
			this.printProvenanceLineOld(versionIndex.getByNumber(intervals[iInterval].start()), CSS.CSSProvenancePlus, body);
			if (!intervals[iInterval].isOpen()) {
				this.printProvenanceLineOld(versionIndex.getByNumber(intervals[iInterval].end() + 1), CSS.CSSProvenanceMinus, body);
			}
		}	

		body.closeTABLE();		
	}
	
	/*
	 * Private Methods
	 */
	
	private void printProvenanceLineOld(Version version, String css, HtmlLinePrinter body) {
		body.openTR();
		body.openTD(css);
		body.text(version.name());
		body.closeTD();
		body.openTD(css);
		if (version.provenance().user() != null) {
			body.text(version.provenance().user().fullName());
		} else {
			body.text(User.UnknownUserName);
		}
		body.closeTD();
		body.openTD(css);
		if (version.provenance().isCopy()) {
			body.text(version.provenance().name() + " (");
			body.link(((ProvenanceCopy)version.provenance()).sourceURL(), "Source", CSS.CSSLinkActive);
			body.text(")");
		} else {
			body.text(version.provenance().name());
		}
		body.closeTD();
		body.closeTR();
	}
}
