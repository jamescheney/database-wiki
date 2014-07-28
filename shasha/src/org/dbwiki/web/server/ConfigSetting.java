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
package org.dbwiki.web.server;

import java.util.StringTokenizer;

import org.dbwiki.web.request.parameter.RequestParameter;

/** 
 * A quadruple containing the date, layout, template and stylesheet version numbers of 
 * a single "version" of a DatabaseWiki.
 * @author jcheney
 *
 */
public class ConfigSetting {
	/*
	 * Private Constants
	 */
	
	private String _date;
	private int _layoutVersion;
	private int _templateVersion;
	private int _styleSheetVersion;
	private int _urlDecodingRulesVersion;
	
	
	/*
	 * Constructors
	 */
	
	public ConfigSetting(String date, int layoutVersion, int templateVersion, int styleSheetVersion, int urlDecodingRulesVersion) {
		_date = date;
		_layoutVersion = layoutVersion;
		_templateVersion = templateVersion;
		_styleSheetVersion = styleSheetVersion;
		_urlDecodingRulesVersion = urlDecodingRulesVersion;
	}
	
	public ConfigSetting(int layoutVersion, int templateVersion, int styleSheetVersion, int urlDecodingRulesVersion) {
		this("", layoutVersion, templateVersion, styleSheetVersion, urlDecodingRulesVersion);
	}

	public ConfigSetting(String date, ConfigSetting setting) {
		this(date, setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
	}

	public ConfigSetting(String parameterValue) {
		_date = null;
		
		StringTokenizer tokens = new StringTokenizer(parameterValue, "_");
		_layoutVersion = Integer.parseInt(tokens.nextToken());
		_templateVersion = Integer.parseInt(tokens.nextToken());
		_styleSheetVersion = Integer.parseInt(tokens.nextToken());
	}

	public ConfigSetting() {
		this("DEFAULT", WikiServerConstants.RelConfigFileColFileVersionValUnknown, WikiServerConstants.RelConfigFileColFileVersionValUnknown, WikiServerConstants.RelConfigFileColFileVersionValUnknown, WikiServerConstants.RelConfigFileColFileVersionValUnknown);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String date() {
		return _date;
	}
	
	public int getLayoutVersion() {
		return _layoutVersion;
	}
	
	public int getTemplateVersion() {
		return _templateVersion;
	}
	
	public int getStyleSheetVersion() {
		return _styleSheetVersion;
	}
	
	public int getURLDecodingRulesVersion() {
		return _urlDecodingRulesVersion;
	}
	
	public void setLayoutVersion(int value) {
		_layoutVersion = value;
	}
	
	public void setTemplateVersion(int value) {
		_templateVersion = value;
	}
	
	public void setStyleSheetVersion(int value) {
		_styleSheetVersion = value;
	}
	
	public void setURLDecodingRulesVersion(int value) {
		_urlDecodingRulesVersion = value;
	}

	public String toURLString() {
		return  RequestParameter.ParameterReset + "=" + _layoutVersion + "_" + _templateVersion + "_" + _styleSheetVersion;
	}
}
