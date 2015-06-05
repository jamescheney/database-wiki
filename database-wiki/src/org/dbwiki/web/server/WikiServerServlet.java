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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.security.WikiServletAuthenticator;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.ServerResponseHandler;
import org.dbwiki.web.ui.printer.server.DatabaseAccessDeniedPrinter;

/**
 * Root WikiServer class with a servlet interface
 * 
 * @author o.cierny
 * 
 */

public class WikiServerServlet extends WikiServer {

	private Vector<DatabaseWikiServlet> _wikiListing;
	private WikiServletAuthenticator _authenticator;
	
	/*
	 * Constructors
	 */
	
	public WikiServerServlet(String prefix, Properties properties) throws org.dbwiki.exception.WikiException {
		super(prefix, properties);
		_authenticator = new WikiServletAuthenticator( "/", _users,this, _policy);
	}
	
	/** 
	 * Initialise a list of DatabaseWikis from database
	 * @param con
	 * @throws SQLException
	 * @throws WikiException
	 */
	protected void getWikiListing (Connection con) throws SQLException, WikiException {
		_wikiListing = new Vector<DatabaseWikiServlet>();
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
			int autoSchemaChanges = rs.getInt(RelDatabaseColAutoSchemaChanges);
			ConfigSetting setting = new ConfigSetting(layoutVersion, templateVersion, styleSheetVersion, urlDecodingVersion);
			SimplePolicy policy = new SimplePolicy(rs.getInt(RelDatabaseColAuthentication));
			WikiServletAuthenticator authenticator = new WikiServletAuthenticator( "/"+name, _users, this,policy);
			_wikiListing.add(new DatabaseWikiServlet(id, name, title, autoSchemaChanges, authenticator, setting, _connector, this));
		}
		rs.close();
		stmt.close();
	}
	
	/*
	 * Getters
	 */
	
	/**
	 * @return The DatabaseWiki with index i
	 */
	public DatabaseWikiServlet get(int index) {
		return _wikiListing.get(index);
	}
	
	/** Get the DatabaseWiki with string id @name */
	public DatabaseWikiServlet get(String name) {
		for (int iWiki = 0; iWiki < this.size(); iWiki++) {
			if (this.get(iWiki).name().equals(name)) {
				return this.get(iWiki);
			}
		}
		return null;
	}
	
	/**
	 * @return The number of DatabaseWikis
	 */
	public int size() {
		return _wikiListing.size();
	}
	
	/** 
	 * Creates new database with a given schema and import given data into it
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
		CreateCollectionRecord r = new CreateCollectionRecord(name, title, authenticationMode, autoSchemaChanges, databaseSchema, user);
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		try {
			
			wikiID = r.createCollection(con, versionIndex);
			con.commit();
			SimplePolicy policy = new SimplePolicy(authenticationMode);
			WikiServletAuthenticator authenticator = new WikiServletAuthenticator( "/" + name, _users,this, policy);
			DatabaseWikiServlet wiki = new DatabaseWikiServlet(wikiID, name, title, autoSchemaChanges, authenticator, _connector, this,
									con, versionIndex);
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
	
	/** 
	 * Servlet-tuned request handler
	 * @param request
	 * @param response
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		Exchange<HttpServletRequest> exchange = new ServletExchangeWrapper(request, response);
		try {
			String path = request.getRequestURI();
			if (path.equals("/")) {
				if (_serverLog != null) {
					_serverLog.logRequest(request);
				}
				if(_authenticator.authenticate(request)) {
					this.respondTo(exchange);
				} else {
					// OLD
					//response.setHeader("WWW-Authenticate", "Basic realm=\"/\"");
					//response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
					ServerResponseHandler responseHandler = new ServerResponseHandler(new HttpRequest(new RequestURL(exchange, ""), users()), "Access Denied");
					responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseAccessDeniedPrinter());
					exchange.send(HtmlTemplateDecorator.decorate(new BufferedReader(new FileReader(_formTemplate)), responseHandler));
				}
			} else if ((path.startsWith(SpecialFolderDatabaseWikiStyle + "/")) && (path.endsWith(".css"))) {
				this.sendCSSFile(path.substring(SpecialFolderDatabaseWikiStyle.length() + 1, path.length() - 4), exchange);
			} else if (path.equals(SpecialFolderLogin)) {
				//FIXME: #request This is a convoluted way of parsing the request parameter!
				if(_authenticator.authenticate(request)) {
					exchange.send(new RedirectPage(new RequestURL(exchange,"").parameters().get(RequestParameter.ParameterResource).value()));
				} else {
					response.setHeader("WWW-Authenticate", "Basic realm=\"/login\"");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
				}
			} else if (path.length() > 1) {
				int pos = path.indexOf('/', 1);
				DatabaseWikiServlet wiki = null;
				if (pos != -1) {
					wiki = this.get(path.substring(1, pos));
				} else {
					wiki = this.get(path.substring(1));
				}
				if (wiki != null) {
					wiki.handle(request, response);
				} else {
					this.sendFile(exchange);
				}
			} else {
				this.sendFile(exchange);
			}
		} catch (Exception exception) {
			try {
				exchange.send(new FatalExceptionPage(exception));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

		
	

}
