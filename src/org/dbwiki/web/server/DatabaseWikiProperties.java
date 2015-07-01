package org.dbwiki.web.server;

import java.net.URL;

import org.dbwiki.web.request.parameter.RequestParameterList;

public class DatabaseWikiProperties {

	// Parameter values that indicate whether automatic schema
	// changes are allowed when importing (copy/paste, merge)
	// data
	public static final int AutoSchemaChangesAllow  = 0;
	public static final int AutoSchemaChangesIgnore = 1;
	public static final int AutoSchemaChangesNever  = 2;
	
	public static final int AuthenticateAlways = 0;
	public static final int AuthenticateNever = 1;
	public static final int AuthenticateWriteOnly = 2;
	
	public static final boolean HoldPermission = true;
	/*
	 * Private Variables
	 */
	
	private int _authentication;
	private int _autoSchemaChanges;
	private String _name;
	private URL _resource;
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
			_authentication = AuthenticateWriteOnly;
		}
		if (parameters.hasParameter(WikiServer.ParameterAutoSchemaChanges)) {
			_autoSchemaChanges = Integer.parseInt(parameters.get(WikiServer.ParameterAutoSchemaChanges).value());
		} else {
			_autoSchemaChanges = AutoSchemaChangesIgnore;
		}
		if (parameters.hasParameter(WikiServer.ParameterName)) {
			_name = parameters.get(WikiServer.ParameterName).value().toUpperCase();
		} else {
			_name = "";
		}
		if (parameters.hasParameter(WikiServer.ParameterInputFile)) {
			try {
				_resource = new URL(parameters.get(WikiServer.ParameterInputFile).value());
			} catch (Exception e) {
				_resource = null;
			}
		} else {
			_resource = null;
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
		_resource = null;
		_schema = wiki.database().schema().printSchemaHTML();
		_schemaPath = "";
		_title = wiki.getTitle();
	}
	
	public DatabaseWikiProperties() {
		
		_authentication = AuthenticateWriteOnly;
		_autoSchemaChanges = AutoSchemaChangesIgnore;
		_name = "";
		_resource = null;
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
	
	public URL getResource() {
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
