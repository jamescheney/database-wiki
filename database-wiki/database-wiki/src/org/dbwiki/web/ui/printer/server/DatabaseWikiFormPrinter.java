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
	
	public static final int MessageNone            = -1;
	public static final int MessageNoName          = 0;
	public static final int MessageDuplicateName   = 1;
	public static final int MessageFileNotFound    = 2;
	public static final int MessageInvalidName     = 3;
	public static final int MessageErroneousSchema = 4;
	public static final int MessageNoTitle         = 5;
	public static final int MessageEditSchema      = 6;
	
	/*
	 * Private Variables
	 */
	
	private String _action;
	private String _authentication;
	private String _autoSchemaChanges;
	private int _message;
	private String _name;
	private String _resource;
	private String _schema;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWikiFormPrinter(String action, String name, String title, String authentication, String autoSchemaChanges, String schema, String resource, int message) {
		_action = action;
		_authentication = authentication;
		_autoSchemaChanges = autoSchemaChanges;
		_message = message;
		_name = name;
		_resource = resource;
		_schema = schema;
		_title = title;
	}
	
	public DatabaseWikiFormPrinter(String action, String name, String title, int authenticationMode, int autoSchemaChanges) {
		this(action, name, title, Integer.toString(authenticationMode), Integer.toString(autoSchemaChanges), "", "", MessageNone);
	}
	
	public DatabaseWikiFormPrinter(String action, String name, String title, String authenticationMode, String autoSchemaChanges, int message) {
		this(action, name, title, authenticationMode, autoSchemaChanges, "", "", message);
	}

	public DatabaseWikiFormPrinter() {
		this(RequestParameterAction.ActionInsert, "", "", Integer.toString(WikiAuthenticator.AuthenticateWriteOnly), Integer.toString(DatabaseWiki.AutoSchemaChangesIgnore), "", "", MessageNone);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void print(HtmlLinePrinter printer) throws WikiException {
		printer.paragraph("Create Database Wiki", CSS.CSSHeadline);

		//printer.openUPLOADFORM("frmCreateWiki", "POST", "/");
		printer.openFORM("frmCreateWiki", "POST", "/");

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);
		
		printer.openTABLE(CSS.CSSFormFrame);
		
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
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _name);
				printer.closeTD();
			} else if (_message == MessageDuplicateName) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("The name " + _name + " already exists.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _name);
				printer.closeTD();
			} else if (_message != MessageNone) {
				printer.openTD(CSS.CSSFormText);
				printer.addHIDDEN(WikiServer.ParameterName, _name);
				printer.text(_name);
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(WikiServer.ParameterName, "90", _name);
				printer.closeTD();
			}
		} else {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterName, _name);
			printer.text(_name);
			printer.closeTD();
		}
		printer.closeTR();
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Title");
		printer.closeTD();
		if (_message == MessageNoTitle) {
			printer.openTD(CSS.CSSFormMessage);
			printer.text("Please enter a valid title.");
			printer.addBR();
			printer.addBR();
			printer.addTEXTAREA(WikiServer.ParameterTitle, "90", _title);
			printer.closeTD();
		} else if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterTitle, _title);
			printer.text(_title);
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addTEXTAREA(WikiServer.ParameterTitle, "90", _title);
			printer.closeTD();
		}
		printer.closeTR();
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Authentication mode");
		printer.closeTD();

		if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterAuthenticationMode, _authentication);
			if (_authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateNever))) {
				printer.text("Never login (<i>NO AUTHENTICATION</i>)");
			} else if (_authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateWriteOnly))) {
				printer.text("Login for updates (<i>WRITE-ONLY AUTHENTICATION</i>)");
			} else if (_authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateAlways))) {
				printer.text("Always login (<i>FULL AUTHENTICATION</i>)");
			}
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addRADIOBUTTON("Never login (<i>NO AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateNever), _authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateNever)));
			printer.addBR();
			printer.addRADIOBUTTON("Login for updates (<i>WRITE-ONLY AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateWriteOnly), _authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateWriteOnly)));
			printer.addBR();
			printer.addRADIOBUTTON("Always login (<i>FULL AUTHENTICATION</i>)", WikiServer.ParameterAuthenticationMode, Integer.toString(WikiAuthenticator.AuthenticateAlways), _authentication.equals(Integer.toString(WikiAuthenticator.AuthenticateAlways)));
			printer.closeTD();
		}
		
		printer.closeTR();

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
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _schema);
				printer.closeTD();
			} else if (_message == MessageEditSchema) {
				printer.openTD(CSS.CSSFormMessage);
				printer.text("Please specify the target pat and schema for the given resource.");
				printer.addBR();
				printer.addBR();
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _schema);
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(WikiServer.ParameterSchema, "90", "15", true, _schema);
				printer.closeTD();
			}
			printer.closeTR();
		}
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Automatic schema changes");
		printer.closeTD();

		if (_message != MessageNone) {
			printer.openTD(CSS.CSSFormText);
			printer.addHIDDEN(WikiServer.ParameterAutoSchemaChanges, _autoSchemaChanges);
			if (_autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesNever))) {
				printer.text("Never change schema automatically.");
			} else if (_autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesIgnore))) {
				printer.text("Ignore unknown node types.");
			} else if (_autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesAllow))) {
				printer.text("Automatically add new node types if necessary.");
			}
			printer.closeTD();
		} else {
			printer.openTD(CSS.CSSFormControl);
			printer.addRADIOBUTTON("Never change schema automatically.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesNever), _autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesNever)));
			printer.addBR();
			printer.addRADIOBUTTON("Ignore unknown node types.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesIgnore), _autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesIgnore)));
			printer.addBR();
			printer.addRADIOBUTTON("Automatically add new node types if necessary.", WikiServer.ParameterAutoSchemaChanges, Integer.toString(DatabaseWiki.AutoSchemaChangesAllow), _autoSchemaChanges.equals(Integer.toString(DatabaseWiki.AutoSchemaChangesAllow)));
			printer.closeTD();
		}
		printer.closeTR();
		
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
				printer.addTEXTAREA(WikiServer.ParameterInputFile, "90", _resource);
				printer.closeTD();
			} else if (_message != MessageNone) {
				printer.openTD(CSS.CSSFormText);
				printer.addHIDDEN(WikiServer.ParameterInputFile, _resource);
				printer.text(_resource);
				printer.closeTD();
			} else {
				printer.openTD(CSS.CSSFormControl);
				//printer.addFILE(WikiServer.ParameterInputFile);
				printer.addTEXTAREA(WikiServer.ParameterInputFile, "90", _resource);
				printer.closeTD();
			}
			printer.closeTR();
		}
		
		printer.closeTABLE();

		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addBUTTON("image", "action", _action, "/pictures/button_save.gif");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addBUTTON("image", "action", RequestParameterAction.ActionCancel, "/pictures/button_cancel.gif");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();
	}
}
