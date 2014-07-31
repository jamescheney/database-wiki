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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Properties;
import java.util.Vector;

import java.util.zip.GZIPInputStream;

import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.StructureParser;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaParser;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.driver.rdbms.SQLVersionIndex;

import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.log.FileServerLog;
import org.dbwiki.web.log.ServerLog;
import org.dbwiki.web.log.StandardOutServerLog;

import org.dbwiki.web.request.HttpRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.ServerResponseHandler;

import org.dbwiki.web.ui.printer.server.DatabaseWikiFormPrinter;
import org.dbwiki.web.ui.printer.server.DatabaseWikiListingPrinter;
import org.dbwiki.web.ui.printer.server.DatabaseWikiProperties;
import org.dbwiki.web.ui.printer.server.ServerMenuPrinter;




/**
 * WikiServer
 * 
 * Parameters handled by this class:
 * 
 * <ul>
 * <li> AUTHENTICATION: Type of authentication (AuthenticateAlways=0,AuthenticateNever=1, AuthenticateWriteOnly=2) </li>
 * <li> BACKLOG: Web server parameter </li>
 * <li> DIRECTORY: The directory where the data resources are described. </li>
 * <li> FORM_TEMPLATE: Alternative HTML template for server homepage </li>
 * <li> HOMEPAGE_TEMPLATE: Alternative HTML template for server homepage </li>
 * <li> LOGFILE: Filename where log messages will be directed (default STDOUT) </li>
 * <li> PORT: Port the web server will listen to </li>
 * <li> THREADCOUNT: Web server parameter</li>
 * <li> RDBMS_TYPE: either MYSQL or PSQL, used by JDBCConnector </li>
 * <li> JDBC_USER: username of database account to use </li>
 * <li> JDBC_PASSWORD: Password of database account to use </li>
 * <li> JDBC_URL: URL pointing to database instance to use </li>
 * </ul>
 * @author jcheney
 *
 */


public abstract class WikiServer  implements WikiServerConstants {
	/*
	 * Public Constants
	 */
	
	public static final String ParameterAuthenticationMode = "PASSWORD";
	public static final String ParameterAutoSchemaChanges = "SCHEMA_CHANGES";
	public static final String ParameterSchemaPath = "SCHEMA_PATH";
	public static final String ParameterInputFile = "INPUT_FILE";
	public static final String ParameterName = "NAME";
	public static final String ParameterPath = "PATH";
	public static final String ParameterSchema = "SCHEMA";
	public static final String ParameterTitle = "TITLE";
		

	/*
	 * Private Constants
	 */
	
	protected static final String propertyAuthenticationMode = "AUTHENTICATION";
	protected static final String propertyBacklog = "BACKLOG";
	protected static final String propertyDirectory = "DIRECTORY";
	protected static final String propertyFormTemplate = "FORM_TEMPLATE";
	protected static final String propertyHomepageTemplate = "HOMEPAGE_TEMPLATE";
	protected static final String propertyLogFile = "LOGFILE";
	protected static final String propertyPort = "PORT";
	protected static final String propertyThreadCount = "THREADCOUNT";

	protected static final String propertyLogFileValueSTDOUT = "STDOUT";
	protected static final String propertyWikiTitle = "WIKI_TITLE";
	
	/*
	 * Private Variables
	 */
	
	protected String _wikiTitle = "Database Wiki";
	protected int _authenticationMode;
	protected DatabaseConnector _connector;
	protected File _formTemplate = null;
	protected File _homepageTemplate = null;
	protected ServerLog _serverLog = null;
	protected UserListing _users;
	protected File _directory;
	
	
	
//	private ExtendedPegDownProcessor _pegDownProcessor = null;

