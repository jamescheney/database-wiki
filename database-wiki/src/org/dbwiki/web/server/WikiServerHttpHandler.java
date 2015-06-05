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

package org.dbwiki.web.server;

import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.io.ImportHandler;
import org.dbwiki.data.io.XMLDocumentImportReader;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.driver.rdbms.DatabaseImportHandler;
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;
import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.RedirectPage;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.security.WikiAuthenticator;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction") 
public class WikiServerHttpHandler extends WikiServer implements HttpHandler {



	private Vector<DatabaseWikiHttpHandler> _wikiListing;
	protected HttpServer _webServer = null;
	protected int _port;
	protected int _threadCount;
	protected int _backlog;
	/*
	 * Constructors
	 */
	
	
	public WikiServerHttpHandler(Properties properties) throws WikiException {
		super(properties);
		// Web Server Properties
		_backlog = Integer.parseInt(properties.getProperty(propertyBacklog));
		_port = Integer.parseInt(properties.getProperty(propertyPort));
		_threadCount = Integer.parseInt(properties.getProperty(propertyThreadCount));
	}
	
	
	/** 
	 * Initialize list of DatabaseWikis from database
	 * @param con
	 * @throws SQLException
	 * @throws WikiException
	 */
	protected void getWikiListing (Connection con) throws SQLException, WikiException {
		_wikiListing = new Vector<DatabaseWikiHttpHandler>();
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
			int urlDecodingVersion = RelConfigFileColFileVersionValUnknown;
			if (org.dbwiki.lib.JDBC.hasColumn(rs, RelDatabaseColURLDecoding)) {
				urlDecodingVersion = rs.getInt(RelDatabaseColURLDecoding);
			}
			SimplePolicy policy = new SimplePolicy(rs.getInt(RelDatabaseColAuthentication));
			WikiAuthenticator authenticator = new WikiAuthenticator("/" + name,  _users, _formTemplate,this, policy);
			int autoSchemaChanges = rs.getInt(RelDatabaseColAutoSchemaChanges);
			ConfigSetting setting = new ConfigSetting(layoutVersion, templateVersion, styleSheetVersion, urlDecodingVersion);
			_wikiListing.add(new DatabaseWikiHttpHandler(id, name, title, authenticator, autoSchemaChanges, setting, _connector, this));
		}
		rs.close();
		stmt.close();
	}
	
	/*
	 * Getters
	 */
	
	/** Get the DatabaseWiki with index i */
	public DatabaseWiki get(int index) {
		return _wikiListing.get(index);
	}
	
	/**
	 * 
	 * @return The number of DatabaseWikis
	 */
	public int size() {
		return _wikiListing.size();
	}

	
	
	/** Starts the web server and creates an individual handler
	  *	 for each of the current wikis.
	  * 
	  * @throws java.io.IOException
	  */
	
	
	public void start() throws java.io.IOException {
		_webServer = HttpServer.create(new InetSocketAddress(_port), _backlog);
		_webServer.setExecutor(Executors.newFixedThreadPool(_threadCount));

		HttpContext context = _webServer.createContext("/", this);
		context.setAuthenticator(new WikiAuthenticator("/", _users, _formTemplate,this,_policy));

		for (int iWiki = 0; iWiki < _wikiListing.size(); iWiki++) {
			DatabaseWikiHttpHandler wiki = _wikiListing.get(iWiki);
			context = _webServer.createContext("/" + wiki.name(), wiki);
			context.setAuthenticator(wiki.authenticator());
		}
				
		System.out.println("START SERVER ON ADDRESS " + _webServer.getAddress() + " AT " + new java.util.Date().toString());

		_webServer.start();
	}
	
	
	/** Creates new database with a given schema and import given data into it
	 * TODO #import Move this into a separate class, to factor out common functionality with DatabaseImport
	 * TODO #server Split this into server-level and data-level stuff. 
	 * @param name - string identifying database tables
	 * @param title - human readable title
	 * @param path - path to entries in the document
	 * @param resource
	 * @param databaseSchema
	 * @param user
	 * @param authenticationMode
	 * @param autoSchemaChanges
	 * @throws NumberFormatException
	 * @throws WikiException
	 * @throws SQLException
	 */
	public void registerDatabase(String name, String title, String path, URL resource, DatabaseSchema databaseSchema, User user, int authenticationMode, int autoSchemaChanges)
		throws NumberFormatException, WikiException, SQLException {
		
		
		Connection con = _connector.getConnection();
		int wikiID = -1;
		SQLVersionIndex versionIndex = new SQLVersionIndex(con, name, users(), true);
		CreateCollectionRecord r = new CreateCollectionRecord(name,title,authenticationMode,autoSchemaChanges,databaseSchema,user);
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		try {
			
			wikiID = r.createCollection(con, versionIndex);
			con.commit();
			SimplePolicy policy = new SimplePolicy(authenticationMode);
			WikiAuthenticator authenticator = new WikiAuthenticator("/" + name,  _users, _formTemplate,this,policy);
			DatabaseWikiHttpHandler wiki = new DatabaseWikiHttpHandler(wikiID, name, title, authenticator, autoSchemaChanges, _connector, this,
									con, versionIndex);

			// this should now only be called when starting a web server

			String realm = wiki.database().identifier().databaseHomepage();
			HttpContext context = _webServer.createContext(realm, wiki);
			context.setAuthenticator(authenticator);
			
			_wikiListing.add(wiki);
			Collections.sort(_wikiListing);
			//
			// Import data into created database wiki if the user specified an import resource.
			//
			if (resource != null) {
				Database database = wiki.database();
				// Note that database.schema() is a copy of databaseSchema that has been read back from the database
				// after being loaded in when we created new database above.
				// We should really deal with the target path separately, e.g. via extra text field
				XMLDocumentImportReader reader = new XMLDocumentImportReader(resource, 
														database.schema(),
														path, user, false, false);
				ImportHandler importHandler = new DatabaseImportHandler(con,database);
				reader.setImportHandler(importHandler);
				reader.start();
			}
		} catch (java.sql.SQLException sqlException) {
			con.rollback();
			con.close();
			throw new WikiFatalException(sqlException);
		}
		con.commit();
		con.close();
	}
	
	public void sortWikiListing() {
		Collections.sort(_wikiListing);
	}
	
	/** Implements HttpHandler.handle() which is called by the Web Server
	 * for every browser request (Note that requests for the individual
	 * wikis are handled by DatabaseWiki.handle())
	 * The HttpExchange is side-effected and the response part is eventually sent back to the client.
	 */
	public void handle(HttpExchange httpExchange) throws java.io.IOException {
		Exchange<HttpExchange> exchange = new HttpExchangeWrapper(httpExchange);
		try {
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/")) {
				if (_serverLog != null) {
					_serverLog.logRequest(exchange.getRequestURI(),exchange.get().getRemoteAddress(),exchange.get().getResponseHeaders());
				}
				this.respondTo(exchange);
			} else if ((path.startsWith(SpecialFolderDatabaseWikiStyle + "/")) && (path.endsWith(".css"))) {
	    		this.sendCSSFile(path.substring(SpecialFolderDatabaseWikiStyle.length() + 1, path.length() - 4), exchange);
			} else if (path.equals(SpecialFolderLogin)) {
				//FIXME: #request This is a convoluted way of parsing the request parameter!
				exchange.send(new RedirectPage(new RequestURL( exchange,"").parameters().get(RequestParameter.ParameterResource).value()));
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
		} catch (Exception exception) {
			exchange.send(new FatalExceptionPage(exception));
		}
	}
	
	

	

			
}
