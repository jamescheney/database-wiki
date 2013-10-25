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

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;

import org.dbwiki.web.server.ConfigSetting;
import org.dbwiki.web.server.WikiServerConstants;

import org.dbwiki.web.ui.CSS;

/** Prints out listing of settings that can be used to revert to past versions
 * @author jcheney
 *
 */
public class SettingsListingPrinter implements HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public SettingsListingPrinter(WikiRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter body) throws org.dbwiki.exception.WikiException {
		String baseURL = _request.wri().getURL();

		body.paragraph("Reset display to previous version", CSS.CSSHeadline);
		
		body.openTABLE(CSS.CSSList);
		body.openTR();
		body.openTH(CSS.CSSList);
		body.text("Date");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("Layout");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("Template");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("Style Sheet");
		body.closeTH();
		body.openTH(CSS.CSSList);
		body.text("URL Decoding Rules");
		body.closeTH();
		body.closeTR();
		
		Vector<ConfigSetting> settings = _request.wiki().listSettings();
		ConfigSetting cSet = settings.lastElement();
		for (int iSetting = 0; iSetting < settings.size(); iSetting++) {
			ConfigSetting setting = settings.get(iSetting);
			body.openTR();
			body.openTD(CSS.CSSList);
			body.link(baseURL + "?" + setting.toURLString(), setting.date(), CSS.CSSList);
			body.closeTD();
			body.openTD(CSS.CSSList);
			ConfigSetting s = new ConfigSetting(setting.getLayoutVersion(), cSet.getTemplateVersion(), cSet.getStyleSheetVersion(), cSet.getURLDecodingRulesVersion());
			body.link(baseURL + "?" + s.toURLString(), this.valueOf(setting.getLayoutVersion()), CSS.CSSList);
			body.closeTD();
			body.openTD(CSS.CSSList);
			s = new ConfigSetting(cSet.getLayoutVersion(), setting.getTemplateVersion(), cSet.getStyleSheetVersion(), cSet.getURLDecodingRulesVersion());
			body.link(baseURL + "?" + s.toURLString(), this.valueOf(setting.getTemplateVersion()), CSS.CSSList);
			body.closeTD();
			body.openTD(CSS.CSSList);
			s = new ConfigSetting(cSet.getLayoutVersion(), cSet.getTemplateVersion(), setting.getStyleSheetVersion(), cSet.getURLDecodingRulesVersion());
			body.link(baseURL + "?" + s.toURLString(), this.valueOf(setting.getStyleSheetVersion()), CSS.CSSList);
			body.closeTD();
			body.openTD(CSS.CSSList);
			s = new ConfigSetting(cSet.getLayoutVersion(), cSet.getTemplateVersion(), cSet.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
			body.link(baseURL + "?" + s.toURLString(), this.valueOf(setting.getStyleSheetVersion()), CSS.CSSList);
			body.closeTD();
			body.closeTR();
		}
		
		body.closeTABLE();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private String valueOf(int fileVersion) {
		if (fileVersion == WikiServerConstants.RelConfigFileColFileVersionValUnknown) {
			return "DEFAULT";
		} else {
			return "Ver. " + fileVersion;
		}
	}
}
