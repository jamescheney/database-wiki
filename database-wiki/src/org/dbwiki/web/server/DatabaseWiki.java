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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringWriter;

import java.net.URL;

import java.sql.Connection;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.dbwiki.data.annotation.Annotation;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.database.NodeUpdate;
import org.dbwiki.data.database.Update;

import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.io.CopyPasteInputHandler;
import org.dbwiki.data.io.CopyPasteNodeWriter;
import org.dbwiki.data.io.ExportNodeWriter;
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.io.SAXCallbackInputHandler;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.PageIdentifier;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.wiki.SimpleWiki;
import org.dbwiki.data.wiki.Wiki;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.SQLVersionIndex;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.user.UserListing;

import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.html.RedirectPage;

import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.URLDecodingRules;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiRequest;
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

import org.dbwiki.web.ui.printer.CSSLinePrinter;
import org.dbwiki.web.ui.printer.FileEditor;
import org.dbwiki.web.ui.printer.LayoutEditor;
import org.dbwiki.web.ui.printer.ObjectAnnotationPrinter;
import org.dbwiki.web.ui.printer.ObjectProvenancePrinter;
import org.dbwiki.web.ui.printer.SettingsListingPrinter;
import org.dbwiki.web.ui.printer.TimemachinePrinter;
import org.dbwiki.web.ui.printer.VersionIndexPrinter;

import org.dbwiki.web.ui.printer.data.CreateSchemaNodeFormPrinter;
import org.dbwiki.web.ui.printer.data.DataMenuPrinter;
import org.dbwiki.web.ui.printer.data.DataUpdateFormPrinter;
import org.dbwiki.web.ui.printer.data.DataNodePrinter;
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

import org.dbwiki.web.ui.printer.schema.SchemaNodePrinter;
import org.dbwiki.web.ui.printer.schema.SchemaMenuPrinter;
import org.dbwiki.web.ui.printer.schema.SchemaPathPrinter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/** Implements the DatabaseWiki functionality for a given database. 
 * 
 * @author jcheney
 *
 */
public class DatabaseWiki implements HttpHandler, Comparable<DatabaseWiki> {
	/*
	 * Public Constants
	 */
	
	// Parameter values that indicate whether automatic schema
	// changes are allowed when importing (copy/paste, merge)
	// data
	public static final int AutoSchemaChangesAllow  = 0;
	public static final int AutoSchemaChangesIgnore = 1;
	public static final int AutoSchemaChangesNever  = 2;
	
	public static final String IndexAZMultiPage     = "AZ_MULTI_PAGE";
	public static final String IndexAZSinglePage    = "AZ_SINGLE_PAGE";
	public static final String IndexFullList        = "FULL_LIST";
	public static final String IndexMultiColumn     = "MULTI_COLUMN";
	public static final String IndexPartialList     = "PARTIAL_LIST";
	
	public static final String ParameterSchemaNodeName = "schema_node_name";
	public static final String ParameterSchemaNodeType = "schema_node_type";
	public static final String ParameterFileContent    = "file_content";
	public static final String ParameterFileType       = "file_type";
	public static final String ParameterDatabaseID     = "database_id";


	/*
	 * Public Constants
	 */
	public static final String WikiPageRequestPrefix = "wiki";
	public static final String SchemaRequestPrefix = "schema";
	
	/*
	 * Private Variables
	 */
	private WikiAuthenticator _authenticator;
	private int _autoSchemaChanges;
	private CSSLinePrinter _cssLinePrinter;
	private int _cssVersion;
	private int _templateVersion;
	private int _layoutVersion;
	private Database _database;
	private int _id;
	private DatabaseLayouter _layouter = null;
	private String _name;
	private WikiServer _server;
	private String _template = null;
	private String _title;
	private int _urlDecodingVersion;
	private URLDecodingRules _urlDecoder;
	private Wiki _wiki;
	
	/*
	 * Constructors
	 */
	
