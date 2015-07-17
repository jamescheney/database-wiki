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

import java.io.File;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.SQLDatabaseSchema;
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.ui.DatabaseWikiContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Implements the sun http server interface to a Database Wiki
 * 
 * @author jcheney
 * 
 */
@SuppressWarnings("restriction")
public class DatabaseWikiHttpHandler extends DatabaseWiki implements
		HttpHandler {

	protected WikiAuthenticator _authenticator;
	protected WikiServerHttpHandler _server;

	/**
	 * Create new DatabaseWiki from given data. Used in
	 * WikiServer.getWikiListing.
	 * 
	 */
	public DatabaseWikiHttpHandler(int id, String name, String title,
			int authenticationMode, int autoSchemaChanges,
			DatabaseConnector connector, ConfigSetting setting, 
			File _formTemplate, 
			WikiServerHttpHandler server)
			throws org.dbwiki.exception.WikiException {
		super(id, name, title, authenticationMode, autoSchemaChanges, connector);
		
		_server = server;
		_authenticator = new WikiAuthenticator("/" + name,  _server.users(), _formTemplate,_rolePolicy);
		
		initialize(setting, server);
		
		_database = new RDBMSDatabase(this, connector);
		_database.validate();
		
	}

	

	public DatabaseWikiHttpHandler(int id, String name, String title,
			int authenticationMode, int autoSchemaChanges,
			DatabaseConnector connector, 
			File _formTemplate, 
			WikiServerHttpHandler server,
			SQLDatabaseSchema schema, SQLVersionIndex versionIndex)
			throws org.dbwiki.exception.WikiException {
		super(id,name,title,authenticationMode,autoSchemaChanges,connector);
		
		_server = server;
		_authenticator = new WikiAuthenticator("/" + name,  _server.users(), _formTemplate,_rolePolicy);

		initialize(new ConfigSetting(), server);
		
		_database = new RDBMSDatabase(this, connector, schema, versionIndex);
		
	}
	/*
	 * Getters
	 */
	
	public WikiAuthenticator authenticator() {
		return _authenticator;
	}

	

	/*
	 * Actions
	 */

	/**
	 * Dispatches HTTP interactions based on the type of the request. 
	 * - Data requests are handled by respondToDataRequest 
	 * - Wiki Page requests are handled by respondToPageRequest 
	 * - Schema requests are handled by respondToSchemaRequest
	 */
	public void handle(HttpExchange httpExchange) throws java.io.IOException {
		Exchange<HttpExchange> exchange = new HttpExchangeWrapper(httpExchange);
		try {
			String filename = exchange.getRequestURI().getPath();
			int pos = filename.lastIndexOf('.');
			if (pos != -1) {
				server().sendFile(exchange);
			} else {
				if (server().serverLog() != null) {
					server().serverLog().logRequest(exchange.getRequestURI(),
							exchange.get().getRemoteAddress(),
							exchange.get().getResponseHeaders());
				}
				RequestURL url = new RequestURL(exchange, _database.identifier().linkPrefix());
				if (url.isDataRequest()) {
					respondToDataRequest(new WikiDataRequest(this, url),
							exchange);
				} else if (url.isPageRequest()) {
					respondToPageRequest(new WikiPageRequest(this, url),
							exchange);
				} else if (url.isSchemaRequest()) {
					respondToSchemaRequest(new WikiSchemaRequest(this, url),
							exchange);
				}
			}
		} catch (org.dbwiki.exception.WikiException wikiException) {
			wikiException.printStackTrace();
			try {
				exchange.send(HtmlTemplateDecorator.decorate(_template,
						new DatabaseWikiContentGenerator(this.identifier(),
								this.getTitle(), this.cssLinePrinter(),
								wikiException)));
			} catch (org.dbwiki.exception.WikiException exception) {
				exchange.send(new FatalExceptionPage(exception));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			exchange.send(new FatalExceptionPage(exception));
		}
	}
	
	

	@Override
	public WikiServer server() {
		return _server;
	}



}
