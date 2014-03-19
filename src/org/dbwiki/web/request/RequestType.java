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
package org.dbwiki.web.request;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.request.parameter.RequestParameterVersion;

public class RequestType {
	/*
	 * Private Constants
	 */
	
	private static final byte requestTypeUnknown = -1;

	//?action=insert, annotate, update ...
	private static final byte requestTypeAction = 0;
	//?activate
	private static final byte requestTypeActivate = 1;
	//?copy{&version=...]
	private static final byte requestTypeCopy = 2;
	//?cpxml{&version=...]
	private static final byte requestTypeCopyPasteExport = 3;
	//?new[=...]
	private static final byte requestTypeCreate = 4;
	//?new_schema_node
	private static final byte requestTypeCreateSchemaNode = 5;
	//?delete
	private static final byte requestTypeDelete = 6;
	//?edit[=...]
	private static final byte requestTypeEdit = 7;
	// path != / && (?action=cancel ... || <null> || ?version=...) 
	private static final byte requestTypeGet = 8;
	// / [index_pos=...] [version=...]
	private static final byte requestTypeIndex = 9;
	//?layout resource=...
	private static final byte requestTypeLayout = 10;
	//?paste
	private static final byte requestTypePaste = 11;
	//?paste_form
	private static final byte requestTypePasteForm = 12;
	//?reset=...
	private static final byte requestTypeReset = 13;
	//?search=...
	private static final byte requestTypeSearch = 14;
	//?synchronize_form
	private static final byte requestTypeSynchronizeForm = 24;
	//?synchronize_form1
	private static final byte requestTypeSynchronizeFormApproach1 = 29;
	//?synchronize_form1
	private static final byte requestTypeSynchronizeFormApproach2 = 30;
	//?synchronize
	private static final byte requestTypeSynchronize = 25;
	//?synchronize1
	private static final byte requestTypeSynchronize1 = 31;
	//?synchronize2
	private static final byte requestTypeSynchronize2 = 32;
	//?synxml
	private static final byte requestTypeSynchronizeExport = 26;
	//?pushtoremote
	private static final byte requestTypePushToRemote = 27;
	//?synthenxml
	private static final byte requestTypeSynchronizeThenExport = 28;
	//?synthenxml
	private static final byte requestTypeSynchronizeThenExport1 = 33;
	//?synthenxml
	private static final byte requestTypeSynchronizeThenExport2 = 34;
	//?settings
	private static final byte requestTypeSettings = 15;
	//?style_sheet resource=...
	private static final byte requestTypeStyleSheet = 16;
	//?template resource=...
	private static final byte requestTypeTemplate = 17;
	//?previous
	private static final byte requestTypeTimemachinePrevious = 18;
	//?changes
	private static final byte requestTypeTimemachineChanges = 19;
	//?pagehistory=...
	private static final byte requestTypePageHistory = 20;
	//?xml [?version]
	private static final byte requestTypeExportXML = 21;
	//?url_decoding resource=...
	private static final byte requestTypeURLDecoding = 22;
	// ?json[?version]
	private static final byte requestTypeExportJSON = 23;
	
	
	
	/*
	 * Private Variables
	 */
	
	private byte _type;

	
	/*
	 * Constructors
	 */
	