	/** Create new DatabaseWiki from given data.  Used in WikiServer.getWikiListing.
	 * 
	 */
	public DatabaseWiki(int id, String name, String title, WikiAuthenticator authenticator, int autoSchemaChanges, ConfigSetting setting, DatabaseConnector connector, WikiServer server) throws org.dbwiki.exception.WikiException {
		_authenticator = authenticator;
		_autoSchemaChanges = autoSchemaChanges;
		_id = id;
		_server = server;
		_name = name;
		_title = title;
		
		reset(setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
		
		_database = new RDBMSDatabase(this, connector);
		_wiki = new SimpleWiki(name, connector, server.users());
	}
	
	// HACK: pass in and use an existing connection and version index.
	// Used only in WikiServer.RegisterDatabase to create a new database.
	public DatabaseWiki(int id, String name, String title, WikiAuthenticator authenticator, int autoSchemaChanges, DatabaseConnector connector,
						WikiServer server, Connection con, SQLVersionIndex versionIndex)
	throws org.dbwiki.exception.WikiException {
		_authenticator = authenticator;
		_autoSchemaChanges = autoSchemaChanges;
		_id = id;
		_server = server;
		_name = name;
		_title = title;
		
		ConfigSetting setting = new ConfigSetting();
		
		reset(setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
		
		_database = new RDBMSDatabase(this, connector, con, versionIndex);
		_wiki = new SimpleWiki(name, connector, server.users());
	}
	
	
	/*
	 * Public Methods
	 */

	/** Comparator.  Compare database wikis by title, to sort list of wikis.
	 * 
	 */
 	public int compareTo(DatabaseWiki wiki) {
	 	return this.getTitle().compareTo(wiki.getTitle());
	}
	
	
	/* 
	 * Getters
	 */

	public WikiAuthenticator authenticator() {
		return _authenticator;
	}
	
	public int getAutoSchemaChanges() {
		return _autoSchemaChanges;
	}
	
	public CSSLinePrinter cssLinePrinter() {
		return _cssLinePrinter;
	}
	
	public URLDecodingRules urlDecoder() throws org.dbwiki.exception.WikiException {
		if (_urlDecoder == null) {
			_urlDecoder = new URLDecodingRules(_database.schema(), _server.getURLDecoding(this, _urlDecodingVersion));
		}
		return _urlDecoder;
	}
	

	public Database database() {
		return _database;
	}
	
	
	@Deprecated
	public AttributeSchemaNode displaySchemaNode(DatabaseSchema schema) {
		return _layouter.displaySchemaNode(schema);
	}
	
	/** Gets the string value of a template or stylesheet, for use in the editor form.
	 * 
	 * @param fileType
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public String getContent(int fileType) throws org.dbwiki.exception.WikiException {
		if (fileType == WikiServerConstants.RelConfigFileColFileTypeValTemplate) {
			return _template;
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValCSS) {
			return _server.getStyleSheet(this, _cssVersion);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValURLDecoding) {
			return _server.getURLDecoding(this, _urlDecodingVersion);
		} else {
			throw new WikiFatalException("Unknown configuration file type");
		}
	}
	
	public String getTitle() {
		return _title;
	}
	
	public UserListing users() {
		return _server.users();
	}
	
	public Wiki wiki() {
		return _wiki;
	}
	
	public WikiServer server() {
		return _server;
	}
	
	/**
	 * The list of all previous display settings for this wiki. Contains only
     * the file version numbers, not the actual data.
	 * @return Vector&lt;ConfigSetting&gt;
	 * @throws org.dbwiki.exception.WikiException
	 */
	public Vector<ConfigSetting> listSettings() throws org.dbwiki.exception.WikiException {
		
		return _server.listSettings(this);
	}
	
	public String name() {
		return _name;
	}
	
	public int id() {
		return _id;
	}
	
	public DatabaseIdentifier identifier() {
		return _database.identifier();
	}
	
	public DatabaseLayouter layouter() {
		return _layouter;
	}
	
	/*
	 * Setters
	 */
	
	/** Sets the auto schema changes policy.
	 */
	public void setAutoSchemaChanges(int autoSchemaChanges) {
		assert(autoSchemaChanges == AutoSchemaChangesNever || autoSchemaChanges == AutoSchemaChangesIgnore || autoSchemaChanges == AutoSchemaChangesAllow);
		_autoSchemaChanges = autoSchemaChanges;
	}

	public void setTitle(String value) {
		_title = value;
	}
	
	/* 
	 * Actions
	 */
	
	/**
	 * Reset configuration to the specified file versions.
	 */
	
	public void reset(int layoutVersion, int templateVersion, int styleSheetVersion, int urlDecodingVersion) throws org.dbwiki.exception.WikiException {
		_cssVersion = styleSheetVersion;
		_layoutVersion = layoutVersion;
		_templateVersion = templateVersion;
		_urlDecodingVersion = urlDecodingVersion;
		_template = _server.getTemplate(this, _templateVersion);
		_cssLinePrinter = new CSSLinePrinter(this.id(), _cssVersion);
		_urlDecoder = null;
		_layouter = new DatabaseLayouter(_server.getLayout(this, _layoutVersion));
	}
	
	public int getLayoutVersion() {
		return _layoutVersion;
	}
	
	public int getTemplateVersion() {
		return _templateVersion;
	}
	
	public int getCSSVersion() {
		return _cssVersion;
	}
	
	public int getURLDecodingVersion() {
		return _urlDecodingVersion;
	}
	
	/** Dispatches HTTP interactions based on the type of the request.
	 * Data requests are handled by respondToDataRequest
	 * Wiki Page requests are handled by respondToPageRequest
	 * Schema requests are handled by respondToSchemaRequest
	 */
	public void handle(HttpExchange exchange) throws java.io.IOException {
		try {
			String filename = exchange.getRequestURI().getPath();
			int pos = filename.lastIndexOf('.');
			if (pos != -1) {
	    		_server.sendFile(exchange);
			} else {
				if (_server.serverLog() != null) {
					_server.serverLog().logRequest(exchange.getRequestURI(),exchange.getRemoteAddress(),exchange.getResponseHeaders());
				}
				RequestURL url = new RequestURL(exchange, _database.identifier().linkPrefix());
				if (url.isDataRequest()) {
					respondToDataRequest(new WikiDataRequest(this, url));
				} else if (url.isPageRequest()) {
					respondToPageRequest(new WikiPageRequest(this, url));
				} else if (url.isSchemaRequest()) {
					respondToSchemaRequest(new WikiSchemaRequest(this, url));
				}
			}
		} catch (org.dbwiki.exception.WikiException wikiException) {
			wikiException.printStackTrace();
			try {
				HtmlSender.send(HtmlTemplateDecorator.decorate(_template, new DatabaseWikiContentGenerator(this, wikiException)),exchange);
			} catch (org.dbwiki.exception.WikiException exception) {
				HtmlSender.send(new FatalExceptionPage(exception),exchange);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			HtmlSender.send(new FatalExceptionPage(exception),exchange);
		}
	}

	
	/*
	 * Private Methods
	 */
	
	
	/** Gets the document node associated with an insert POST request.
	 * 
	 * @param request
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	private DocumentNode getInsertNode(WikiDataRequest request) throws org.dbwiki.exception.WikiException {
		SchemaNode schemaNode = database().schema().get(Integer.parseInt(request.parameters().get(RequestParameter.ActionValueSchemaNode).value()));
		if (schemaNode.isAttribute()) {
			AttributeSchemaNode attributeSchemaNode = (AttributeSchemaNode)schemaNode;
			DocumentAttributeNode attribute = new DocumentAttributeNode(attributeSchemaNode);
			RequestParameter parameter = request.parameters().get(RequestParameter.TextFieldIndicator + attributeSchemaNode.id());
			if (parameter.hasValue()) {
				if (!parameter.value().equals("")) {
					attribute.setValue(parameter.value());
				}
			}
			return attribute;
		} else {
			Hashtable<Integer, DocumentGroupNode> groupIndex = new Hashtable<Integer, DocumentGroupNode>();
			DocumentGroupNode root = SchemaNode.createGroupNode((GroupSchemaNode)schemaNode, groupIndex);
			for (int iParameter = 0; iParameter < request.parameters().size(); iParameter++) {
				RequestParameter parameter = request.parameters().get(iParameter);
				if ((parameter.name().startsWith(RequestParameter.TextFieldIndicator)) && (parameter.hasValue())) {
					if (!parameter.value().equals("")) {
						SchemaNode child = database().schema().get(Integer.parseInt(parameter.name().substring(RequestParameter.TextFieldIndicator.length())));
						if (child.isAttribute()) {
							DocumentAttributeNode attribute = new DocumentAttributeNode((AttributeSchemaNode)child);
							attribute.setValue(parameter.value());
							groupIndex.get(new Integer(attribute.schema().parent().id())).children().add(attribute);
						}
					}
				}
			}
			DocumentGroupNode.removeEmptyNodes(root);
			return root;
		}
	}
	
	

	/** Collects the node updates associated with the text fields of a POST request generated by a data edit form
	 *  
	 * @param request
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	private Update getNodeUpdates(WikiDataRequest request) throws org.dbwiki.exception.WikiException {
		Update updates = new Update();
		
		for (int iParameter = 0; iParameter < request.parameters().size(); iParameter++) {
			RequestParameter parameter = request.parameters().get(iParameter);
			if (parameter.name().startsWith(RequestParameter.TextFieldIndicator)) {
				if (parameter.hasValue()) {
					if (!parameter.value().equals("")) {
						updates.add(new NodeUpdate(database().getIdentifierForParameterString(parameter.name().substring(RequestParameter.TextFieldIndicator.length())), parameter.value()));
					}
				}
			}
		}

		return updates;
	}
	
	/** Handle a paste action
	 * 
	 * @param request
	 * @param url
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void pasteURL(WikiDataRequest request, String url) throws org.dbwiki.exception.WikiException {
		if (url != null) {
			CopyPasteInputHandler ioHandler = new CopyPasteInputHandler();
			String sourceURL = url;
			if (sourceURL.indexOf("?") != -1) {
				sourceURL = sourceURL + "&" + RequestParameter.ParameterCopyPasteExport;
			} else {
				sourceURL = sourceURL + "?" + RequestParameter.ParameterCopyPasteExport;
			}
			try {
				new SAXCallbackInputHandler(ioHandler, false).parse(new URL(sourceURL).openStream(), false, false);
			} catch (java.io.IOException ioException) {
				throw new WikiFatalException(ioException);
			} catch (org.xml.sax.SAXException saxException) {
				throw new WikiFatalException(saxException);
			}
			/* TODO: The version parameter may be missing in the source URL when copying from
			 * an external source.
			 */
			this.database().paste(request.wri().resourceIdentifier(), ioHandler.getPasteNode(), url, request.user());
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidUrl, "(null)");
		}
	}
	
	
	
