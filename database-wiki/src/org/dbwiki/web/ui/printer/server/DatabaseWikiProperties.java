package org.dbwiki.web.ui.printer.server;

import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;

// TODO: Move this to main or server.
public class DatabaseWikiProperties {

	/*
	 * Private Variables
	 */
	
	private int _authentication;
	private int _autoSchemaChanges;
	private String _name;
	private String _resource;
	private String _schema;
	private String _schemaPath;
	private String _title;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWikiProperties(RequestParameterList parameters) {
		
		if (parameters.hasParameter(WikiServer.ParameterAuthenticationMode)) {
			_authentication = Integer.parseInt(parameters.get(WikiServer.ParameterAuthenticationMode).value());
		} else {
			_authentication = DatabaseWiki.AuthenticateWriteOnly;
		}
		if (parameters.hasParameter(WikiServer.ParameterAutoSchemaChanges)) {
			_autoSchemaChanges = Integer.parseInt(parameters.get(WikiServer.ParameterAutoSchemaChanges).value());
		} else {
			_autoSchemaChanges = DatabaseWiki.AutoSchemaChangesIgnore;
		}
		if (parameters.hasParameter(WikiServer.ParameterName)) {
			_name = parameters.get(WikiServer.ParameterName).value().toUpperCase();
		} else {
			_name = "";
		}
		if (parameters.hasParameter(WikiServer.ParameterInputFile)) {
			_resource = parameters.get(WikiServer.ParameterInputFile).value();
		} else {
			_resource = "";
		}
		if (parameters.hasParameter(WikiServer.ParameterSchema)) {
			_schema = parameters.get(WikiServer.ParameterSchema).value();
		} else {
			_schema = "";
		}
		if (parameters.hasParameter(WikiServer.ParameterSchemaPath)) {
			_schemaPath = parameters.get(WikiServer.ParameterSchemaPath).value();
		} else {
			_schemaPath = "";
		}
		if (parameters.hasParameter(WikiServer.ParameterTitle)) {
			_title = parameters.get(WikiServer.ParameterTitle).value();
		} else {
			_title = "";
		}
	}
	
	public DatabaseWikiProperties(DatabaseWiki wiki) {
		
		_authentication = wiki.getAuthenticationMode();
		_autoSchemaChanges = wiki.getAutoSchemaChanges();
		_name = wiki.name();
		_resource = "";
		_schema = wiki.database().schema().printSchemaHTML();
		_schemaPath = "";
		_title = wiki.getTitle();
	}
	
	public DatabaseWikiProperties() {
		
		_authentication = DatabaseWiki.AuthenticateWriteOnly;
		_autoSchemaChanges = DatabaseWiki.AutoSchemaChangesIgnore;
		_name = "";
		_resource = "";
		_schema = "";
		_schemaPath = "";
		_title = "";
	}
	
	
	/*
	 * Public Methods
	 */
	
	public int getAuthentication() {
		return _authentication;
	}
	
	public int getAutoSchemaChanges() {
		return _autoSchemaChanges;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getResource() {
		return _resource;
	}
	
	public String getSchema() {
		return _schema;
	}
	
	public String getSchemaPath() {
		return _schemaPath;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public void setSchema(String schema) {
		_schema = schema;
	}
}
