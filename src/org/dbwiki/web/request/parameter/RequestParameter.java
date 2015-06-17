/*
    BEGIN LICENSE BLOCK
    Copyright 2010-2014, Heiko Mueller, Sam Lindley, James Cheney, 
    Ondrej Cierny, Mingjun Han, and
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
package org.dbwiki.web.request.parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.dbwiki.exception.web.WikiRequestException;

public class RequestParameter {
	/*
	 * Public Constants
	 */
	
	public static final String ParameterAction           = "action";
	public static final String ParameterActivate         = "activate";
	public static final String ParameterChangesSince     = "changes";
	public static final String ParameterCopy             = "copy";
	public static final String ParameterCopyPasteExport  = "cpxml";
	public static final String ParameterCreate           = "new";
	public static final String ParameterCreateSchemaNode = "new_schema_node";
	public static final String ParameterDelete           = "delete";
	public static final String ParameterEdit             = "edit";
	public static final String ParameterExportXML        = "xml";
	public static final String ParameterExportJSON       = "json";
	public static final String ParameterHistory          = "history";
	public static final String ParameterIndexPosition    = "idxpos";
	public static final String ParameterLayout           = "layout";
	public static final String ParameterPaste            = "paste";
	public static final String ParameterPasteForm        = "paste_form";
	public static final String ParameterPreviousVersion  = "previous";
	public static final String ParameterSearch           = "search";
	public static final String ParameterReset            = "reset";
	public static final String ParameterResource         = "resource";
	public static final String ParameterSettings         = "settings";
	public static final String ParameterStyleSheet       = "style_sheet";
	public static final String ParameterTemplate         = "html_template";
	public static final String ParameterURL              = "url";
	public static final String ParameterURLDecoding      = "url_decoding";
	public static final String ParameterVersion          = "version";

	public static final String ParameterAllUsers         = "all_users";
	public static final String ParameterAuthorization	 = "authorization";
	public static final String ParameterEntryAuthorization	 = "entry_authorization";

	
	public static final String ActionValueAnnotation      = "annotation";
	public static final String ActionValueSchemaNode      = "schema_node";
	public static final String ActionValuePageID          = "page_id";
	public static final String ActionValuePageTitle       = "page_title";
	public static final String ActionValuePageValue       = "page_value";

	public static final String TextFieldIndicator       = "txt_";
	
	
	/*
	 * Static Methods
	 */
	
	public static RequestParameterAction actionParameter(RequestParameter parameter) throws org.dbwiki.exception.WikiException {
		String value = parameter.value();
		if (value != null) {
			if (value.equals(RequestParameterAction.ActionAnnotate)) {
				return new RequestParameterActionAnnotate();
			} else if (value.equals(RequestParameterAction.ActionCancel)) {
				return new RequestParameterActionCancel();
			} else if (value.equals(RequestParameterAction.ActionSchemaNode)) {
				return new RequestParameterActionSchemaNode();
			} else if (value.equals(RequestParameterAction.ActionInsert)) {
				return new RequestParameterActionInsert();
			} else if (value.equals(RequestParameterAction.ActionUpdate)) {
				return new RequestParameterActionUpdate();
			} else if (value.equals(RequestParameterAction.ActionUpdateUsers)) {
				return new RequestParameterActionUpdateUsers();
			} else if (value.equals(RequestParameterAction.ActionUpdateAuthorization)) {
				return new RequestParameterActionUpdateAuthorization();
			} else if (value.equals(RequestParameterAction.ActionCancelAuthorizationUpdate)) {
				return new RequestParameterActionCancelAuthorizationUpdate();
			} else if (value.equals(RequestParameterAction.ActionUpdateEntryAuthorization)) {
				return new RequestParameterActionUpdateEntryAuthorization();
			} else if (value.equals(RequestParameterAction.ActionCancelEntryAuthorizationUpdate)) {
				return new RequestParameterActionCancelEntryAuthorizationUpdate();
			} else {
				throw new WikiRequestException(WikiRequestException.InvalidParameterValue, parameter.toString());
			}
		} else {
			throw new WikiRequestException(WikiRequestException.MissingParameterValue, parameter.toString());
		}
	}
	
	public static RequestParameterVersion versionParameter(RequestParameter parameter) throws org.dbwiki.exception.WikiException {
		if (parameter != null) {
			String value = parameter.value();
			if (value != null) {
				if (value.equals(RequestParameterVersion.VersionCurrent)) {
					return new RequestParameterVersionCurrent();
				} else if (value.equals(RequestParameterVersion.VersionAll)) {
					return new RequestParameterVersionAll();
				} else if (value.startsWith(RequestParameterVersion.VersionChanges)) {
					try {
						return new RequestParameterVersionChanges(Integer.parseInt(value.substring(RequestParameterVersion.VersionChanges.length())));
					} catch (NumberFormatException e) {
						throw new WikiRequestException(WikiRequestException.InvalidParameterValue, parameter.toString());
					}
				} else {
					try {
						return new RequestParameterVersionSingle(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						throw new WikiRequestException(WikiRequestException.InvalidParameterValue, parameter.toString());
					}
				}
			} else {
				throw new WikiRequestException(WikiRequestException.MissingParameterValue, parameter.toString());
			}
		} else {
			return new RequestParameterVersionCurrent();
		}
	}

	
	/*
	 * Private Variables
	 */
	
	private String _name;
	private String _value;
	
	
	/*
	 * Constructors
	 */
	
	public RequestParameter(String text) {
		int pos = text.indexOf("=");
		if (pos != -1) {
			_name = text.substring(0, pos);
			_value = text.substring(pos + 1);
		} else {
			_name = text;
			_value = null;
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean hasValue() {
		return (_value != null);
	}
	
	public String name() {
		return _name;
	}
	
	public String toString() {
		if (_value != null) {
			return _name + "=" + _value;
		} else {
			return _name;
		}
	}
	
	public String toURLString() {
		if (_value != null) {
			try {
				return _name + "=" + URLEncoder.encode(_value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return _name + "=" + _value;
			}
		} else {
			return _name;
		}
	}
	
	public String value() {
		return _value;
	}
}
