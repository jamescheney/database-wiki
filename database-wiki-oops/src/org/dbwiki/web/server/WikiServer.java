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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import java.net.InetSocketAddress;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import org.dbwiki.data.io.XMLDocumentImportReader;
import org.dbwiki.data.io.XMLDocumentSchemaParser;

import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.ImportEntity;
import org.dbwiki.data.schema.SchemaParser;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.driver.rdbms.DatabaseImportHandler;
import org.dbwiki.driver.rdbms.RDBMSDatabase;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.user.SQLUserListing;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.FileNotFoundPage;
import org.dbwiki.web.html.RedirectPage;

import org.dbwiki.web.log.FileServerLog;
import org.dbwiki.web.log.ServerLog;
import org.dbwiki.web.log.StandardOutServerLog;

import org.dbwiki.web.request.ServerRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.security.WikiAuthenticator;

import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;

import org.dbwiki.web.ui.ServerResponseHandler;

import org.dbwiki.web.ui.printer.server.DatabaseWikiFormPrinter;
import org.dbwiki.web.ui.printer.server.DatabaseWikiListingPrinter;
import org.dbwiki.web.ui.printer.server.ServerMenuPrinter;

//import org.pegdown.ExtendedPegDownProcessor;
//import org.pegdown.ExtendedPegDownProcessor;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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

public class WikiServer extends FileServer implements WikiServerConstants {
	/*
	 * Public Constants
	 */
	
	public static final String ParameterAuthenticationMode = "PASSWORD";
	public static final String ParameterAutoSchemaChanges = "SCHEMA_CHANGES";
	public static final String ParameterInputFile = "INPUT_FILE";
	public static final String ParameterName = "NAME";
	public static final String ParameterSchema = "SCHEMA";
	public static final String ParameterTitle = "TITLE";
		

	/*
	 * Private Constants
	 */
	
	private static final String propertyAuthenticationMode = "AUTHENTICATION";
	private static final String propertyBacklog = "BACKLOG";
	private static final String propertyDirectory = "DIRECTORY";
	private static final String propertyFormTemplate = "FORM_TEMPLATE";
	private static final String propertyHomepageTemplate = "HOMEPAGE_TEMPLATE";
	private static final String propertyLogFile = "LOGFILE";
	private static final String propertyPort = "PORT";
	private static final String propertyThreadCount = "THREADCOUNT";

	private static final String propertyLogFileValueSTDOUT = "STDOUT";
	private static final String propertyWikiTitle = "WIKI_TITLE";
	
	/*
	 * Private Variables
	 */
	
	private String _wikiTitle = "Database Wiki";
	private int _authenticationMode;
	private int _backlog;
	private DatabaseConnector _connector;
	private URL _formTemplate = null;
	private URL _homepageTemplate = null;
	private int _port;
	private ServerLog _serverLog = null;
	private int _threadCount;
	private UserListing _users;
	private HttpServer _webServer = null;
	private Vector<DatabaseWiki> _wikiListing;

//	private ExtendedPegDownProcessor _pegDownProcessor = null;

	/*
	 * Constructors
	 */
	