	public WikiServer(String prefix, Properties properties) throws org.dbwiki.exception.WikiException {
		_directory = new File(prefix + properties.getProperty(propertyDirectory));
		
		initServerLog(properties.getProperty(propertyLogFile));
		
		initFormTemplate(properties.getProperty(propertyFormTemplate));
		
		initHomepageTemplate(properties.getProperty(propertyHomepageTemplate));
		
		_authenticationMode = Integer.parseInt(properties.getProperty(propertyAuthenticationMode));
		
		// Database Connection
		_connector = new DatabaseConnectorFactory().getConnector(properties);
		
		initWikiTitle(properties.getProperty(propertyWikiTitle));
		
		
		
		// Read information about users and DatabassWikis
		// from the database we are connected to
		try {
			Connection con = _connector.getConnection();
			getUserListing(con);	
			getWikiListing(con);
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	public WikiServer(Properties properties) throws org.dbwiki.exception.WikiException {
		this("", properties);
	}
	
	public File directory() {
		return _directory;
	}
	
	/* Private methods to initialize data structures in constructor */
	/** Initialize the server log object
	 * 
	 */
	protected void initServerLog(String serverLogValue) {
		
		if (serverLogValue != null) {
			if (serverLogValue.equalsIgnoreCase(propertyLogFileValueSTDOUT)) {
				_serverLog = new StandardOutServerLog();
			} else {
				_serverLog = new FileServerLog(new File(serverLogValue));
			}
		}
	}

	/** 
	 * Initialize the form template File.  
	 * @param formTemplateValue
	 */
	protected void initFormTemplate(String formTemplateValue) {
		if (formTemplateValue != null) {
			_formTemplate = null;
			File file = new File(formTemplateValue);
			if (file.exists()) {
				_formTemplate = file;
			} 
		} else {
			_formTemplate = new File(directory().getAbsolutePath() + "/html/server.html");
		}	
	}
	
	/** Initialize the homepage template File.
	 * @param homepageTemplateValue
	 */
	protected void initHomepageTemplate(String homepageTemplateValue) {
		if (homepageTemplateValue != null) {
			_homepageTemplate = null;
			File file = new File(homepageTemplateValue);
			if (file.exists()) {
				_homepageTemplate = file;
			} 
		} else {
			_homepageTemplate = new File(directory().getAbsolutePath() + "/html/server-main.html");
		}
	}
	
	/** Initialize wiki title
	 * 
	 * @param wikiTitleValue
	 */
	
	protected void initWikiTitle(String wikiTitleValue) {
		if (wikiTitleValue != null) {
			_wikiTitle = wikiTitleValue;
		}
	}
	
	/** Initialize user listing from database
	 * 
	 */
	protected void getUserListing(Connection connection) throws SQLException {
		_users = new UserListing();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + RelationUser);
		while (rs.next()) {
			_users.add(new User(rs.getInt(RelUserColID), rs.getString(RelUserColLogin), rs.getString(RelUserColFullName), rs.getString(RelUserColPassword)));
		}
		rs.close();
		stmt.close();
		
	}
	
	protected abstract void getWikiListing(Connection con) throws SQLException, WikiException ;
	/*
	 * Public Methods
	 */
	
	public abstract DatabaseWiki get(int index);
	
	/** Get the DatabaseWiki with string id @name */
	public DatabaseWiki get(String name) {
		for (int iWiki = 0; iWiki < this.size(); iWiki++) {
			if (this.get(iWiki).name().equals(name)) {
				return this.get(iWiki);
			}
		}
		return null;
	}
	
	/**Depending on the file version of the requested css-definition-file
	 * this method either returns (as a String) the content of the default
	 * css-definition-file (in org.dbwiki.server.files.html.style) or it
	 * reads the information from the database (-> readConfigFile()).
	 */
	public String getLayout(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return null;
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValLayout, fileVersion);
		}
	}

	/** Depending on the file version of the requested template-file
	 * this method either returns (as a String) the content of the default
	 * template-file (in org.dbwiki.server.files.html) or it
	 * reads the information from the database (-> readConfigFile()).
	 */
	public String getTemplate(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return this.readDefaultConfigFile("/html/wiki_template.html");
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValTemplate, fileVersion);
		}
	}
	
	/** Depending on the file version of the requested css-definition-file
	 * this method either returns (as a String) the content of the default
	 * css-definition-file (in org.dbwiki.server.files.html.style) or it
	 * reads the information from the database (-> readConfigFile()).
	 */
	public String getStyleSheet(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return this.readDefaultConfigFile("/html/style/wiki_template.css");
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValCSS, fileVersion);
		}
	}
	
	/** Depending on the file version of the requested URL-decoding-rules-definition-file
	 * this method either returns an empty string (for ValUnknown) or it
	 * reads the information from the database (-> readConfigFile()).
	 */
	public String getURLDecoding(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return "";
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValURLDecoding, fileVersion);
		}
	}
	

	/** The list of all previous display settings for the specified wiki
	 * 
	 * @param wiki - the wiki whose settings we want
	 * @return a vector of previous display settings
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized Vector<ConfigSetting> listSettings(DatabaseWiki wiki) throws org.dbwiki.exception.WikiException {
		
		try {
			Vector<ConfigSetting> settings = new Vector<ConfigSetting>();
			ConfigSetting currentSetting = new ConfigSetting();
			settings.add(currentSetting);
			Connection con = _connector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT * FROM " + RelationPresentation + " " +
				"WHERE " + RelPresentationColDatabase + " = " + wiki.id() + " " +
				"ORDER BY " + RelPresentationColTime);
			while (rs.next()) {
				ConfigSetting setting = new ConfigSetting(new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new java.util.Date(rs.getLong(RelPresentationColTime))), currentSetting);
				switch (rs.getInt(RelPresentationColType)) {
				case RelConfigFileColFileTypeValLayout:
					setting.setLayoutVersion(rs.getInt(RelPresentationColVersion));
					break;
				case RelConfigFileColFileTypeValTemplate:
					setting.setTemplateVersion(rs.getInt(RelPresentationColVersion));
					break;
				case RelConfigFileColFileTypeValCSS:
					setting.setStyleSheetVersion(rs.getInt(RelPresentationColVersion));
					break;
				case RelConfigFileColFileTypeValURLDecoding:
					setting.setURLDecodingRulesVersion(rs.getInt(RelPresentationColVersion));
					break;
				default:
					throw new WikiFatalException("Unknown config file type");
				}
				settings.add(setting);
				currentSetting = setting;
			}
			stmt.close();
			con.close();
			return settings;
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
	}

	/** Resets the configuration files of the specified wiki to
	 * the default state
	 * @param wiki - the wiki to be reset
	 */
	public synchronized void resetWikiConfiguration(DatabaseWiki wiki) throws org.dbwiki.exception.WikiException {
		this.resetWikiConfiguration(wiki, RelConfigFileColFileVersionValUnknown, RelConfigFileColFileVersionValUnknown, RelConfigFileColFileVersionValUnknown, RelConfigFileColFileVersionValUnknown);
	}
	
	/** 
	 * Resets the configuration files of the specified wiki to
	 * the default state
	 * @param wiki
	 * @param layoutVersion
	 * @param templateVersion
	 * @param styleSheetVersion
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized void resetWikiConfiguration(DatabaseWiki wiki, int layoutVersion, int templateVersion, int styleSheetVersion, int urlDecodingVersion) throws org.dbwiki.exception.WikiException {
		try {
			Connection con = _connector.getConnection();
			Statement stmt = con.createStatement();
			stmt.execute("UPDATE " + RelationDatabase + " " +
				"SET " + RelDatabaseColCSS + " = " + styleSheetVersion + ", " +
				RelDatabaseColLayout + " = " + layoutVersion + ", " +
				RelDatabaseColTemplate + " = " + templateVersion + " " +
				"WHERE " + RelDatabaseColID + " = " + wiki.id());
			stmt.close();
			con.close();
			wiki.reset(layoutVersion, templateVersion, styleSheetVersion, urlDecodingVersion);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	/**  
	 * @return The server log
	 */
	public ServerLog serverLog() {
		// The server log used by all the wiki's to log web requests
		return _serverLog;
	}
	
	public abstract int size(); 
	

	/**  Updates a config file (css, template, or layout as specified by fileType)
		 for the given wiki (wikiID). Returns the version number of the updated
		 file. The method has to store the file content in RelationConfigFile and
		 also update the information in RelationDatabase, i.e., whenever a config
		 file is updated the new version is set to be the default version of this
		 file type for the respective wiki.
		 * 
		 * @param wikiID - index of wiki
		 * @param fileType - type of file 
		 * @param value - string containing file contents
		 * @param user - user who updated the file
		 * @return the new version number of the file
		 * @throws org.dbwiki.exception.WikiException
		 */
	public synchronized int updateConfigFile(int wikiID, int fileType, String value, User user) throws org.dbwiki.exception.WikiException {
		
		try {
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			Statement stmt = con.createStatement();
			// Here we rely on the assumption that only one WikiServer is running at
			// a time, i.e. no one else will be able to modify the configuration
			// table in parallel. Otherwise, the following insert may fail if the
			// max. version number changes inbetween this query and the insert.
			// The setAutoCommit(false), however, should prevent any major damage
			// in case the assumption does not hold.
			ResultSet rs = stmt.executeQuery(
				"SELECT MAX(" + RelPresentationColVersion + ") " + 
				"FROM " + RelationPresentation + " " +
				"WHERE " + RelPresentationColDatabase + " = " + wikiID + " " +
				"AND " + RelPresentationColType + " = " + fileType);
			rs.next();
			// User generated config file versions start from 1
			int version = Math.max(rs.getInt(1) + 1, 1);
			rs.close();
			try {
				PreparedStatement pStmtInsertConfig = con.prepareStatement(
					"INSERT INTO " + RelationPresentation + " (" +
						RelPresentationColType + ", " +
						RelPresentationColVersion + ", " +
						RelPresentationColTime + ", " +
						RelPresentationColUser + ", " +
						RelPresentationColValue + ", " +
						RelPresentationColDatabase + ") VALUES(?, ?, ?, ?, ?, ?)");
				pStmtInsertConfig.setInt(1, fileType);
				pStmtInsertConfig.setInt(2, version);
				pStmtInsertConfig.setLong(3, new java.util.Date().getTime());
				if (user != null) {
					pStmtInsertConfig.setInt(4, user.id());
				} else {
					pStmtInsertConfig.setInt(4, User.UnknownUserID);
				}
				pStmtInsertConfig.setString(5, value);
				pStmtInsertConfig.setInt(6, wikiID);
				pStmtInsertConfig.execute();
				pStmtInsertConfig.close();
				String sql = "UPDATE " + RelationDatabase + " SET ";
				if (fileType == RelConfigFileColFileTypeValCSS) {
					sql = sql + RelDatabaseColCSS;
				} else if (fileType == RelConfigFileColFileTypeValLayout) {
					sql = sql + RelDatabaseColLayout;
				} else if (fileType == RelConfigFileColFileTypeValTemplate) {
					sql = sql + RelDatabaseColTemplate;
				} else if (fileType == RelConfigFileColFileTypeValURLDecoding) {
					sql = sql + RelDatabaseColURLDecoding;
				}
				sql = sql +	" = " + version + " WHERE " + RelDatabaseColID + " = " + wikiID;
				stmt.execute(sql);
				stmt.close();
				con.commit();
			} catch (java.sql.SQLException sqlException) {
				con.rollback();
				con.close();
				throw new WikiFatalException(sqlException);
			}
			con.setAutoCommit(true);
			con.close();
			return version;
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	/**
	 * 
	 * @return The full list of registered wiki users
	 */
	public UserListing users() {
		return _users;
	}
	
	
	/*
	 * Private Methods
	 */

	/** Creates appropriate response handler for homepage 
	 * 
	 */
	protected ServerResponseHandler getHomepageResponseHandler(HttpRequest request) {
		ServerResponseHandler responseHandler = new ServerResponseHandler(request, _wikiTitle);
		responseHandler.put(HtmlContentGenerator.ContentMenu, new ServerMenuPrinter(this));
		responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiListingPrinter(this));

		return responseHandler;
	}

	/** Creates appropriate response handler for new DatabaseWiki request.
	 *  FIXME #import: Make path into a parameter that can be passed into the form and infer a "good" path.
	 * @param request
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected ServerResponseHandler getInsertWikiResponseHandler(HttpRequest request) throws org.dbwiki.exception.WikiException, MalformedURLException, IOException {
		
		DatabaseWikiProperties properties = new DatabaseWikiProperties(request.parameters());
		
		DatabaseSchema databaseSchema = null;
		
		//
		// Validate parameter values.
		//
		int message = DatabaseWikiFormPrinter.MessageNone;
		
		//
		// Validate name
		//
		if (properties.getName().equals("")) {
			message = DatabaseWikiFormPrinter.MessageNoName;
		} else if (!this.isValidWikiName(properties.getName())) {
			message = DatabaseWikiFormPrinter.MessageInvalidName;
		} else {
			for (int iWiki = 0; iWiki < this.size(); iWiki++) {
				if (this.get(iWiki).name().equalsIgnoreCase(properties.getName())) {
					message = DatabaseWikiFormPrinter.MessageDuplicateName;
				}
			}
		}
		
		//
		// Validate title
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (properties.getTitle().equals(""))) {
			message = DatabaseWikiFormPrinter.MessageNoTitle;
		}
		
		//
		// Validate schema
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (!properties.getSchema().equals(""))) {
			try {
				databaseSchema = new SchemaParser().parse(properties.getSchema());
			} catch (org.dbwiki.exception.WikiException wikiException) {
				wikiException.printStackTrace();
				message = DatabaseWikiFormPrinter.MessageErroneousSchema;
			}
		}
		
		//
		// Validate resource. If no schema is specified then generate schema from
		// given resource and let the user edit/verify the schema.
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (!properties.getResource().equals(""))) {
			InputStream in = null;
			try {
				if (properties.getResource().endsWith(".gz")) {
					in = new GZIPInputStream(new URL(properties.getResource()).openStream());
				} else {
					in = new URL(properties.getResource()).openStream();
				}
			} catch (java.net.MalformedURLException mue) {
				message = DatabaseWikiFormPrinter.MessageFileNotFound;
			} catch (java.io.IOException ioe) {
				message = DatabaseWikiFormPrinter.MessageFileNotFound;
			}
			if ((message == DatabaseWikiFormPrinter.MessageNone) && (properties.getSchema().equals(""))) {
				try {
					// FIXME #schemaparsing: Make this a method somewhere...
					StructureParser structureParser = new StructureParser();
					new SAXCallbackInputHandler(structureParser, false).parse(in, false, false);
					if (structureParser.hasException()) {
						throw structureParser.getException();
					}
					databaseSchema = structureParser.getDatabaseSchema();
					properties.setSchema(databaseSchema.printSchema());
				} catch (Exception excpt) {
					throw new WikiFatalException(excpt);
				}
				message = DatabaseWikiFormPrinter.MessageEditSchema;
			}
			if (in != null) {
				try {
					in.close();
				} catch (java.io.IOException ioe) {
				}
			}
		}
		
		if (message != DatabaseWikiFormPrinter.MessageNone) {
			//
			// If parameter validation results in an error message the create wiki
			// form is re-displayed showing the error message.
			//
			ServerResponseHandler responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Create Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(properties, RequestParameterAction.ActionInsert, "Create Database Wiki", message));
			return responseHandler;
		} else {
			//
			// If the parameter values are valid the database wiki is created
			//
			if ((request.user() == null) && (_authenticationMode != DatabaseWiki.AuthenticateNever)) {
				throw new WikiFatalException("User information is missing");
			}
			
			
			if (databaseSchema != null) {
				// Path is either the value of the form parameter SCHEMA_PATH or
				// the path of the schema root node;
				String path = null;
				if (!properties.getSchemaPath().equals("")) {
					path = properties.getSchemaPath();
					databaseSchema = databaseSchema.getSubSchema(path);
				} else {
					path = databaseSchema.root().path();
				}
				
				URL resourceURL = null;
				if (!properties.getResource().equals("")) {
					resourceURL = new URL(properties.getResource());
				}
				
				try {
					registerDatabase(properties.getName(), properties.getTitle(), path, resourceURL, databaseSchema, request.user(),
							properties.getAuthentication(), properties.getAutoSchemaChanges());
				} catch (java.sql.SQLException sqlException) {
					throw new WikiFatalException(sqlException);
				}
			} else {
				throw new WikiFatalException("Empty Schema");
			}
						
			return this.getHomepageResponseHandler(request);
		}
	}

	
	public abstract void registerDatabase(String name, String title, String path,
			URL resourceURL, DatabaseSchema databaseSchema, User user,
			int authentication, int autoSchemaChanges)
	throws NumberFormatException, WikiException, SQLException;

	// FIXME: This inner class should be used for other database creation forms/tools 
	class CreateDatabaseRecord {
		public String name;
		public String title;
		public int authenticationMode;
		public int autoSchemaChanges;
		DatabaseSchema databaseSchema;
		User user;
		
		CreateDatabaseRecord(String _name, String _title,int _auth, int _auto, DatabaseSchema _databaseSchema, User _user) {
			name = _name;
			title = _title;
			authenticationMode = _auth;
			databaseSchema = _databaseSchema;
			user = _user;
		}
		
	
	public int createDatabase(Connection con,SQLVersionIndex versionIndex) 
		throws WikiException, SQLException {
			int wikiID;
			_connector.createDatabase(con, name, databaseSchema, user, versionIndex);
	
			PreparedStatement pStmt = con.prepareStatement(
					"INSERT INTO " + RelationDatabase + "(" +
					RelDatabaseColName + ", " + RelDatabaseColTitle + ", " + 
					RelDatabaseColAuthentication + ", " + RelDatabaseColAutoSchemaChanges + ", " +
					RelDatabaseColUser + ") VALUES(? , ? , ? , ? , ?)", Statement.RETURN_GENERATED_KEYS);
			pStmt.setString(1, name);
			pStmt.setString(2, title);
			pStmt.setInt(3, authenticationMode);
			pStmt.setInt(4, autoSchemaChanges);
			if (user != null) {
				pStmt.setInt(5, user.id());
			} else {
				pStmt.setInt(5, User.UnknownUserID);
			}
			pStmt.execute();
			ResultSet rs = pStmt.getGeneratedKeys();
			if (rs.next()) {
				wikiID = rs.getInt(1);
			} else {
				throw new WikiFatalException("There are no generated keys.");
			}
			rs.close();
			return wikiID;
		}
	}
	
	
	/** 
	 * Gets the wiki corresponding to parameter "key" of request parameters.
	 * First tries to interpret value as a wiki index, if that fails, tries to interpret as a case-insensitive string.
	 * TODO:  Clean this up.
	 * @param request
	 * @param key
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	protected DatabaseWiki getRequestWiki(HttpRequest request, String key) throws org.dbwiki.exception.WikiException {
		RequestParameter parameter = request.parameters().get(key);
		if (parameter.hasValue()) {
			try {
				int wikiID = Integer.parseInt(parameter.value());
				for (int iWiki = 0; iWiki < this.size(); iWiki++) {
					if (this.get(iWiki).id() == wikiID) {
						return this.get(iWiki);
					}
				}
			} catch (NumberFormatException exception) {
				for (int iWiki = 0; iWiki < this.size(); iWiki++) {
					if (this.get(iWiki).name().equalsIgnoreCase(parameter.value())) {
						return this.get(iWiki);
					}
				}
			}
		}
		throw new WikiRequestException(WikiRequestException.InvalidRequest, request.toString());
	}
	
	/** Constructs a response handler for a DatabaseWiki update page.
	 * 
	 * @param request
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	protected ServerResponseHandler getUpdateWikiResponseHandler(HttpRequest request) throws org.dbwiki.exception.WikiException {
		//TODO: Simplify validation/control flow.  
		
		// Validate data passed in from form
		// FIXME #security: Check that the other fields have reasonable values!
		DatabaseWiki wiki = this.getRequestWiki(request, ParameterName);
		
		DatabaseWikiProperties properties = new DatabaseWikiProperties(request.parameters());
		
		int message = DatabaseWikiFormPrinter.MessageNone;
		if (properties.getTitle().equals("")) {
			message = DatabaseWikiFormPrinter.MessageNoTitle;
		}
		
		// If invalid, pass back appropriate message.
		if (message != DatabaseWikiFormPrinter.MessageNone) {
			ServerResponseHandler responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Edit Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(properties, RequestParameterAction.ActionUpdate, "Edit Database Wiki", message));
			return responseHandler;
		} else {
			// Otherwise, apply the changes.
			if ((request.user() == null) && (_authenticationMode != DatabaseWiki.AuthenticateNever)) {
				throw new WikiFatalException("User information is missing");
			}
			try {
				Connection con = _connector.getConnection();
				con.setAutoCommit(false);
				PreparedStatement pStmt = con.prepareStatement("UPDATE " + RelationDatabase + " " + 
					"SET " + RelDatabaseColTitle + " = ?, " +
					RelDatabaseColAuthentication + " = ?, " +
					RelDatabaseColAutoSchemaChanges + " = ? " +
					"WHERE " + RelDatabaseColID + " = " + wiki.id());
				pStmt.setString(1, properties.getTitle());
				pStmt.setInt(2, properties.getAuthentication());
				pStmt.setInt(3, properties.getAutoSchemaChanges());
				pStmt.execute();
				pStmt.close();
				con.commit();
				con.close();
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
			wiki.setAuthenticationMode(properties.getAuthentication());
			wiki.setAutoSchemaChanges(properties.getAutoSchemaChanges());
			wiki.setTitle(properties.getTitle());
			sortWikiListing();
			return this.getHomepageResponseHandler(request);
		}
	}

	/** Checks whether a given string is a valid short wiki name, i.e. suitable for use
	 * as a prefix to tables in DBMS.
	 * @param name
	 * @return
	 */
	private boolean isValidWikiName(String name) {
		if (name.length() > 16) {
			return false;
		} else {
			for (int iChar = 0; iChar < name.length(); iChar++) {
				char c = name.charAt(iChar);
				if ((!Character.isDigit(c)) && (!Character.isLetter(c)) && (c != '_') && (c != '-')) {
					return false;
				}
			}
		}
		return true;
	}
	
	/** Finds a default configuration file (layout, template, css) at a given 
	 *  path from the server home directory
	 * 
	 * @param path
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	private String readDefaultConfigFile(String path) throws org.dbwiki.exception.WikiException{
		try {
			String value = null;
			
			File file = new File(directory().getAbsolutePath() + path);
			if ((file.exists()) && (!file.isDirectory())) {	
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line;
				while ((line = in.readLine()) != null) {
					if (value == null) {
						value = line;
					} else {
						value = value + "\n" + line;
					}
				}
				in.close();
				return value;
			} else {
				System.out.println("File Not Found: " + path);
				throw new WikiFatalException("File Not Found: " + path);
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	/** Reads a config file of a given type and version from the database.
	 * 
	 * @param wikiID
	 * @param fileType
	 * @param fileVersion
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public String readConfigFile(int wikiID, int fileType, int fileVersion) throws org.dbwiki.exception.WikiException {
		String value = null;
		try {
			Connection con = _connector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + RelPresentationColValue + " " +
				"FROM " + RelationPresentation + " " +
				"WHERE " + RelPresentationColDatabase + " = " + wikiID + " " +
				" AND " + RelPresentationColType + " = " + fileType + " " +
				"AND " + RelPresentationColVersion + " = " + fileVersion);
			if (rs.next()) {
				value = rs.getString(1);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		return value;
	}

	
	public abstract void sortWikiListing ();
}
