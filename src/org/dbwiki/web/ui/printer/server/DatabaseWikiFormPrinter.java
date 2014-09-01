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
package org.dbwiki.web.ui.printer.server;

import org.dbwiki.exception.WikiException;
import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.CSS;
import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Generates a form that allows creating a new Database Wiki
 * 
 * @author jcheney
 *
 */
public class DatabaseWikiFormPrinter implements HtmlContentPrinter {
	/*
	 * Public Constants
	 */
	
	public static final int MessageNone                 = -1;
	public static final int MessageNoName               = 0;
	public static final int MessageDuplicateName        = 1;
	public static final int MessageFileNotFound         = 2;
	public static final int MessageInvalidName          = 3;
	public static final int MessageErroneousConstraints = 4;
	public static final int MessageErroneousSchema      = 5;
	public static final int MessageNoTitle              = 6;
	public static final int MessageEditSchema           = 7;
	
	/*
	 * Private Variables
	 */
	
	private String _action;
	private String _headline;
	private DatabaseWikiProperties _properties;
	private int _message;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWikiFormPrinter(DatabaseWikiProperties properties, String action, String headline, int message) {
		_action = action;
		_properties = properties;
		_headline = headline;
		_message = message;
	}
	
	public DatabaseWikiFormPrinter(DatabaseWikiProperties properties, String action, String headline) {
		this(properties, action, headline, MessageNone);
	}