	public WikiServer(Properties properties) throws org.dbwiki.exception.WikiException {
		super(org.dbwiki.lib.IO.getFile(properties.getProperty(propertyDirectory)));
		
		// Web Server Properties
		_backlog = Integer.parseInt(properties.getProperty(propertyBacklog));
		_port = Integer.parseInt(properties.getProperty(propertyPort));
		_threadCount = Integer.parseInt(properties.getProperty(propertyThreadCount));

		if (properties.getProperty(propertyLogFile) != null) {
			String serverLogValue = properties.getProperty(propertyLogFile);
			if (serverLogValue.equalsIgnoreCase(propertyLogFileValueSTDOUT)) {
				_serverLog = new StandardOutServerLog();
			} else {
				_serverLog = new FileServerLog(org.dbwiki.lib.IO.getFile(serverLogValue));
			}
		}

		if (properties.getProperty(propertyFormTemplate) != null) {
			_formTemplate = null;
			try {
				File file = org.dbwiki.lib.IO.getFile(properties.getProperty(propertyFormTemplate));
				if (file.exists()) {
					_formTemplate = file.toURI().toURL();
				}
			} catch (java.io.IOException ioException) {
				_formTemplate = null;
			}
		}

		if (properties.getProperty(propertyHomepageTemplate) != null) {
			_homepageTemplate = null;
			try {
				File file = org.dbwiki.lib.IO.getFile(properties.getProperty(propertyHomepageTemplate));
				if (file.exists()) {
					_homepageTemplate = file.toURI().toURL();
				}
			} catch (java.io.IOException ioException) {
				_homepageTemplate = null;
			}
		}
		
		_authenticationMode = Integer.parseInt(properties.getProperty(propertyAuthenticationMode));
		
		// Database Connection
		_connector = new DatabaseConnectorFactory().getConnector(properties);
		
		if (properties.getProperty(propertyWikiTitle) != null) {
			_wikiTitle = properties.getProperty(propertyWikiTitle);
		}
		
		_wikiListing = new Vector<DatabaseWiki>();
		
		// Read information about users and DatabassWikis
		// from the database we are connected to
		try {
			Connection con = _connector.getConnection();
			_users = new SQLUserListing(con);	
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + RelationDatabase + " " +
					" WHERE " + RelDatabaseColIsActive + " = " + RelDatabaseColIsActiveValTrue + " " +
					"ORDER BY " + RelDatabaseColTitle);
			while (rs.next()) {
				int id = rs.getInt(RelDatabaseColID);
				String name = rs.getString(RelDatabaseColName);
				String title = rs.getString(RelDatabaseColTitle);
				int layoutVersion = rs.getInt(RelDatabaseColLayout);
				int templateVersion = rs.getInt(RelDatabaseColTemplate);
				int styleSheetVersion = rs.getInt(RelDatabaseColCSS);
				WikiAuthenticator authenticator = new WikiAuthenticator("/" + name, rs.getInt(RelDatabaseColAuthentication), _users);
				int autoSchemaChanges = rs.getInt(RelDatabaseColAutoSchemaChanges);
				ConfigSetting setting = new ConfigSetting(layoutVersion, templateVersion, styleSheetVersion);
				_wikiListing.add(new DatabaseWiki(id, name, title, authenticator, autoSchemaChanges, setting, _connector, this));
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public DatabaseWiki get(int index) {
		return _wikiListing.get(index);
	}
	
	public DatabaseWiki get(String name) {
		for (int iWiki = 0; iWiki < this.size(); iWiki++) {
			if (this.get(iWiki).name().equals(name)) {
				return this.get(iWiki);
			}
		}
		return null;
	}
	
	public String getLayout(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		// Depending on the file version of the requested css-definition-file
		// this method either returns (as a String) the content of the default
		// css-definition-file (in org.dbwiki.server.files.html.style) or it
		// reads the information from the database (-> readConfigFile()).
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return null;
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValLayout, fileVersion);
		}
	}