	public RequestType(RequestURL<?> url) {
		RequestParameterList parameters = url.parameters();
		
		_type = requestTypeUnknown;
		
		if (parameters.hasParameter(RequestParameter.ParameterAction)) {
			// Note that the number of parameters for action may differ.
			if (parameters.get(RequestParameter.ParameterAction).value().equals(RequestParameterAction.ActionCancel)) {
				if (url.isRoot()) {
					// path = / && (?action=cancel ... || <null> || ?version=...) 
					_type = requestTypeIndex;
				} else {
					// path != / && (?action=cancel ... || <null> || ?version=...) 
					_type = requestTypeGet;
				}
			} else {
				//?action=insert, annotate, update ...
				_type =  requestTypeAction;
			}
		} else if (parameters.size() == 0) {
			if (url.isRoot()) {
				// / [index_pos=...] [version=...]
				_type = requestTypeIndex;
			} else {
				// path != / && (?action=cancel ... || <null> || ?version=...) 
				_type = requestTypeGet;
			}
		} else if (parameters.size() == 1) {
			if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
				//?activate
				_type = requestTypeActivate;
			} else if (parameters.hasParameter(RequestParameter.ParameterCopy)) {
				//?copy{&version=...]
				_type = requestTypeCopy;
			} else if (parameters.hasParameter(RequestParameter.ParameterCopyPasteExport)) {
				//?cpxml{&version=...]
				_type = requestTypeCopyPasteExport;
			} else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
				//?new[=...]
				_type = requestTypeCreate;
			} else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
				//?new_schema_node
				_type = requestTypeCreateSchemaNode;
			} else if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
				//?delete
				_type = requestTypeDelete;
			} else if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
				//?edit[=...]
				_type = requestTypeEdit;
			} else if (parameters.hasParameter(RequestParameter.ParameterPaste)) {
				//?paste
				_type = requestTypePaste;
			} else if (parameters.hasParameter(RequestParameter.ParameterPasteForm)) {
				//?paste
				_type = requestTypePasteForm;
			} else if (parameters.hasParameter(RequestParameter.ParameterReset)) {
				//?reset=...
				_type = requestTypeReset;
			} else if (parameters.hasParameter(RequestParameter.ParameterSearch)) {
				//?search=...
				_type = requestTypeSearch;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeExport)) {
				//?synxml
				_type = requestTypeSynchronizeExport;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport)) {
				//?synthenxml
				_type = requestTypeSynchronizeThenExport;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport1)) {
				//?synthenxml
				_type = requestTypeSynchronizeThenExport1;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport2)) {
				//?synthenxml
				_type = requestTypeSynchronizeThenExport2;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeForm)) {
				//?synchronize_form
				_type = requestTypeSynchronizeForm;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeFormApproach1)) {
				//?synchronize_form1
				_type = requestTypeSynchronizeFormApproach1;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronizeFormApproach2)) {
				//?synchronize_form1
				_type = requestTypeSynchronizeFormApproach2;
			} else if (parameters.hasParameter(RequestParameter.ParameterSynchronize)) {
				//?synchronize
				_type = requestTypeSynchronize;
			} else if (parameters.hasParameter(RequestParameter.ParameterPushToRemote)) {
				//?pushtoremote
				_type = requestTypePushToRemote;
			} else if (parameters.hasParameter(RequestParameter.ParameterSettings)) {
				//?settings
				_type = requestTypeSettings;
			} else if (parameters.hasParameter(RequestParameter.ParameterPreviousVersion)) {
				//?previous
				_type = requestTypeTimemachinePrevious;
			} else if (parameters.hasParameter(RequestParameter.ParameterChangesSince)) {
				//?changes
				_type = requestTypeTimemachineChanges;
			} else if (parameters.hasParameter(RequestParameter.ParameterExportXML)) {
				//?xml
				_type = requestTypeExportXML;
			} else if (parameters.hasParameter(RequestParameter.ParameterExportJSON)) {
				//?xml
				_type = requestTypeExportJSON;
			} else if (parameters.hasParameter(RequestParameter.ParameterHistory)) {
				_type = requestTypePageHistory;
			} else if (url.isRoot()) {
				// / [index_pos=...] [version=...]
				if (parameters.hasParameter(RequestParameter.ParameterVersion)) {
					_type = requestTypeIndex;
				} else if (parameters.hasParameter(RequestParameter.ParameterIndexPosition)) {
					_type = requestTypeIndex;
				}
			} else {
				if (parameters.hasParameter(RequestParameter.ParameterVersion)) {
					// path != / && (?action=cancel ... || <null> || ?version=...) 
					_type = requestTypeGet;
				}	
			}
		} else if (parameters.size() == 2) {
			if ((parameters.hasParameter(RequestParameter.ParameterCopy))  && (parameters.hasParameter(RequestParameter.ParameterVersion))) {
				//?copy{&version=...]
				_type = requestTypeCopy;
			} else if ((parameters.hasParameter(RequestParameter.ParameterCopyPasteExport))  && (parameters.hasParameter(RequestParameter.ParameterVersion))) {
				//?cpxml{&version=...]
				_type = requestTypeCopyPasteExport;
			} else if ((parameters.hasParameter(RequestParameter.ParameterLayout)) && (parameters.hasParameter(RequestParameter.ParameterResource))) {
				//?layout resource=...
				_type = requestTypeLayout;
			} else if ((parameters.hasParameter(RequestParameter.ParameterTemplate)) && (parameters.hasParameter(RequestParameter.ParameterResource))) {
				//?template resource=...
				_type = requestTypeTemplate;
			} else if ((parameters.hasParameter(RequestParameter.ParameterPaste)) && (parameters.hasParameter(RequestParameter.ParameterURL))) {
				//?paste
				_type = requestTypePaste;
			}else if ((parameters.hasParameter(RequestParameter.ParameterStyleSheet)) && (parameters.hasParameter(RequestParameter.ParameterResource))) {
				//?style_sheet resource=...
				_type = requestTypeStyleSheet;
			} else if ((parameters.hasParameter(RequestParameter.ParameterURLDecoding)) && (parameters.hasParameter(RequestParameter.ParameterResource))) {
				//?url_decoding resource=...
				_type = requestTypeURLDecoding;
				
			} else if ((parameters.hasParameter(RequestParameter.ParameterReset)) && (parameters.hasParameter(RequestParameter.ParameterResource))) {
				// / [index_pos=...] [version=...]
				if ((parameters.hasParameter(RequestParameter.ParameterVersion)) && (parameters.hasParameter(RequestParameter.ParameterIndexPosition))) {
					_type = requestTypeIndex;
				}
			} else if ((parameters.hasParameter(RequestParameter.ParameterVersion)) && (parameters.hasParameter(RequestParameter.ParameterExportXML))) {
				//?version=x && ?xml
				try {
					RequestParameterVersion version = RequestParameter.versionParameter(parameters.get(RequestParameter.ParameterVersion));
					if (version.versionSingle()) {
						_type = requestTypeExportXML;
					}
				} catch (org.dbwiki.exception.WikiException wikiException) {
					// silently ignore wiki exceptions
					// FIXME #requestparsing: is this really the behaviour we want?
				}
			} else if ((parameters.hasParameter(RequestParameter.ParameterVersion)) && (parameters.hasParameter(RequestParameter.ParameterExportJSON))) {
				//?version=x && ?json
				try {
					RequestParameterVersion version = RequestParameter.versionParameter(parameters.get(RequestParameter.ParameterVersion));
					if (version.versionSingle()) {
						_type = requestTypeExportJSON;
					}
				} catch (org.dbwiki.exception.WikiException wikiException) {
					// silently ignore wiki exceptions
					// FIXME #requestparsing: is this really the behaviour we want?
				}
			}
		}
		else if(parameters.size() == 7){
			if ((parameters.hasParameter(RequestParameter.ParameterSynchronize1)) && (parameters.hasParameter(RequestParameter.ParameterURL))
					&&  (parameters.hasParameter(RequestParameter.parameterAddedAdded)) &&  (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged)) && (parameters.hasParameter(RequestParameter.parameterchangedDeleted))
					&& (parameters.hasParameter(RequestParameter.ParameterLocalPort))) {
				//?paste
				_type = requestTypeSynchronize1;
			}
			if ((parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport1)) && (parameters.hasParameter(RequestParameter.ParameterURL))
					&&  (parameters.hasParameter(RequestParameter.parameterAddedAdded)) &&  (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged)) && (parameters.hasParameter(RequestParameter.parameterchangedDeleted))
					&& (parameters.hasParameter(RequestParameter.ParameterLocalPort))) {
				//?paste
				_type = requestTypeSynchronizeThenExport1;
			}
		}
		else if(parameters.size() == 10){
			if ((parameters.hasParameter(RequestParameter.ParameterSynchronize2)) && (parameters.hasParameter(RequestParameter.ParameterURL))
					&&  (parameters.hasParameter(RequestParameter.parameterAddedAdded)) &&  (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged)) && (parameters.hasParameter(RequestParameter.parameterchangedDeleted))
					&& (parameters.hasParameter(RequestParameter.ParameterLocalPort)) && (parameters.hasParameter(RequestParameter.parameterLocalAdded))
					&& (parameters.hasParameter(RequestParameter.parameterLocalChanged)) && (parameters.hasParameter(RequestParameter.parameterLocalDeleted))) {
				//?paste
				_type = requestTypeSynchronize2;
			}
			if ((parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport2)) && (parameters.hasParameter(RequestParameter.ParameterURL))
					&&  (parameters.hasParameter(RequestParameter.parameterAddedAdded)) &&  (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged)) && (parameters.hasParameter(RequestParameter.parameterchangedDeleted))
					&& (parameters.hasParameter(RequestParameter.ParameterLocalPort)) && (parameters.hasParameter(RequestParameter.parameterLocalAdded))
					&& (parameters.hasParameter(RequestParameter.parameterLocalChanged)) && (parameters.hasParameter(RequestParameter.parameterLocalDeleted))) {
				//?paste
				_type = requestTypeSynchronizeThenExport2;
			}
		}
		else if(parameters.size() == 8){
			if ((parameters.hasParameter(RequestParameter.ParameterSynchronize)) && (parameters.hasParameter(RequestParameter.ParameterURL))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteAdded))&& (parameters.hasParameter(RequestParameter.parameterRemoteChanged))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteDeleted))&& (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged))&& (parameters.hasParameter(RequestParameter.parameterchangedDeleted))) {
				//?paste
				_type = requestTypeSynchronize;
			}
			if ((parameters.hasParameter(RequestParameter.ParameterLocalPort)) && (parameters.hasParameter(RequestParameter.ParameterSynchronizeThenExport))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteAdded))&& (parameters.hasParameter(RequestParameter.parameterRemoteChanged))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteDeleted))&& (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged))&& (parameters.hasParameter(RequestParameter.parameterchangedDeleted))) {
				//?paste
				_type = requestTypeSynchronizeThenExport;
			}
		} else if(parameters.size() == 9) {
			if ((parameters.hasParameter(RequestParameter.ParameterLocalPort)) && (parameters.hasParameter(RequestParameter.ParameterSynchronize))
					&& (parameters.hasParameter(RequestParameter.ParameterRemoteAddr))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteAdded))&& (parameters.hasParameter(RequestParameter.parameterRemoteChanged))
					&& (parameters.hasParameter(RequestParameter.parameterRemoteDeleted))&& (parameters.hasParameter(RequestParameter.parameterchangedChanged))
					&& (parameters.hasParameter(RequestParameter.parameterdeletedChanged))&& (parameters.hasParameter(RequestParameter.parameterchangedDeleted))) {
				//?paste
				_type = requestTypeSynchronize;
			}
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean isAction() {
		return (_type == requestTypeAction);
	}
	
	public boolean isActivate() {
		return (_type == requestTypeActivate);
	}
	
	public boolean isCopy() {
		return (_type == requestTypeCopy);
	}
	
	public boolean isCopyPasteExport() {
		return (_type == requestTypeCopyPasteExport);
	}
	
	public boolean isCreate() {
		return (_type == requestTypeCreate);
	}
	
	public boolean isCreateSchemaNode() {
		return (_type == requestTypeCreateSchemaNode);
	}
	
	public boolean isDelete() {
		return (_type == requestTypeDelete);
	}
	
	public boolean isEdit() {
		return (_type == requestTypeEdit);
	}
	
	public boolean isExportXML() {
		return (_type == requestTypeExportXML);
	}
	
	public boolean isExportJSON() {
		return (_type == requestTypeExportJSON);
	}
	
	public boolean isGet() {
		return (_type == requestTypeGet);
	}
	
	public boolean isIndex() {
		return (_type == requestTypeIndex);
	}
	
	public boolean isLayout() {
		return (_type == requestTypeLayout);
	}
	
	public boolean isPaste() {
		return (_type == requestTypePaste);
	}
	
	public boolean isPasteForm() {
		return (_type == requestTypePasteForm);
	}
	
	public boolean isReset() {
		return (_type == requestTypeReset);
	}
	
	public boolean isSearch() {
		return (_type == requestTypeSearch);
	}
	
	public boolean isSynchronizeExport() {
		return (_type == requestTypeSynchronizeExport);
	}
	
	public boolean isSynchronizeThenExport() {
		return (_type == requestTypeSynchronizeThenExport);
	}
	
	public boolean isSynchronizeThenExport1() {
		return (_type == requestTypeSynchronizeThenExport1);
	}
	
	public boolean isSynchronizeThenExport2() {
		return (_type == requestTypeSynchronizeThenExport2);
	}
	
	public boolean isSynchronizeForm() {
		return (_type == requestTypeSynchronizeForm);
	}
	
	public boolean isSynchronizeForm1() {
		return (_type == requestTypeSynchronizeFormApproach1);
	}
	
	public boolean isSynchronizeForm2() {
		return (_type == requestTypeSynchronizeFormApproach2);
	}

	public boolean isPushToRemote() {
		return (_type == requestTypePushToRemote);
	}
	
	public boolean isSynchronize() {
		return (_type == requestTypeSynchronize);
	}
	
	public boolean isSettings() {
		return (_type == requestTypeSettings);
	}
	
	public boolean isStyleSheet() {
		return (_type == requestTypeStyleSheet);
	}
	
	public boolean isTemplate() {
		return (_type == requestTypeTemplate);
	}
	
	public boolean isTimemachinePrevious() {
		return (_type == requestTypeTimemachinePrevious);
	}
	
	public boolean isTimemachineChanges() {
		return (_type == requestTypeTimemachineChanges);
	}
	
	public boolean isPageHistory() {
		return (_type == requestTypePageHistory);
	}
	
	public boolean isUnknown() {
		return (_type == requestTypeUnknown);
	}
	
	public boolean isURLDecoding() {
		return (_type == requestTypeURLDecoding);
	}

	public boolean isSynchronize1() {
		return (_type == requestTypeSynchronize1);
	}

	public boolean isSynchronize2() {
		return (_type == requestTypeSynchronize2);
	}
	
	@Override
	public String toString() {
		return String.valueOf(_type);
	}
}