	public DatabaseWikiFormPrinter(String headline) {
		this(new DatabaseWikiProperties(), RequestParameterAction.ActionInsert, headline, MessageNone);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph(_headline, CSS.CSSHeadline);

		//printer.openUPLOADFORM("frmCreateWiki", "POST", "/");
		printer.openFORM("frmCreateWiki", "POST", "/");

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		printer.openTABLE(CSS.CSSFormFrame);
		
		//
		// Short Name
		//
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Short Name");
		printer.closeTD();
		if (_action.equals(RequestParameterAction.ActionInsert)) {
			if ((_message == MessageNoName) || (_message == MessageInvalidName)) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("Please enter a valid name.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _properties.getName());
				printer.closeTD();
			} else if (_message == MessageDuplicateName) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("The name " + _properties.getName() + " already exists.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _properties.getName());
				printer.closeTD();
			} else if (_message != MessageNone) {
				printer.openTD(CSS.CSSFormText);
				printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
				printer.text(_properties.getName());
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _properties.getName());
				printer.closeTD();
			}
		} else {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterName, _properties.getName());
			printer.text(_properties.getName());
			printer.closeTD();
		}
		printer.closeTR();
		
		//
		// Title
		//
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Title");
		printer.closeTD();
		if (_message == MessageNoTitle) {
			printer.openTD(CSS.CSSFormMessage);
			printer.text("Please enter a valid title.");
			printer.addBR();
			printer.addBR();
			printer.addTEXTAREA(WikiServer.ParameterTitle, "90", _properties.getTitle());
			printer.closeTD();
		} else if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterTitle, _properties.getTitle());
			printer.text(_properties.getTitle());
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addTEXTAREA(WikiServer.ParameterTitle, "90", _properties.getTitle());
			printer.closeTD();
		}
		printer.closeTR();
		
		//
		// Authentication Mode
		//
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Authentication mode");
		printer.closeTD();

		if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterAuthenticationMode, String.valueOf(_properties.getAuthentication()));
			if (_properties.getAuthentication() == WikiAuthenticator.AuthenticateNever) {
				printer.text("Never login (<i>NO AUTHENTICATION</i>)");
			} else if (_properties.getAuthentication() == WikiAuthenticator.AuthenticateWriteOnly) {
				printer.text("Login for updates (<i>WRITE-ONLY AUTHENTICATION</i>)");
			} else if (_properties.getAuthentication() == WikiAuthenticator.AuthenticateAlways) {
				printer.text("Always login (<i>FULL AUTHENTICATION</i>)");
			}
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addRADIOBUTTON("Never login (<i>NO AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateNever), (_properties.getAuthentication() == WikiAuthenticator.AuthenticateNever));
			printer.addBR();
			printer.addRADIOBUTTON("Login for updates (<i>WRITE-ONLY AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateWriteOnly), (_properties.getAuthentication() == WikiAuthenticator.AuthenticateWriteOnly));
			printer.addBR();
			printer.addRADIOBUTTON("Always login (<i>FULL AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateAlways), (_properties.getAuthentication() == WikiAuthenticator.AuthenticateAlways));
			printer.closeTD();
		}
		
		printer.closeTR();
		
		//
		// Authorization Mode
		//
		if(_headline.equals("Edit Database Wiki")){
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Authorization by users");
			printer.closeTD();
			
			printer.openTD(CSS.CSSFormText);
			printer.link("?"+RequestParameter.ParameterAuthorization + "=" + _properties.getName(), "Manage access authority");
			printer.closeTD();
	
			printer.closeTR();
		}
		//
		// Schema 
		//
		
		if (_action.equals(RequestParameterAction.ActionInsert)) {
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Schema");
			printer.closeTD();
			if (_message == MessageErroneousSchema) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("The given schema is invalid.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _properties.getSchema());
				printer.closeTD();
			} else if (_message == MessageEditSchema) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("Please specify the target path and schema for the given XML resource, or modify the inferred one below.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _properties.getSchema());
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _properties.getSchema());
				printer.closeTD();
			}
			printer.closeTR();
		} else {
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Schema");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			printer.text(_properties.getSchema());
			printer.closeTD();
			printer.closeTR();
		}
		
		//
		// Automatic Schema Changes
		//
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Automatic schema changes");
		printer.closeTD();

		if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterAutoSchemaChanges, String.valueOf(_properties.getAutoSchemaChanges()));
			if (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesNever) {
				printer.text("Never change schema automatically.");
			} else if (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesIgnore) {
				printer.text("Ignore unknown node types.");
			} else if (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesAllow) {
				printer.text("Automatically add new node types if necessary.");
			}
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addRADIOBUTTON("Never change schema automatically.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesNever), (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesNever));
			printer.addBR();
			printer.addRADIOBUTTON("Ignore unknown node types.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesIgnore), (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesIgnore));
			printer.addBR();
			printer.addRADIOBUTTON("Automatically add new node types if necessary.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesAllow), (_properties.getAutoSchemaChanges() == DatabaseWiki.AutoSchemaChangesAllow));
			printer.closeTD();
		}
		printer.closeTR();
		
		//
		// Data file & Schema path
		//
		
		if (_action.equals(RequestParameterAction.ActionInsert)) {
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Data file (URL)");
			printer.closeTD();
			if (_message == MessageFileNotFound) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("Resource not found.");
				printer.addBR();
				printer.addBR();
				//printer.addFILE(WikiServer.ParameterInputFile);
				printer.addTEXTAREA(WikiServer.ParameterInputFile, "90", _properties.getResource());
				printer.closeTD();
			} else if (_message != MessageNone) {
				printer.openTD(CSS.CSSFormText);
				printer.addHIDDEN(WikiServer.ParameterInputFile, _properties.getResource());
				printer.text(_properties.getResource());
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				//printer.addFILE(WikiServer.ParameterInputFile);
				printer.addTEXTAREA(WikiServer.ParameterInputFile, "90", _properties.getResource());
				printer.closeTD();
			}
			printer.closeTR();
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Schema path");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			//if (_message == MessageNone) {
				printer.addTEXTAREA(WikiServer.ParameterSchemaPath, "90", _properties.getSchemaPath());
			//} else {
			//	printer.addHIDDEN(WikiServer.ParameterSchemaPath, _properties.getSchemaPath());
			//	printer.text(_properties.getSchemaPath());
			//}
			printer.closeTD();
			printer.closeTR();
		}
		
		printer.closeTABLE();

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addREALBUTTON("submit",
				"action", _action, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addREALBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
