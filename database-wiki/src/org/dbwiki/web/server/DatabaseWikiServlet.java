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

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.data.wiki.SimpleWiki;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.security.WikiServletAuthenticator;
import org.dbwiki.web.ui.DatabaseWikiContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;

/**
 * Implements a basic database wiki with a servlet interface
 * 
 * @author o.cierny
 * 
 */
public class DatabaseWikiServlet extends DatabaseWiki {
	
	private WikiServletAuthenticator _authenticator;
	protected WikiServerServlet _server;

	/**
	 * Create new DatabaseWiki from given data. Used in
	 * WikiServer.getWikiListing.
	 * 
	 */
	// FIXME: Factor out common stuff in DatabaseWiki constructor
	public DatabaseWikiServlet(int id, String name, String title,
			int authenticationMode, int autoSchemaChanges, UserListing _users,  
			ConfigSetting setting, DatabaseConnector connector,
			WikiServerServlet server)
			throws org.dbwiki.exception.WikiException {
		
		_policy = new SimplePolicy(authenticationMode,_policy);
		_authenticator = new WikiServletAuthenticator( "/"+name, _users, this,_policy);
		_id = id;
		_server = server;
		_name = name;
		_title = title;

		reset(setting.getLayoutVersion(), setting.getTemplateVersion(),
				setting.getStyleSheetVersion(),
				setting.getURLDecodingRulesVersion());

		_database = new RDBMSDatabase(this, connector);
		_database.validate();
		_wiki = new SimpleWiki(name, connector, server.users());
		initializePolicy();
		
	}

	// HACK: pass in and use an existing connection and version index.
	// Used only in WikiServer.RegisterDatabase to create a new database.
	// FIXME: Factor out common stuff in DatabaseWiki constructor
	public DatabaseWikiServlet(int id, String name, String title,
			int authenticationMode, int autoSchemaChanges, UserListing _users, 
			DatabaseConnector connector, WikiServerServlet server,
			Connection con, SQLVersionIndex versionIndex)
			throws org.dbwiki.exception.WikiException {
		
		_policy = new SimplePolicy(authenticationMode,_policy);
		_authenticator = new WikiServletAuthenticator( "/"+name, _users, this,_policy);
		_autoSchemaChanges = autoSchemaChanges;
		_id = id;
		_server = server;
		_name = name;
		_title = title;

		ConfigSetting setting = new ConfigSetting();

		reset(setting.getLayoutVersion(), setting.getTemplateVersion(),
				setting.getStyleSheetVersion(),
				setting.getURLDecodingRulesVersion());

		_database = new RDBMSDatabase(this, connector, con, versionIndex);
		_wiki = new SimpleWiki(name, connector, server.users());
		initializePolicy();
		
	}

	/*
	 * Getters
	 */

	@Override
	public WikiServer server() {
		return _server;
	}
	
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		Exchange<HttpServletRequest> exchange = new ServletExchangeWrapper(request,response);
		try {
			String filename = exchange.getRequestURI().toString();
			if(_authenticator.authenticate(request,response)) {
				int pos = filename.lastIndexOf('.');
				if (pos != -1) {
					_server.sendFile(exchange);
				} else {
					if (_server.serverLog() != null) {
						_server.serverLog().logRequest(request);
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
			} else {
				response.setHeader("WWW-Authenticate", "Basic realm=\"" + filename + "\"");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
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
	

	

}
