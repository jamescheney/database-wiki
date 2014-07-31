package org.dbwiki.web.server;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringWriter;
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
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.resource.PageIdentifier;
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
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiSchemaRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.request.parameter.RequestParameterActionCancel;
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
import org.dbwiki.web.ui.printer.page.PageContentPrinter;
import org.dbwiki.web.ui.printer.page.PageHistoryPrinter;
import org.dbwiki.web.ui.printer.page.PageMenuPrinter;
import org.dbwiki.web.ui.printer.page.PageUpdateFormPrinter;
import org.dbwiki.web.ui.printer.schema.SchemaMenuPrinter;
import org.dbwiki.web.ui.printer.schema.SchemaNodePrinter;
import org.dbwiki.web.ui.printer.schema.SchemaPathPrinter;

import com.sun.net.httpserver.Headers;
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
	public void handle(HttpExchange exchange) throws java.io.IOException {
		try {
			String filename = exchange.getRequestURI().getPath();
			int pos = filename.lastIndexOf('.');
			if (pos != -1) {
				_server.sendFile(exchange);
			} else {
				if (_server.serverLog() != null) {
					_server.serverLog().logRequest(exchange.getRequestURI(),
							exchange.getRemoteAddress(),
							exchange.getResponseHeaders());
				}
				RequestURL url = new RequestURL(new HttpExchangeWrapper(
						exchange), _database.identifier().linkPrefix());
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
				HtmlSender.send(HtmlTemplateDecorator.decorate(_template,
						new DatabaseWikiContentGenerator(this.identifier(),
								this.getTitle(), this.cssLinePrinter(),
								wikiException)), exchange);
			} catch (org.dbwiki.exception.WikiException exception) {
				HtmlSender.send(new FatalExceptionPage(exception), exchange);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			HtmlSender.send(new FatalExceptionPage(exception), exchange);
		}
	}
	
	// TODO: Merge XML and JSON export to avoid code duplication

	/**
	 * Handles data export requests (generating XML)
	 * 
	 * @param request
	 * @param writer
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToExportXMLRequest(WikiDataRequest request,
			NodeWriter writer, HttpExchange exchange)
			throws org.dbwiki.exception.WikiException {
		int versionNumber = database().versionIndex().getLastVersion().number();
		if (request.parameters()
				.hasParameter(RequestParameter.ParameterVersion)) {
			versionNumber = ((RequestParameterVersionSingle) RequestParameter
					.versionParameter(request.parameters().get(
							RequestParameter.ParameterVersion)))
					.versionNumber();
		}

		try {
			// if the request is for all the data in a DatabaseWiki, create a
			// temporary file.
			// Otherwise, do it in memory (ASSUMES each entry is small!).
			// TODO: This could probably be done uniformly by streaming instead.
			if (request.isRootRequest()) {
				File tmpFile = File.createTempFile("dbwiki", "xml");
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				_server.sendXML(exchange, new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				_server.sendXML(exchange, new ByteArrayInputStream(buf
						.toString().getBytes("UTF-8")));
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	/**
	 * Handles data export requests (generating JSON)
	 * 
	 * @param request
	 * @param writer
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToExportJSONRequest(WikiDataRequest request,
			NodeWriter writer, HttpExchange exchange)
			throws org.dbwiki.exception.WikiException {
		int versionNumber = database().versionIndex().getLastVersion().number();
		if (request.parameters()
				.hasParameter(RequestParameter.ParameterVersion)) {
			versionNumber = ((RequestParameterVersionSingle) RequestParameter
					.versionParameter(request.parameters().get(
							RequestParameter.ParameterVersion)))
					.versionNumber();
		}

		try {
			// if the request is for all the data in a DatabaseWiki, create a
			// temporary file.
			// Otherwise, do it in memory (ASSUMES each entry is small!).
			// TODO: This could probably be done uniformly by streaming instead.
			if (request.isRootRequest()) {
				File tmpFile = File.createTempFile("dbwiki", "json");
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				_server.sendJSON(exchange, new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				_server.sendJSON(exchange, new ByteArrayInputStream(buf
						.toString().getBytes("UTF-8")));
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	/**
	 * Handles a request for a data node. First check authentication. Next
	 * determine whether it's a GET request, or an action. If it's an action,
	 * perform the action and redirect. If it's a GET request, there are many
	 * cases depending on the particular content being requested (e.g. the
	 * current version, past versions, xml, search, file editor, etc.) In each
	 * case, build appropriate content generator and plug into the template.
	 * 
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToDataRequest(WikiDataRequest request,
			HttpExchange exchange) throws java.io.IOException,
			org.dbwiki.exception.WikiException {
		HtmlPage page = null;

		// The following test is just an additional security check in case
		// someone
		// managed to get past the WikiAuthenticator.
		if ((request.user() == null)
				&& (getAuthenticationMode() != DatabaseWikiProperties.AuthenticateNever)) {
			if ((request.type().isAction()) || (request.type().isActivate())
					|| (request.type().isDelete()) || request.type().isPaste()) {
				throw new WikiFatalException(
						"User login required to perform requested operation");
			}
		}

		// All requests of type .isAction() have to result in .isGet() or
		// .isIndex() requests
		// which is not recognized/distinguished by the RequestType class. Thus,
		// this
		// decision has to be taken below (thats why the following variables are
		// needed).
		boolean isGetRequest = (request.type().isGet() || request.type()
				.isActivate());
		boolean isIndexRequest = request.type().isIndex();

		// This is where all the action happens. Note that .isAction() requests
		// result from
		// HTTP POST request. The class RequestType currently does not
		// distinguish these
		// requests further, thus it has to be done here.
		if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter
					.actionParameter(request.parameters().get(
							RequestParameter.ParameterAction));
			if (action.actionAnnotate()) {
				RequestParameter parameter = request.parameters().get(
						RequestParameter.ActionValueAnnotation);
				if (parameter.hasValue()) {
					if (!parameter.value().trim().equals("")) {
						database().annotate(
								request.wri().resourceIdentifier(),
								new Annotation(parameter.value(),
										new SimpleDateFormat(
												"d MMM yyyy HH:mm:ss")
												.format(new Date()), request
												.user()));
					}
				}
				// Only objects may be annotated, thus this is a get request
				isGetRequest = true;
			} else if (action.actionSchemaNode()) {
				GroupSchemaNode parent = null;
				if (!request.isRootRequest()) {
					parent = (GroupSchemaNode) ((DatabaseElementNode) _database
							.get(request.wri().resourceIdentifier())).schema();
				}
				_database.insertSchemaNode(
						parent,
						request.parameters().get(ParameterSchemaNodeName)
								.value(),
						Byte.parseByte(request.parameters()
								.get(ParameterSchemaNodeType).value()),
						request.user());
				isGetRequest = !request.isRootRequest();
				isIndexRequest = !isGetRequest;
			} else if (action.actionInsert()) {
				DocumentNode insertNode = this.getInsertNode(request);
				page = new RedirectPage(request, database().insertNode(
						request.wri().resourceIdentifier(), insertNode,
						request.user()));
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterDatabaseID)) {
					// Updating a configuration file
					this.updateConfigurationFile(request);
					// Configuration files may be modified either while viewing
					// the
					// database index or and object. Make sure to display the
					// appropriate
					// page after the update.
					isGetRequest = !request.isRootRequest();
					isIndexRequest = !isGetRequest;
				} else {
					// Updating a data object
					Update update = this.getNodeUpdates(request);
					if (update != null) {
						database().update(request.wri().resourceIdentifier(),
								update, request.user());
						if (request.node().isText()) {
							page = new RedirectPage(request,
									((DatabaseTextNode) request.node())
											.parent().identifier());
						}
					}
					isGetRequest = true;
				}
			}
		} else if (request.type().isActivate()) {
			database().activate(request.wri().resourceIdentifier(),
					request.user());
		} else if (request.type().isCopy()) {
			String sourceURL = HttpRequest.CookiePropertyCopyBuffer + "="
					+ "http://localhost" + ":"
					+ exchange.getLocalAddress().getPort()
					+ request.wri().getURL();
			RequestParameterVersion version = RequestParameter
					.versionParameter(request.parameters().get(
							RequestParameter.ParameterVersion));
			if (version.versionSingle()) {
				sourceURL = sourceURL + "?" + version.toURLString();
			} else {
				sourceURL = sourceURL
						+ "?"
						+ new RequestParameterVersionSingle(this.database()
								.versionIndex().getLastVersion().number())
								.toURLString();
			}
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Set-Cookie", sourceURL + "; path=/; ");
		} else if (request.type().isDelete()) {
			database().delete(request.wri().resourceIdentifier(),
					request.user());
			if (request.node().parent() != null) {
				page = new RedirectPage(request, request.node().parent()
						.identifier());
			} else {
				isIndexRequest = true;
			}
		} else if (request.type().isPaste()) {
			String url = null;
			if (request.parameters()
					.hasParameter(RequestParameter.ParameterURL)) {
				url = request.parameters().get(RequestParameter.ParameterURL)
						.value();
			} else {
				url = request.copyBuffer();
			}
			this.pasteURL(request, url);
			isGetRequest = !request.isRootRequest();
			isIndexRequest = !isGetRequest;
		} else if (request.type().isReset()) {
			this.resetConfiguration(request.parameters()
					.get(RequestParameter.ParameterReset).value());
			isGetRequest = !request.isRootRequest();
			isIndexRequest = !isGetRequest;
		} else if (request.type().isCopyPasteExport()) {
			this.respondToExportXMLRequest(request, new CopyPasteNodeWriter(),
					exchange);
			return;
		} else if (request.type().isExportXML()) {
			this.respondToExportXMLRequest(request, new ExportNodeWriter(),
					exchange);
			return;
		} else if (request.type().isExportJSON()) {
			this.respondToExportJSONRequest(request,
					new ExportJSONNodeWriter(), exchange);
			return;
		}

		// If the request is not redirected (in case of INSERT or DELETE) then
		// assemble appropriate
		// HtmlContentGenerator.
		if (page == null) {
			DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(
					request, this.getTitle(), this.cssLinePrinter());
			if ((isGetRequest) || (request.type().isCopy())) {// This is the
																// default case
																// where no
																// action has
																// been
																// performed and
																// no special
																// content is
																// requested
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentTimemachine,
						new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
						new DataMenuPrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentObjectLink,
						new NodePathPrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentAnnotation,
						new ObjectAnnotationPrinter(request));
				// contentGenerator.put(DatabaseWikiContentGenerator.ContentProvenance,
				// new VersionIndexPrinter(request));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentProvenance,
						new ObjectProvenancePrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new DataNodePrinter(request, _layouter));
			} else if (isIndexRequest) { // The case for the root of the
											// DatabaseWiki
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentTimemachine,
						new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
						new DataMenuPrinter(request, _layouter));
				// TODO: This could be simplified by storing the mapping in a
				// Map<String,IndexContentPrinter>
				if (DatabaseLayouter.IndexAZMultiPage.equals(_layouter
						.indexType())) {
					contentGenerator.put(
							DatabaseWikiContentGenerator.ContentContent,
							new AZMultiPageIndexPrinter(request, database()
									.content()));
				} else if (DatabaseLayouter.IndexAZSinglePage.equals(_layouter
						.indexType())) {
					contentGenerator.put(
							DatabaseWikiContentGenerator.ContentContent,
							new AZSinglePageIndexPrinter(request, database()
									.content()));
				} else if (DatabaseLayouter.IndexMultiColumn.equals(_layouter
						.indexType())) {
					contentGenerator.put(
							DatabaseWikiContentGenerator.ContentContent,
							new MultiColumnIndexPrinter(request, database()
									.content()));
				} else if (DatabaseLayouter.IndexPartialList.equals(_layouter
						.indexType())) {
					contentGenerator.put(
							DatabaseWikiContentGenerator.ContentContent,
							new PartialIndexPrinter(request, database()
									.content()));
				} else {
					contentGenerator
							.put(DatabaseWikiContentGenerator.ContentContent,
									new FullIndexPrinter(request, database()
											.content()));
				}
			} else if (request.type().isSearch()) { // The case for a search
													// request
				DatabaseContent content = null;
				String query = request.parameters()
						.get(RequestParameter.ParameterSearch).value();
				if (query != null) {
					content = database().search(query);
				} else {
					content = database().content();
				}
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentTimemachine,
						new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
						new DataMenuPrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new SearchResultPrinter(request, content));
			} else if ((request.type().isCreate()) || (request.type().isEdit())) { // The
																					// case
																					// for
																					// a
																					// create
																					// or
																					// edit
																					// request
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentObjectLink,
						new NodePathPrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new DataUpdateFormPrinter(request, _layouter));
			} else if (request.type().isCreateSchemaNode()) { // Creating a new
																// schema node.
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentObjectLink,
						new NodePathPrinter(request, _layouter));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new CreateSchemaNodeFormPrinter(request));
			} else if ((request.type().isTimemachineChanges())
					|| ((request.type().isTimemachinePrevious()))) {
				if (request.node() != null) { // Showing version index
					contentGenerator.put(
							DatabaseWikiContentGenerator.ContentObjectLink,
							new NodePathPrinter(request, _layouter));
				}
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentTimemachine,
						new TimemachinePrinter(request));
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new VersionIndexPrinter(request));
			} else if (request.type().isLayout()) { // Editing the layout
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new LayoutEditor(request));
			} else if (request.type().isPasteForm()) { // Pasting XML data from
														// a URL
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new InputFormPrinter(request, "Copy & Paste",
								"Insert source URL",
								RequestParameter.ParameterPaste,
								RequestParameter.ParameterURL));
			} else if (request.type().isStyleSheet()) { // Editing the
														// stylesheet
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new FileEditor(request, "Edit style sheet"));
			} else if (request.type().isTemplate()) { // Editing the template
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new FileEditor(request, "Edit template"));
			} else if (request.type().isURLDecoding()) { // Editing the URL
															// decoding rules
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new FileEditor(request, "Edit URL decoding rules"));
			} else if (request.type().isSettings()) { // The list of prior
														// combinations of
														// config files, can be
														// used to revert.
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new SettingsListingPrinter(request));
			} else {
				throw new WikiRequestException(
						WikiRequestException.InvalidRequest, exchange
								.getRequestURI().toASCIIString());
			}
			page = HtmlTemplateDecorator.decorate(_template, contentGenerator);
		}

		// Send the resulting page to the user.
		HtmlSender.send(page, exchange);
		
	}

	/**
	 * Responds to requests for wiki pages 
	 * TODO: Factor this out so that wiki pages are handled at server level.
	 * 
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToPageRequest(WikiPageRequest request,
			HttpExchange exchange) throws java.io.IOException,
			org.dbwiki.exception.WikiException {
		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();

		RequestParameterAction action = new RequestParameterActionCancel();
		if (request.type().isDelete()) {
			wiki().delete((PageIdentifier) request.wri().resourceIdentifier());
			HtmlSender.send(new RedirectPage(request.wri().databaseIdentifier()
					.databaseHomepage()), exchange);
			return;
		} else if (request.type().isAction()) {
			action = RequestParameter.actionParameter(request.parameters().get(
					RequestParameter.ParameterAction));
			if (action.actionInsert()) {
				wiki().insert(request.getWikiPage(), request.user());
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterDatabaseID)) {
					this.updateConfigurationFile(request);
					isGetRequest = !request.isRootRequest();
					isIndexRequest = !isGetRequest;
				} else {
					wiki().update(
							new PageIdentifier(request.parameters()
									.get(RequestParameter.ActionValuePageID)
									.value()), request.getWikiPage(),
							request.user());
					isGetRequest = true;
					isIndexRequest = false;
				}
			}
		}

		DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(
				request, this.getTitle(), this.cssLinePrinter());

		if (isGetRequest) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
					new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new PageContentPrinter(request, _layouter));
		} else if ((isIndexRequest) || (request.type().isDelete())
				|| (action.actionInsert())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
					new PageMenuPrinter(request));
			if (DatabaseLayouter.IndexAZMultiPage.equals(_layouter.indexType())) {
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new AZMultiPageIndexPrinter(request, wiki().content()));
			} else if (DatabaseLayouter.IndexAZSinglePage.equals(_layouter
					.indexType())) {
				contentGenerator
						.put(DatabaseWikiContentGenerator.ContentContent,
								new AZSinglePageIndexPrinter(request, wiki()
										.content()));
			} else if (DatabaseLayouter.IndexMultiColumn.equals(_layouter
					.indexType())) {
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new MultiColumnIndexPrinter(request, wiki().content()));
			} else if (DatabaseLayouter.IndexPartialList.equals(_layouter
					.indexType())) {
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new PartialIndexPrinter(request, wiki().content()));
			} else {
				contentGenerator.put(
						DatabaseWikiContentGenerator.ContentContent,
						new FullIndexPrinter(request, wiki().content()));
			}
		} else if ((request.type().isCreate()) || (request.type().isEdit())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new PageUpdateFormPrinter(request));
		} else if (request.type().isLayout()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new LayoutEditor(request));
		} else if (request.type().isStyleSheet()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new FileEditor(request, "Edit style sheet"));
		} else if (request.type().isTemplate()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new FileEditor(request, "Edit template"));
		} else if (request.type().isURLDecoding()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new FileEditor(request, "Edit URL decoding rules"));
		} else if (request.type().isSettings()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new SettingsListingPrinter(request));
		} else if (request.type().isPageHistory()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
					new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new PageHistoryPrinter(request));
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidRequest,
					exchange.getRequestURI().toASCIIString());
		}
		HtmlSender.send(
				HtmlTemplateDecorator.decorate(_template, contentGenerator),
				exchange);
	}

	/**
	 * Respond to request for a schema node Like respondToDataRequest, this
	 * first checks the operation is a GET or POST. If POST, then the action is
	 * performed and the response redirects. If GET, then an appropriate
	 * ContentGenerator is constructed and used to render the schema node.
	 * 
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToSchemaRequest(WikiSchemaRequest request,
			HttpExchange exchange) throws java.io.IOException,
			org.dbwiki.exception.WikiException {
		// TODO: fill in and tidy up
		//
		// Most of the code here is copied and pasted from above.

		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();

		if (request.type().isDelete()) {
			database().deleteSchemaNode(request.wri().resourceIdentifier(),
					request.user());

			if (request.schema().parent() != null) {
				HtmlSender.send(new RedirectPage(request, request.schema()
						.parent().identifier()), exchange);
				return;
			} else {
				isIndexRequest = true;
			}
		}

		// This is where all the action happens. Note that .isAction() requests
		// result from
		// HTTP POST request. The class RequestType currently does not
		// distinguish these
		// requests further, thus it has to be done here.
		if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter
					.actionParameter(request.parameters().get(
							RequestParameter.ParameterAction));
			GroupSchemaNode parent = null;
			if (action.actionSchemaNode()) {
				if (!request.isRootRequest())
					parent = (GroupSchemaNode) request.schema();
				_database.insertSchemaNode(
						parent,
						request.parameters().get(ParameterSchemaNodeName)
								.value(),
						Byte.parseByte(request.parameters()
								.get(ParameterSchemaNodeType).value()),
						request.user());
				isGetRequest = !request.isRootRequest();
				isIndexRequest = !isGetRequest;
			}
		}

		// RequestParameterAction action = new RequestParameterActionCancel();
		// if (request.type().isDelete()) {
		// wiki().delete((PageIdentifier)request.wri().resourceIdentifier());
		// new
		// RedirectPage(request.wri().databaseIdentifier().databaseHomepage()).send(request.exchange());
		// return;
		// } else if (request.type().isAction()) {
		// action =
		// RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
		// if (action.actionInsert()) {
		// wiki().insert(this.getWikiPage(request), request.user());
		// } else if (action.actionUpdate()) {
		// if (request.parameters().hasParameter(ParameterWikiID)) {
		// this.updateConfigurationFile(request);
		// isGetRequest = !request.isRootRequest();
		// isIndexRequest = !isGetRequest;
		// } else {
		// wiki().update(new
		// PageIdentifier(request.parameters().get(RequestParameter.ActionValuePageID).value()),
		// this.getWikiPage(request), request.user());
		// isGetRequest = true;
		// isIndexRequest = false;
		// }
		// }
		// }
		//
		DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(
				request, this.getTitle(), this.cssLinePrinter());

		if (isGetRequest) {
			contentGenerator.put(
					DatabaseWikiContentGenerator.ContentTimemachine,
					new TimemachinePrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
					new SchemaMenuPrinter(request));
			contentGenerator.put(
					DatabaseWikiContentGenerator.ContentObjectLink,
					new SchemaPathPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new SchemaNodePrinter(request, _layouter));
		} else if ((isIndexRequest) || (request.type().isDelete())) { // ||
																		// (action.actionInsert()))
																		// {
			contentGenerator.put(
					DatabaseWikiContentGenerator.ContentTimemachine,
					new TimemachinePrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu,
					new SchemaMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new SchemaNodePrinter(request, _layouter));
		} else if (request.type().isCreateSchemaNode()
				&& request.schema().isGroup()) {
			// FIXME #schemaversioning: only display the option to create a new
			// schema node if we're viewing a group?
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent,
					new CreateSchemaNodeFormPrinter(request));
		}

		// if (IndexAZMultiPage.equals(_layouter.indexType())) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// AZMultiPageIndexPrinter(request, wiki().content()));
		// } else if (IndexAZSinglePage.equals(_layouter.indexType())) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// AZSinglePageIndexPrinter(request, wiki().content()));
		// } else if (IndexMultiColumn.equals(_layouter.indexType())) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// MultiColumnIndexPrinter(request, wiki().content()));
		// } else if (IndexPartialList.equals(_layouter.indexType())) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// PartialIndexPrinter(request, wiki().content()));
		// } else {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// FullIndexPrinter(request, wiki().content()));
		// }
		// }
		// } //else if ((request.type().isCreate()) ||
		// (request.type().isEdit())) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// PageUpdateFormPrinter(request));
		// } else if (request.type().isLayout()) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// LayoutEditor(request));
		// } else if (request.type().isStyleSheet()) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// FileEditor(request, "Edit style sheet"));
		// } else if (request.type().isTemplate()) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// FileEditor(request, "Edit template"));
		// } else if (request.type().isSettings()) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// SettingsListingPrinter(request));
		// } else if (request.type().isPageHistory()) {
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new
		// PageMenuPrinter(request));
		// contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new
		// PageHistoryPrinter(request));
		// } else {
		// throw new WikiRequestException(WikiRequestException.InvalidRequest,
		// request.exchange().getRequestURI().toASCIIString());
		// }

		HtmlSender.send(
				HtmlTemplateDecorator.decorate(_template, contentGenerator),
				exchange);
	}

	@Override
	public WikiServer server() {
		return _server;
	}

}
