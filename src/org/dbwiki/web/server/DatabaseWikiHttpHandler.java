package org.dbwiki.web.server;

import java.net.URLEncoder;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dbwiki.data.annotation.Annotation;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.database.Update;
import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.io.CopyPasteNodeWriter;
import org.dbwiki.data.io.ExportJSONNodeWriter;
import org.dbwiki.data.io.ExportNodeWriter;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.wiki.SimpleWiki;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.web.WikiRequestException;
import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.html.RedirectPage;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.ui.DatabaseWikiContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.printer.FileEditor;
import org.dbwiki.web.ui.printer.LayoutEditor;
import org.dbwiki.web.ui.printer.ObjectAnnotationPrinter;
import org.dbwiki.web.ui.printer.ObjectProvenancePrinter;
import org.dbwiki.web.ui.printer.SettingsListingPrinter;
import org.dbwiki.web.ui.printer.TimemachinePrinter;
import org.dbwiki.web.ui.printer.VersionIndexPrinter;
import org.dbwiki.web.ui.printer.data.CreateSchemaNodeFormPrinter;
import org.dbwiki.web.ui.printer.data.DataMenuPrinter;
import org.dbwiki.web.ui.printer.data.DataNodePrinter;
import org.dbwiki.web.ui.printer.data.DataUpdateFormPrinter;
import org.dbwiki.web.ui.printer.data.InputFormPrinter;
import org.dbwiki.web.ui.printer.data.NodePathPrinter;
import org.dbwiki.web.ui.printer.index.AZMultiPageIndexPrinter;
import org.dbwiki.web.ui.printer.index.AZSinglePageIndexPrinter;
import org.dbwiki.web.ui.printer.index.FullIndexPrinter;
import org.dbwiki.web.ui.printer.index.MultiColumnIndexPrinter;
import org.dbwiki.web.ui.printer.index.PartialIndexPrinter;
import org.dbwiki.web.ui.printer.index.SearchResultPrinter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Implements the sun http server interface to a Database Wiki
 * 
 * @author jcheney
 * 
 */
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
			WikiAuthenticator authenticator, int autoSchemaChanges,
			ConfigSetting setting, DatabaseConnector connector,
			WikiServerHttpHandler server)
			throws org.dbwiki.exception.WikiException {
		_authenticator = authenticator;
		_autoSchemaChanges = autoSchemaChanges;
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
	}

	// HACK: pass in and use an existing connection and version index.
	// Used only in WikiServer.RegisterDatabase to create a new database.
	public DatabaseWikiHttpHandler(int id, String name, String title,
			WikiAuthenticator authenticator, int autoSchemaChanges,
			DatabaseConnector connector, WikiServerHttpHandler server,
			Connection con, SQLVersionIndex versionIndex)
			throws org.dbwiki.exception.WikiException {
		_authenticator = authenticator;
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
	}

	/*
	 * Getters
	 */

	public WikiAuthenticator authenticator() {
		return _authenticator;
	}

	@Override
	public int getAuthenticationMode() {
		return _authenticator.getAuthenticationMode();
	}

	@Override
	public void setAuthenticationMode(int authMode) {
		super.setAuthenticationMode(authMode);
		_authenticator.setAuthenticationMode(authMode);
	}

	/*
	 * Actions
	 */

	/**
	 * Dispatches HTTP interactions based on the type of the request. Data
	 * requests are handled by respondToDataRequest Wiki Page requests are
	 * handled by respondToPageRequest Schema requests are handled by
	 * respondToSchemaRequest
	 */
	public void handle(HttpExchange httpExchange) throws java.io.IOException {
		Exchange<HttpExchange> exchange = new HttpExchangeWrapper(httpExchange);
		try {
			String filename = exchange.getRequestURI().getPath();
			int pos = filename.lastIndexOf('.');
			if (pos != -1) {
				_server.sendFile(exchange);
			} else {
				if (_server.serverLog() != null) {
					_server.serverLog().logRequest(exchange.getRequestURI(),
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