	public String getTemplate(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		// Depending on the file version of the requested template-file
		// this method either returns (as a String) the content of the default
		// template-file (in org.dbwiki.server.files.html) or it
		// reads the information from the database (-> readConfigFile()).
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return this.readDefaultConfigFile("/html/wiki_template.html");
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValTemplate, fileVersion);
		}
	}
	
	public String getStyleSheet(DatabaseWiki wiki, int fileVersion) throws org.dbwiki.exception.WikiException {
		// Depending on the file version of the requested css-definition-file
		// this method either returns (as a String) the content of the default
		// css-definition-file (in org.dbwiki.server.files.html.style) or it
		// reads the information from the database (-> readConfigFile()).
		if (fileVersion == RelConfigFileColFileVersionValUnknown) {
			return this.readDefaultConfigFile("/html/style/wiki_template.css");
		} else {
			return this.readConfigFile(wiki.id(), RelConfigFileColFileTypeValCSS, fileVersion);
		}
	}
	
	public void handle(HttpExchange exchange) throws java.io.IOException {
		// Implements HttpHandler.handle() which is called by the Web Server
		// for every browser request (Note that requests for the individual
		// wikis are handled by DatabaseWiki.handle()
		try {
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/")) {
				if (_serverLog != null) {
					_serverLog.logRequest(exchange);
				}
				this.respondTo(exchange);
			} else if ((path.startsWith(SpecialFolderDatabaseWikiStyle + "/")) && (path.endsWith(".css"))) {
	    		this.sendCSSFile(path.substring(SpecialFolderDatabaseWikiStyle.length() + 1, path.length() - 4), exchange);
			} else if (path.equals(SpecialFolderLogin)) {
				new RedirectPage(new ServerRequest(this, exchange).parameters().get(RequestParameter.ParameterResource).value()).send(exchange);
	    	// The following code is necessary if using only a single HttpContext
	    	// instead of multiple ones (i.e., one per Database Wiki).
	    	//} else if (path.length() > 1) {
	    	//	int pos = path.indexOf('/', 1);
	    	//	DatabaseWiki wiki = null;
	    	//	if (pos != -1) {
	    	//		wiki = this.get(path.substring(1, pos));
	    	//	} else {
	    	//		wiki = this.get(path.substring(1));
	    	//	}
	    	//	if (wiki != null) {
	    	//		wiki.handle(exchange);
	    	//	} else {
		    //		this.sendFile(exchange);
	    	//	}
    		} else {
	    		this.sendFile(exchange);
	    	}
		} catch (org.dbwiki.exception.WikiException wikiException) {
				new FatalExceptionPage(wikiException).send(exchange);
		} catch (Exception exception) {
			new FatalExceptionPage(exception).send(exchange);
		}
	}

	public synchronized Vector<ConfigSetting> listSettings(DatabaseWiki wiki) throws org.dbwiki.exception.WikiException {
		// The list of all previous display settings for the specified wiki
		try {
			Vector<ConfigSetting> settings = new Vector<ConfigSetting>();
			ConfigSetting currentSetting = new ConfigSetting();
			settings.add(currentSetting);
			Connection con = _connector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT * FROM " + RelationConfigFile + " " +
				"WHERE " + RelConfigFileColWikiID + " = " + wiki.id() + " " +
				"ORDER BY " + RelConfigFileColTime);
			while (rs.next()) {
				ConfigSetting setting = new ConfigSetting(new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new java.util.Date(rs.getLong(RelConfigFileColTime))), currentSetting);
				switch (rs.getInt(RelConfigFileColFileType)) {
				case RelConfigFileColFileTypeValLayout:
					setting.setLayoutVersion(rs.getInt(RelConfigFileColFileVersion));
					break;
				case RelConfigFileColFileTypeValTemplate:
					setting.setTemplateVersion(rs.getInt(RelConfigFileColFileVersion));
					break;
				case RelConfigFileColFileTypeValCSS:
					setting.setStyleSheetVersion(rs.getInt(RelConfigFileColFileVersion));
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

	public synchronized void resetWikiConfiguration(DatabaseWiki wiki) throws org.dbwiki.exception.WikiException {
		// Resets the configuration files of the specified wiki to
		// the default state
		this.resetWikiConfiguration(wiki, RelConfigFileColFileVersionValUnknown, RelConfigFileColFileVersionValUnknown, RelConfigFileColFileVersionValUnknown);
	}
	
	public synchronized void resetWikiConfiguration(DatabaseWiki wiki, int layoutVersion, int templateVersion, int styleSheetVersion) throws org.dbwiki.exception.WikiException {
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
			wiki.reset(layoutVersion, templateVersion, styleSheetVersion);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	public ServerLog serverLog() {
		// The server log used by all the wiki's to log web requests
		return _serverLog;
	}
	
	public int size() {
		return _wikiListing.size();
	}

	public void start() throws java.io.IOException {
		// Starts the web server and creates an individual handler
		// for each of the current wikis.
		_webServer = HttpServer.create(new InetSocketAddress(_port), _backlog);
		_webServer.setExecutor(Executors.newFixedThreadPool(_threadCount));

		HttpContext context = _webServer.createContext("/", this);
		context.setAuthenticator(new WikiAuthenticator("/", _authenticationMode, _users));
		//context.setAuthenticator(new WikiServerAuthenticator("/", _authenticationMode, this, _users));

		// The old code for having a single HttpContext per Database Wiki.
		// Such a scenario, however, does not allow copy/paste between
		// Wikis (as it is currently implemented). Therefore the switch
		// to the single HttpContext.
		for (int iWiki = 0; iWiki < _wikiListing.size(); iWiki++) {
			DatabaseWiki wiki = _wikiListing.get(iWiki);
			context = _webServer.createContext("/" + wiki.name(), wiki);
			context.setAuthenticator(wiki.authenticator());
		}
				
		System.out.println("START SERVER ON ADDRESS " + _webServer.getAddress() + " AT " + new java.util.Date().toString());

		_webServer.start();
	}
	
	public synchronized int updateConfigFile(int wikiID, int fileType, String value, User user) throws org.dbwiki.exception.WikiException {
		// Updates a config file (css, template, or layout as specified by fileType)
		// for the given wiki (wikiID). Returns the version number of the updated
		// file. The method has to store the file content in RelationConfigFile and
		// also update the information in RelationDatabase, i.e., whenever a config
		// file is updated the new version is set to be the default version of this
		// file type for the respective wiki.
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
				"SELECT MAX(" + RelConfigFileColFileVersion + ") " + 
				"FROM " + RelationConfigFile + " " +
				"WHERE " + RelConfigFileColWikiID + " = " + wikiID + " " +
				"AND " + RelConfigFileColFileType + " = " + fileType);
			rs.next();
			// User generated config file versions start from 1
			int version = Math.max(rs.getInt(1) + 1, 1);
			rs.close();
			try {
				PreparedStatement pStmtInsertConfig = con.prepareStatement(
					"INSERT INTO " + RelationConfigFile + " (" +
						RelConfigFileColFileType + ", " +
						RelConfigFileColFileVersion + ", " +
						RelConfigFileColTime + ", " +
						RelConfigFileColUser + ", " +
						RelConfigFileColValue + ", " +
						RelConfigFileColWikiID + ") VALUES(?, ?, ?, ?, ?, ?)");
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
	
	public UserListing users() {
		// The full list of registered wiki users
		return _users;
	}
	
	
	/*
	 * Private Methods
	 */

	private ServerResponseHandler getHomepageResponseHandler(ServerRequest request) {
		ServerResponseHandler responseHandler = new ServerResponseHandler(request, _wikiTitle);
		responseHandler.put(HtmlContentGenerator.ContentMenu, new ServerMenuPrinter(request.server()));
		responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiListingPrinter(request.server()));

		return responseHandler;
	}

	private ServerResponseHandler getInsertWikiResponseHandler(ServerRequest request) throws org.dbwiki.exception.WikiException {
		ServerResponseHandler responseHandler = null;
		
		String name = request.parameters().get(ParameterName).value().toUpperCase();
		String title = request.parameters().get(ParameterTitle).value();
		String authenticationMode = request.parameters().get(ParameterAuthenticationMode).value();
		String autoSchemaChanges = request.parameters().get(ParameterAutoSchemaChanges).value();
		String schema = request.parameters().get(ParameterSchema).value();
		String resource = request.parameters().get(ParameterInputFile).value();
		
		//
		// Validate parameter values.
		//
		int message = DatabaseWikiFormPrinter.MessageNone;
		
		//
		// Validate name
		//
		if (name.equals("")) {
			message = DatabaseWikiFormPrinter.MessageNoName;
		} else if (!this.isValidWikiName(name)) {
			message = DatabaseWikiFormPrinter.MessageInvalidName;
		} else {
			for (int iWiki = 0; iWiki < this.size(); iWiki++) {
				if (this.get(iWiki).name().equalsIgnoreCase(name)) {
					message = DatabaseWikiFormPrinter.MessageDuplicateName;
				}
			}
		}
		
		//
		// Validate title
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (title.equals(""))) {
			message = DatabaseWikiFormPrinter.MessageNoTitle;
		}
		
		//
		// Validate schema
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (!schema.equals(""))) {
			try {
				new SchemaParser().parse(schema);
			} catch (org.dbwiki.exception.WikiException wikiException) {
				wikiException.printStackTrace();
				message = DatabaseWikiFormPrinter.MessageErroneousSchema;
			}
		}
		
		//
		// Validate resource. If no schema is specified then generate schema from
		// given resource and let the user edit/verify the schema.
		//
		if ((message == DatabaseWikiFormPrinter.MessageNone) && (!resource.equals(""))) {
			InputStream in = null;
			try {
				if (resource.endsWith(".gz")) {
					in = new GZIPInputStream(new URL(resource).openStream());
				} else {
					in = new URL(resource).openStream();
				}
			} catch (java.net.MalformedURLException mue) {
				message = DatabaseWikiFormPrinter.MessageFileNotFound;
			} catch (java.io.IOException ioe) {
				message = DatabaseWikiFormPrinter.MessageFileNotFound;
			}
			if ((message == DatabaseWikiFormPrinter.MessageNone) && (schema.equals(""))) {
				try {
					XMLDocumentSchemaParser parser = new XMLDocumentSchemaParser(in);
					schema = parser.getSchema();
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
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Create Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(RequestParameterAction.ActionInsert, name, title, authenticationMode, autoSchemaChanges, schema, resource, message));
			return responseHandler;
		} else {
			//
			// If the parameter values are valid the database wiki is created
			//
			if ((request.user() == null) && (_authenticationMode != WikiAuthenticator.AuthenticateNever)) {
				throw new WikiFatalException("User information is missing");
			}
			
			WikiAuthenticator authenticator = null;
			DatabaseWiki wiki = null;

			Connection con = _connector.getConnection();
			int wikiID = -1;
			try {
				DatabaseSchema databaseSchema = new SchemaParser().parse(schema);
				con.setAutoCommit(false);
				try {
					if (!schema.equals("")) {
						_connector.createDatabase(con, name, databaseSchema, request.user());
					} else {
						_connector.createDatabase(con, name, request.user());
					}
					PreparedStatement pStmt = con.prepareStatement(
						"INSERT INTO " + RelationDatabase + "(" +
						RelDatabaseColName + ", " + RelDatabaseColTitle + ", " + 
						RelDatabaseColAuthentication + ", " + RelDatabaseColAutoSchemaChanges + ", " + RelDatabaseColUser + ") VALUES(? , ? , ?, ? , ?)", Statement.RETURN_GENERATED_KEYS);
					pStmt.setString(1, name);
					pStmt.setString(2, title);
					pStmt.setInt(3, Integer.parseInt(authenticationMode));
					pStmt.setInt(4, Integer.parseInt(autoSchemaChanges));
					if (request.user() != null) {
						pStmt.setInt(5, request.user().id());
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
				} catch (java.sql.SQLException sqlException) {
					con.rollback();
					con.close();
					throw new WikiFatalException(sqlException);
				}
				
				con.commit();
				
				authenticator = new WikiAuthenticator("/" + name, Integer.parseInt(authenticationMode), _users);
				wiki = new DatabaseWiki(wikiID, name, title, authenticator, Byte.parseByte(autoSchemaChanges), _connector, this);
				String realm = wiki.database().identifier().databaseHomepage();
				HttpContext context = _webServer.createContext(realm, wiki);
				context.setAuthenticator(authenticator);
				_wikiListing.add(wiki);
				Collections.sort(_wikiListing);

				//
				// Import data into created database wiki if the user specified an import resource.
				//
				if (!resource.equals("")) {
					RDBMSDatabase database = (RDBMSDatabase)wiki.database();
					XMLDocumentImportReader reader = new XMLDocumentImportReader(resource, database.schema(), ((ImportEntity)databaseSchema.root()).targetPath(), request.user(), false, false);
					DatabaseImportHandler importHandler = new DatabaseImportHandler(con, database);
					reader.setImportHandler(importHandler);
					reader.start();
				}

				con.close();
				
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
			
			return this.getHomepageResponseHandler(request);
		}
	}

	private DatabaseWiki getRequestWiki(ServerRequest request, String key) throws org.dbwiki.exception.WikiException {
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
	
	private ServerResponseHandler getUpdateWikiResponseHandler(ServerRequest request) throws org.dbwiki.exception.WikiException {
		ServerResponseHandler responseHandler = null;
		
		DatabaseWiki wiki = this.getRequestWiki(request, ParameterName);
		
		String title = request.parameters().get(ParameterTitle).value();
		String authenticationMode = request.parameters().get(ParameterAuthenticationMode).value();
		String autoSchemaChanges = request.parameters().get(ParameterAutoSchemaChanges).value();
		
		int message = DatabaseWikiFormPrinter.MessageNone;
		if (title.equals("")) {
			message = DatabaseWikiFormPrinter.MessageNoTitle;
		}
		if (message != DatabaseWikiFormPrinter.MessageNone) {
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Edit Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(RequestParameterAction.ActionUpdate, wiki.name(), title, authenticationMode, autoSchemaChanges, message));
			return responseHandler;
		} else {
			if ((request.user() == null) && (_authenticationMode != WikiAuthenticator.AuthenticateNever)) {
				throw new WikiFatalException("User information is missing");
			}
			try {
				Connection con = _connector.getConnection();
				PreparedStatement pStmt = con.prepareStatement("UPDATE " + RelationDatabase + " " + 
					"SET " + RelDatabaseColTitle + " = ?, " +
					RelDatabaseColAuthentication + " = ?, " +
					RelDatabaseColAutoSchemaChanges + " = ? " +
					"WHERE " + RelDatabaseColID + " = " + wiki.id());
				pStmt.setString(1, title);
				pStmt.setInt(2, Integer.parseInt(authenticationMode));
				pStmt.setInt(3, Integer.parseInt(autoSchemaChanges));
				pStmt.execute();
				pStmt.close();
				con.close();
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
			wiki.authenticator().setAuthenticationMode(Integer.parseInt(authenticationMode));
			wiki.setAutoSchemaChanges(Integer.parseInt(autoSchemaChanges));
			wiki.setTitle(title);
			Collections.sort(_wikiListing);
			return this.getHomepageResponseHandler(request);
		}
	}

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
	
	private String readConfigFile(int wikiID, int fileType, int fileVersion) throws org.dbwiki.exception.WikiException {
		String value = null;
		try {
			Connection con = _connector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + RelConfigFileColValue + " " +
				"FROM " + RelationConfigFile + " " +
				"WHERE " + RelConfigFileColWikiID + " = " + wikiID + " " +
				" AND " + RelConfigFileColFileType + " = " + fileType + " " +
				"AND " + RelConfigFileColFileVersion + " = " + fileVersion);
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

	private void respondTo(HttpExchange exchange) throws java.io.IOException, org.dbwiki.exception.WikiException {
		ServerResponseHandler responseHandler = null;
		
		ServerRequest request = new ServerRequest(this, exchange);

		if (request.type().isIndex()) {
			responseHandler = this.getHomepageResponseHandler(request);
		} else if (request.type().isCreate()) {
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Create Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter());
		} else if (request.type().isEdit()) {
			DatabaseWiki wiki = this.getRequestWiki(request, RequestParameter.ParameterEdit);
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Edit Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(RequestParameterAction.ActionUpdate, wiki.name(), wiki.getTitle(), wiki.authenticator().getAuthenticationMode(), wiki.getAutoSchemaChanges()));
		} else if (request.type().isReset()) {
			this.resetWikiConfiguration(this.getRequestWiki(request, RequestParameter.ParameterReset));
			responseHandler = this.getHomepageResponseHandler(request);
		} else if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			if (action.actionInsert()) {
				responseHandler = this.getInsertWikiResponseHandler(request);
			} else if (action.actionCancel()) {
				responseHandler = this.getHomepageResponseHandler(request);
			} else if (action.actionUpdate()) {
				responseHandler = this.getUpdateWikiResponseHandler(request);
			} else {
				throw new WikiRequestException(WikiRequestException.InvalidRequest, request.toString());
			}
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidRequest, request.toString());
		}
		
		URL template = null;

		//
		// TODO: Improve handling of individual home pages.
		//
		// This part is still a bit tricky. In order to identify whether the response handler results from
		// a call to getHomepageResponseHandler() we rely on the fact that only getHomepageResponseHandler()
		// adds content handler for Menu and Content.
		//
		if (responseHandler.contains(HtmlContentGenerator.ContentMenu) && responseHandler.contains(HtmlContentGenerator.ContentContent)) {
			if (_homepageTemplate != null) {
				template = _homepageTemplate;
			} else {
				template = new File(directory().getAbsolutePath() + "/html/server-main.html").toURI().toURL(); 
			}
		} else {
			if (_formTemplate != null) {
				template = _formTemplate;
			} else {
				template = new File(directory().getAbsolutePath() + "/html/server.html").toURI().toURL();
			}
		}
		new HtmlTemplateDecorator().decorate(template, responseHandler).send(exchange);
	}
	
	private void sendCSSFile(String name, HttpExchange exchange) throws java.io.IOException, org.dbwiki.exception.WikiException {
		int wikiID = -1;
		int fileVersion = -1;
		
		try {
			int pos = name.indexOf("_");
			wikiID = Integer.valueOf(name.substring(0, pos));
			fileVersion = Integer.valueOf(name.substring(pos + 1));
		} catch (Exception exception ) {
			new FileNotFoundPage(exchange.getRequestURI().getPath()).send(exchange);
			return;
		}
		
		String value = this.readConfigFile(wikiID, RelConfigFileColFileTypeValCSS, fileVersion);
		this.sendData(exchange, "text/css", new ByteArrayInputStream(value.getBytes("UTF-8")));
	}
}