	/** Reset the configuration.  
	 * The value is the parameter value of a ?reset=value request. The format
	 * currently is expected to be <int>_<int>_<int> and these <int>'s are
	 * layout file version, template file version, and style sheet file version.
	 * 
	 * @param value String
	 * @throws org.dbwiki.exception.WikiException
	 */
	private synchronized void resetConfiguration(String value) throws org.dbwiki.exception.WikiException {
		
		ConfigSetting setting = null;
		try {
			setting = new ConfigSetting(value);
		} catch (Exception exception) {
			throw new WikiFatalException(exception);
		}
		_server.resetWikiConfiguration(this, setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
	}
	
	/** Handles data export requests (generating XML)
	 * 
	 * @param request
	 * @param writer
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToExportRequest(WikiDataRequest request, NodeWriter writer) throws org.dbwiki.exception.WikiException {
		int versionNumber = database().versionIndex().getLastVersion().number();
		if (request.parameters().hasParameter(RequestParameter.ParameterVersion)) {
			versionNumber = ((RequestParameterVersionSingle)RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion))).versionNumber();
		}
		
		try {
			// if the request is for all the data in a DatabaseWiki, create a temporary file.
			//  Otherwise, do it in memory (ASSUMES each entry is small!).
			// TODO: This could probably be done uniformly by streaming instead.
			if (request.isRootRequest()) {
				File tmpFile = File.createTempFile("dbwiki", "xml");
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				writer.init(out);
				database().export(request.wri().resourceIdentifier(), versionNumber, writer);
				out.close();
				_server.sendXML(request.exchange(), new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				database().export(request.wri().resourceIdentifier(), versionNumber, writer);
				out.close();
				_server.sendXML(request.exchange(), new ByteArrayInputStream(buf.toString().getBytes("UTF-8")));
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	/** 
	 * Handles a request for a data node.  
	 * First check authentication.
	 * Next determine whether it's a GET request, or an action.  
	 * If it's an action, perform the action and redirect.
	 * If it's a GET request, there are many cases depending on the particular content being 
	 * requested (e.g. the current version, past versions, xml, search, file editor, etc.) 
	 * In each case, build appropriate content generator and plug into the template.
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToDataRequest(WikiDataRequest request) throws java.io.IOException, org.dbwiki.exception.WikiException {
		HtmlPage page = null;
		
		// The following test is just an additional security check in case someone
		// managed to get past the WikiAuthenticator.
		if ((request.user() == null) && (_authenticator.getAuthenticationMode() != WikiAuthenticator.AuthenticateNever)) {
			if ((request.type().isAction()) || (request.type().isActivate()) || (request.type().isDelete()) || request.type().isPaste()) {
				throw new WikiFatalException("User login required to perform requested operation");
			}
		}
		
		// All requests of type .isAction() have to result in .isGet() or .isIndex() requests
		// which is not recognized/distinguished by the RequestType class. Thus, this
		// decision has to be taken below (thats why the following variables are needed).
		boolean isGetRequest = (request.type().isGet() || request.type().isActivate());
		boolean isIndexRequest = request.type().isIndex();
		
		// This is where all the action happens. Note that .isAction() requests result from
		// HTTP POST request. The class RequestType currently does not distinguish these
		// requests further, thus it has to be done here.
		if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			if (action.actionAnnotate()) {
				RequestParameter parameter = request.parameters().get(RequestParameter.ActionValueAnnotation);
				if (parameter.hasValue()) {
					if (!parameter.value().trim().equals("")) {
						database().annotate(request.wri().resourceIdentifier(), new Annotation(parameter.value(), new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new Date()), request.user()));
					}
				}
				// Only objects may be annotated, thus this is a get request
				isGetRequest = true;
			} else if (action.actionSchemaNode()) {
				GroupSchemaNode parent = null;
				if (!request.isRootRequest()) {
					parent = (GroupSchemaNode)((DatabaseElementNode)_database.get(request.wri().resourceIdentifier())).schema();
				}
				_database.insertSchemaNode(parent, request.parameters().get(ParameterSchemaNodeName).value(), Byte.parseByte(request.parameters().get(ParameterSchemaNodeType).value()), request.user());
				isGetRequest = !request.isRootRequest();
				isIndexRequest = ! isGetRequest;
			} else if (action.actionInsert()) {
				DocumentNode insertNode = this.getInsertNode(request);
				page = new RedirectPage(request, database().insertNode(request.wri().resourceIdentifier(), insertNode, request.user()));
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterDatabaseID)) {
					// Updating a configuration file
					this.updateConfigurationFile(request);
					// Configuration files may be modified either while viewing the
					// database index or and object. Make sure to display the appropriate
					// page after the update.
					isGetRequest = !request.isRootRequest();
					isIndexRequest = ! isGetRequest;
				} else {
					// Updating a data object
					Update update = this.getNodeUpdates(request);
					if (update != null) {
						database().update(request.wri().resourceIdentifier(), update, request.user());
						if (request.node().isText()) {
							page = new RedirectPage(request, ((DatabaseTextNode)request.node()).parent().identifier());
						}
					}
					isGetRequest = true;
				}
			}
		} else if (request.type().isActivate()) {
			database().activate(request.wri().resourceIdentifier(), request.user());
		} else if (request.type().isCopy()) {
			String sourceURL = HttpRequest.CookiePropertyCopyBuffer + "=" + "http://localhost" + ":" + request.exchange().getLocalAddress().getPort() + request.wri().getURL();
			RequestParameterVersion version = RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion));
			if (version.versionSingle()) {
				sourceURL = sourceURL + "?" + version.toURLString();
			} else {
				sourceURL = sourceURL + "?" + new RequestParameterVersionSingle(this.database().versionIndex().getLastVersion().number()).toURLString();
			}
	    	Headers responseHeaders = request.exchange().getResponseHeaders();
	    	responseHeaders.set("Set-Cookie", sourceURL + "; path=/; " );
		} else if (request.type().isDelete()) {
			database().delete(request.wri().resourceIdentifier(), request.user());
			if (request.node().parent() != null) {
				page = new RedirectPage(request, request.node().parent().identifier());
			} else {
				isIndexRequest = true;
			}
		} else if (request.type().isPaste()) {
			String url = null;
			if (request.parameters().hasParameter(RequestParameter.ParameterURL)) {
				url = request.parameters().get(RequestParameter.ParameterURL).value();
			} else {
				url = request.copyBuffer();
			}
			this.pasteURL(request, url);
			isGetRequest = !request.isRootRequest();
			isIndexRequest = ! isGetRequest;
		} else if (request.type().isReset()) {
			this.resetConfiguration(request.parameters().get(RequestParameter.ParameterReset).value());
			isGetRequest = !request.isRootRequest();
			isIndexRequest = ! isGetRequest;
		} else if (request.type().isCopyPasteExport()) {
			this.respondToExportRequest(request, new CopyPasteNodeWriter());
			return;
		} else if (request.type().isExport()) {
			this.respondToExportRequest(request, new ExportNodeWriter());
			return;
		}
		
		// If the request is not redirected (in case of INSERT or DELETE) then assemble appropriate
		// HtmlContentGenerator.
		if (page == null) {
			DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(this, request);
			if ((isGetRequest) || (request.type().isCopy())) {// This is the default case where no action has been performed and no special content is requested
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentAnnotation, new ObjectAnnotationPrinter(request));
				//contentGenerator.put(DatabaseWikiContentGenerator.ContentProvenance, new VersionIndexPrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentProvenance, new ObjectProvenancePrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new DataNodePrinter(request, _layouter));
			} else if (isIndexRequest) { // The case for the root of the DatabaseWiki
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				// TODO: This could be simplified by storing the mapping in a Map<String,IndexContentPrinter>
				if (IndexAZMultiPage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, database().content()));
				} else if (IndexAZSinglePage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, database().content()));
				} else if (IndexMultiColumn.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, database().content()));
				} else if (IndexPartialList.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, database().content()));
				} else {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FullIndexPrinter(request, database().content()));
				}
			} else if (request.type().isSearch()) { // The case for a search request
				DatabaseContent content = null;
				String query = request.parameters().get(RequestParameter.ParameterSearch).value();
				if (query != null) {
					content = database().search(query);
				} else {
					content = database().content();
				}
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SearchResultPrinter(request, content));
			} else if ((request.type().isCreate()) || (request.type().isEdit())) { // The case for a create or edit request
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new DataUpdateFormPrinter(request, _layouter));
			} else if (request.type().isCreateSchemaNode()) { // Creating a new schema node.
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new CreateSchemaNodeFormPrinter(request));
			} else if ((request.type().isTimemachineChanges()) || ((request.type().isTimemachinePrevious()))) {
				if (request.node() != null) { // Showing version index
					contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				}
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new VersionIndexPrinter(request));
			} else if (request.type().isLayout()) { // Editing the layout
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new LayoutEditor(request));
			} else if (request.type().isPasteForm()) { // Pasting XML data from a URL
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new InputFormPrinter(request, "Copy & Paste", "Insert source URL", RequestParameter.ParameterPaste, RequestParameter.ParameterURL));
			} else if (request.type().isStyleSheet()) { // Editing the stylesheet
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit style sheet"));
			} else if (request.type().isTemplate()) { // Editing the template
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit template"));
			} else if (request.type().isURLDecoding()) { // Editing the URL decoding rules
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit URL decoding rules"));
			} else if (request.type().isSettings()) { // The list of prior combinations of config files, can be used to revert.
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SettingsListingPrinter(request));
			} else {
				throw new WikiRequestException(WikiRequestException.InvalidRequest, request.exchange().getRequestURI().toASCIIString());
			}
			page = HtmlTemplateDecorator.decorate(_template, contentGenerator);
		}
		
		// Send the resulting page to the user.
		HtmlSender.send(page,request.exchange());
	}

	/** Responds to requests for wiki pages
	 * TODO: Factor this out so that wiki pages are handled at server level.
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToPageRequest(WikiPageRequest request) throws java.io.IOException, org.dbwiki.exception.WikiException {
		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();
		
		RequestParameterAction action = new RequestParameterActionCancel();
		if (request.type().isDelete()) {
			wiki().delete((PageIdentifier)request.wri().resourceIdentifier());
			HtmlSender.send(new RedirectPage(request.wri().databaseIdentifier().databaseHomepage()),request.exchange());
			return;
		} else if (request.type().isAction()) {
			action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			if (action.actionInsert()) {
				wiki().insert(request.getWikiPage(), request.user());
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterDatabaseID)) {
					this.updateConfigurationFile(request);
					isGetRequest = !request.isRootRequest();
					isIndexRequest = !isGetRequest;
				} else {
					wiki().update(new PageIdentifier(request.parameters().get(RequestParameter.ActionValuePageID).value()), request.getWikiPage(), request.user());
					isGetRequest = true;
					isIndexRequest = false;
				}
			}
		}
		
		DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(this, request);
		
		if (isGetRequest) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageContentPrinter(request, _layouter));
		} else if ((isIndexRequest) || (request.type().isDelete()) || (action.actionInsert())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			if (IndexAZMultiPage.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, wiki().content()));
			} else if (IndexAZSinglePage.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, wiki().content()));
			} else if (IndexMultiColumn.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, wiki().content()));
			} else if (IndexPartialList.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, wiki().content()));
			} else {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FullIndexPrinter(request, wiki().content()));
			}
		} else if ((request.type().isCreate()) || (request.type().isEdit())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageUpdateFormPrinter(request));
		} else if (request.type().isLayout()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new LayoutEditor(request));
		} else if (request.type().isStyleSheet()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit style sheet"));
		} else if (request.type().isTemplate()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit template"));
		} else if (request.type().isURLDecoding()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit URL decoding rules"));
		} else if (request.type().isSettings()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SettingsListingPrinter(request));
		} else if (request.type().isPageHistory()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageHistoryPrinter(request));
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidRequest, request.exchange().getRequestURI().toASCIIString());
		}
		HtmlSender.send(HtmlTemplateDecorator.decorate(_template, contentGenerator),request.exchange());
	}
		
	/** Respond to request for a schema node
	 * Like respondToDataRequest, this first checks the operation is a GET or POST.
	 * If POST, then the action is performed and the response redirects.
	 * If GET, then an appropriate ContentGenerator is constructed and 
	 * used to render the schema node.
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondToSchemaRequest(WikiSchemaRequest request) throws java.io.IOException, org.dbwiki.exception.WikiException {
		// TODO: fill in and tidy up
		// 
		// Most of the code here is copied and pasted from above.
		
		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();
		
		if (request.type().isDelete()) {
			database().deleteSchemaNode(request.wri().resourceIdentifier(), request.user());
			
			if (request.schema().parent() != null) {
				HtmlSender.send(new RedirectPage(request, request.schema().parent().identifier()),request.exchange());
				return;
			} else {
				isIndexRequest = true;
			}
		}
		
		// This is where all the action happens. Note that .isAction() requests result from
		// HTTP POST request. The class RequestType currently does not distinguish these
		// requests further, thus it has to be done here.
		if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			GroupSchemaNode parent = null;
			if (action.actionSchemaNode()) {
				if(!request.isRootRequest())
					parent = (GroupSchemaNode)request.schema();
				_database.insertSchemaNode(parent, request.parameters().get(ParameterSchemaNodeName).value(), Byte.parseByte(request.parameters().get(ParameterSchemaNodeType).value()), request.user());
				isGetRequest = !request.isRootRequest();
				isIndexRequest = !isGetRequest;
			}
		}
		
//		RequestParameterAction action = new RequestParameterActionCancel();
//		if (request.type().isDelete()) {
//			wiki().delete((PageIdentifier)request.wri().resourceIdentifier());
//			new RedirectPage(request.wri().databaseIdentifier().databaseHomepage()).send(request.exchange());
//			return;
//		} else if (request.type().isAction()) {
//			action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
//			if (action.actionInsert()) {
//				wiki().insert(this.getWikiPage(request), request.user());
//			} else if (action.actionUpdate()) {
//				if (request.parameters().hasParameter(ParameterWikiID)) {
//					this.updateConfigurationFile(request);
//					isGetRequest = !request.isRootRequest();
//					isIndexRequest = !isGetRequest;
//				} else {
//					wiki().update(new PageIdentifier(request.parameters().get(RequestParameter.ActionValuePageID).value()), this.getWikiPage(request), request.user());
//					isGetRequest = true;
//					isIndexRequest = false;
//				}
//			}
//		}
//		
		DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(this, request);
		
		if (isGetRequest) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new SchemaMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new SchemaPathPrinter(request, _layouter));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SchemaNodePrinter(request, _layouter));
		} else if ((isIndexRequest) || (request.type().isDelete())) { // || (action.actionInsert())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new SchemaMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SchemaNodePrinter(request, _layouter));
		} else if (request.type().isCreateSchemaNode() && request.schema().isGroup()) {
			// FIXME #schemaversioning: only display the option to create a new schema node if we're viewing a group?
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new CreateSchemaNodeFormPrinter(request));
		}


//			if (IndexAZMultiPage.equals(_layouter.indexType())) {
//				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, wiki().content()));
//			} else if (IndexAZSinglePage.equals(_layouter.indexType())) {
//				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, wiki().content()));
//			} else if (IndexMultiColumn.equals(_layouter.indexType())) {
//				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, wiki().content()));
//			} else if (IndexPartialList.equals(_layouter.indexType())) {
//				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, wiki().content()));
//			} else {
//				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FullIndexPrinter(request, wiki().content()));
//			}
//		}
//		} //else if ((request.type().isCreate()) || (request.type().isEdit())) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageUpdateFormPrinter(request));
//		} else if (request.type().isLayout()) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new LayoutEditor(request));
//		} else if (request.type().isStyleSheet()) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit style sheet"));
//		} else if (request.type().isTemplate()) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit template"));
//		} else if (request.type().isSettings()) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SettingsListingPrinter(request));
//		} else if (request.type().isPageHistory()) {
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
//			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageHistoryPrinter(request));
//		} else {
//			throw new WikiRequestException(WikiRequestException.InvalidRequest, request.exchange().getRequestURI().toASCIIString());
//		}
		
		HtmlSender.send(HtmlTemplateDecorator.decorate(_template, contentGenerator), request.exchange());
	}
	
	/** Handles POST requests that provide a new version of a config file.
	 * 
	 * @param request
	 * @throws org.dbwiki.exception.WikiException
	 */
	  
	private synchronized void updateConfigurationFile(WikiRequest request) throws org.dbwiki.exception.WikiException {
		int wikiID = Integer.valueOf(request.parameters().get(ParameterDatabaseID).value());
		int fileType = Integer.valueOf(request.parameters().get(ParameterFileType).value());
		
		String value = null;
		
		if (fileType == WikiServerConstants.RelConfigFileColFileTypeValLayout) {
			Properties properties = new Properties();
			for (int iParameter = 0; iParameter < request.parameters().size(); iParameter++) {
				RequestParameter parameter = request.parameters().get(iParameter);
				if (DatabaseLayouter.isLayoutParameter(parameter.name())) {
					if (parameter.hasValue()) {
						properties.setProperty(parameter.name(), parameter.value());
					} else {
						properties.setProperty(parameter.name(), "(null)");
					}
				}
			}
			StringWriter writer = new StringWriter();
			try {
				properties.store(writer, "Database Layout");
			} catch (java.io.IOException ioException) {
				throw new WikiFatalException(ioException);
			}
			value = writer.toString();
		} else {
			value = request.parameters().get(ParameterFileContent).value();
		}
		if (fileType == WikiServerConstants.RelConfigFileColFileTypeValLayout) {
			_server.updateConfigFile(wikiID, fileType, value, request.user());
			_layouter = new DatabaseLayouter(value);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValTemplate) {
			_server.updateConfigFile(wikiID, fileType, value, request.user());
			_template = value;
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValCSS) {
			_cssVersion = _server.updateConfigFile(wikiID, fileType, value, request.user());
			_cssLinePrinter = new CSSLinePrinter(this.id(), _cssVersion);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValURLDecoding) {
			_urlDecoder = new URLDecodingRules(_database.schema(), value);
			_urlDecodingVersion = _server.updateConfigFile(wikiID, fileType, value, request.user());
		}
	}
}
