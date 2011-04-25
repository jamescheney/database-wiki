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
import org.dbwiki.data.io.SAXDocumentParser;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.PageIdentifier;

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.data.wiki.DatabaseWikiPage;
import org.dbwiki.data.wiki.SimpleWiki;
import org.dbwiki.data.wiki.Wiki;

import org.dbwiki.driver.rdbms.RDBMSDatabase;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.lib.JDBCConnector;

import org.dbwiki.user.UserListing;

import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.html.RedirectPage;

import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiPageRequest;
import org.dbwiki.web.request.WikiRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.request.parameter.RequestParameterActionCancel;
import org.dbwiki.web.request.parameter.RequestParameterVersion;
import org.dbwiki.web.request.parameter.RequestParameterVersionSingle;

import org.dbwiki.web.security.WikiAuthenticator;

import org.dbwiki.web.ui.DatabaseWikiContentGenerator;
import org.dbwiki.web.ui.ExceptionContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.RequestContentGenerator;

import org.dbwiki.web.ui.layout.DatabaseLayouter;

import org.dbwiki.web.ui.printer.CSSLinePrinter;
import org.dbwiki.web.ui.printer.FileEditor;
import org.dbwiki.web.ui.printer.LayoutEditor;
import org.dbwiki.web.ui.printer.ObjectAnnotationPrinter;
import org.dbwiki.web.ui.printer.ObjectProvenancePrinter;
import org.dbwiki.web.ui.printer.SettingsListingPrinter;
import org.dbwiki.web.ui.printer.TimemachinePrinter;
import org.dbwiki.web.ui.printer.VersionIndexPrinter;

import org.dbwiki.web.ui.printer.data.CreateEntityFormPrinter;
import org.dbwiki.web.ui.printer.data.DataMenuPrinter;
import org.dbwiki.web.ui.printer.data.DataUpdateFormPrinter;
import org.dbwiki.web.ui.printer.data.DataValuePrinter;
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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
	
	public static final String ParameterEntityName  = "entity_name";
	public static final String ParameterEntityType  = "entity_type";
	public static final String ParameterFileContent = "file_content";
	public static final String ParameterFileType    = "file_type";
	public static final String ParameterWikiID      = "wiki_id";


	/*
	 * Public Constants
	 */
	
	public static final String WikiPageRequestPrefix = "wiki";
	
	
	/*
	 * Private Variables
	 */
	
	private WikiAuthenticator _authenticator;
	private int _autoSchemaChanges;
	private CSSLinePrinter _cssLinePrinter;
	private int _cssVersion;
	private Database _database;
	private int _id;
	private DatabaseLayouter _layouter = null;
	private String _name;
	private WikiServer _server;
	private String _template = null;
	private String _title;
	private Wiki _wiki;
	
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseWiki(int id, String name, String title, WikiAuthenticator authenticator, int autoSchemaChanges, ConfigSetting setting, JDBCConnector connector, WikiServer server) throws org.dbwiki.exception.WikiException {
		_authenticator = authenticator;
		_autoSchemaChanges = autoSchemaChanges;
		_id = id;
		_server = server;
		_name = name;
		_title = title;
		
		this.reset(setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion());
		
		_database = new RDBMSDatabase(this, connector);
		_wiki = new SimpleWiki(name, connector, server.users());
	}
	
	public DatabaseWiki(int id, String name, String title, WikiAuthenticator authenticator, int autoSchemaChanges, JDBCConnector connector, WikiServer server) throws org.dbwiki.exception.WikiException {
		this(id, name, title, authenticator, autoSchemaChanges, new ConfigSetting(), connector, server);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public WikiAuthenticator authenticator() {
		return _authenticator;
	}
	
	public int getAutoSchemaChanges() {
		return _autoSchemaChanges;
	}
	
	public int compareTo(DatabaseWiki wiki) {
		return this.getTitle().compareTo(wiki.getTitle());
	}
	
	public CSSLinePrinter cssLinePrinter() {
		return _cssLinePrinter;
	}
	
	public Database database() {
		return _database;
	}
	
	public AttributeEntity displayEntity(DatabaseSchema schema) {
		return _layouter.displayEntity(schema);
	}
	
	public String getContent(int fileType) throws org.dbwiki.exception.WikiException {
		if (fileType == WikiServerConstants.RelConfigFileColFileTypeValTemplate) {
			return _template;
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValCSS) {
			return _server.getStyleSheet(this, _cssVersion);
		} else {
			throw new WikiFatalException("Unknown configuration file type");
		}
	}
	
	public String getTitle() {
		return _title;
	}
	
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
					this.respondToDataRequest(new WikiDataRequest(this, url));
				} else {
					this.respondToPageRequest(new WikiPageRequest(this, url));
				}
			}
		} catch (org.dbwiki.exception.WikiException wikiException) {
			wikiException.printStackTrace();
			try {
				HtmlSender.send(new HtmlTemplateDecorator().decorate(_template, new ExceptionContentGenerator(this, wikiException)),exchange);
			} catch (org.dbwiki.exception.WikiException exception) {
				HtmlSender.send(new FatalExceptionPage(exception),exchange);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			HtmlSender.send(new FatalExceptionPage(exception),exchange);
		}
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
	
	public Vector<ConfigSetting> listSettings() throws org.dbwiki.exception.WikiException {
		// The list of all previous display settings for this wiki. Contains only
		// the file version numbers, not the actual data.
		return _server.listSettings(this);
	}
	
	public String name() {
		return _name;
	}
	
	public void reset(int layoutVersion, int templateVersion, int styleSheetVersion) throws org.dbwiki.exception.WikiException {
		// Reset configuration to the specified file versions.
		_cssVersion = styleSheetVersion;
		_template = _server.getTemplate(this, templateVersion);
		_cssLinePrinter = new CSSLinePrinter(this.id(), _cssVersion);
		_layouter = new DatabaseLayouter(_server.getLayout(this, layoutVersion));
	}
	
	public WikiServer server() {
		return _server;
	}
	
	public void setAutoSchemaChanges(int autoSchemaChanges) {
		_autoSchemaChanges = autoSchemaChanges;
	}

	public void setTitle(String value) {
		_title = value;
	}
	
	public UserListing users() {
		return _server.users();
	}
	
	public Wiki wiki() {
		return _wiki;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private DocumentGroupNode createGroupNode(GroupEntity entity, Hashtable<Integer, DocumentGroupNode> groupIndex) throws org.dbwiki.exception.WikiException {
		DocumentGroupNode root = new DocumentGroupNode(entity);
		
		groupIndex.put(new Integer(entity.id()), root);
		
		for (int iChild = 0; iChild < entity.children().size(); iChild++) {
			Entity child = entity.children().get(iChild);
			if (child.isGroup()) {
				root.children().add(this.createGroupNode((GroupEntity)child, groupIndex));
			}
		}
		return root;
	}
	
	private DocumentNode getInsertNode(WikiDataRequest request) throws org.dbwiki.exception.WikiException {
		Entity entity = request.wiki().database().schema().get(Integer.parseInt(request.parameters().get(RequestParameter.ActionValueEntity).value()));
		if (entity.isAttribute()) {
			AttributeEntity attributeEntity = (AttributeEntity)entity;
			DocumentAttributeNode attribute = new DocumentAttributeNode(attributeEntity);
			RequestParameter parameter = request.parameters().get(RequestParameter.TextFieldIndicator + attributeEntity.id());
			if (parameter.hasValue()) {
				if (!parameter.value().equals("")) {
					attribute.setValue(parameter.value());
				}
			}
			return attribute;
		} else {
			Hashtable<Integer, DocumentGroupNode> groupIndex = new Hashtable<Integer, DocumentGroupNode>();
			DocumentGroupNode root = this.createGroupNode((GroupEntity)entity, groupIndex);
			for (int iParameter = 0; iParameter < request.parameters().size(); iParameter++) {
				RequestParameter parameter = request.parameters().get(iParameter);
				if ((parameter.name().startsWith(RequestParameter.TextFieldIndicator)) && (parameter.hasValue())) {
					if (!parameter.value().equals("")) {
						Entity childEntity = request.wiki().database().schema().get(Integer.parseInt(parameter.name().substring(RequestParameter.TextFieldIndicator.length())));
						if (childEntity.isAttribute()) {
							DocumentAttributeNode attribute = new DocumentAttributeNode((AttributeEntity)childEntity);
							attribute.setValue(parameter.value());
							groupIndex.get(new Integer(attribute.entity().parent().id())).children().add(attribute);
						}
					}
				}
			}
			this.removeEmptyNodes(root);
			return root;
		}
	}
	
	// TODO: Eliminate method and call request function directly
	private DatabaseWikiPage getWikiPage(WikiPageRequest request) throws org.dbwiki.exception.WikiException {
		return request.getWikiPage();
		/*
		String title = null;
		String value = null;
		
		if (request.parameters().hasParameter(RequestParameter.ActionValuePageTitle)) {
			title = request.parameters().get(RequestParameter.ActionValuePageTitle).value();
		}
		if (request.parameters().hasParameter(RequestParameter.ActionValuePageValue)) {
			value = request.parameters().get(RequestParameter.ActionValuePageValue).value();
		}
		return new DatabaseWikiPage(-1, title, value, -1, request.user());
		*/
	}

	private Update getNodeUpdates(WikiDataRequest request) throws org.dbwiki.exception.WikiException {
		Update updates = new Update();
		
		for (int iParameter = 0; iParameter < request.parameters().size(); iParameter++) {
			RequestParameter parameter = request.parameters().get(iParameter);
			if (parameter.name().startsWith(RequestParameter.TextFieldIndicator)) {
				if (parameter.hasValue()) {
					if (!parameter.value().equals("")) {
						updates.add(new NodeUpdate(request.wiki().database().getIdentifierForParameterString(parameter.name().substring(RequestParameter.TextFieldIndicator.length())), parameter.value()));
					}
				}
			}
		}

		return updates;
	}
	
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
				new SAXDocumentParser().parse(new URL(sourceURL).openStream(), false, false, new SAXCallbackInputHandler(ioHandler, false));
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
	
	private void removeEmptyNodes(DocumentGroupNode root) {
		int iNode = 0;
		while (iNode < root.children().size()) {
			DocumentNode child = root.children().get(iNode);
			if (child.isGroup()) {
				DocumentGroupNode groupChild = (DocumentGroupNode)child;
				if (groupChild.children().size() > 0) {
					this.removeEmptyNodes(groupChild);
				}
				if (groupChild.children().size() == 0) {
					root.children().remove(iNode);
				} else {
					iNode++;
				}
			} else {
				iNode++;
			}
		}
	}

	private synchronized void resetConfiguration(String value) throws org.dbwiki.exception.WikiException {
		// The value is the parameter value of a ?reset=value request. The format
		// currently is expected to be <int>_<int>_<int> and these <int>'s are
		// layout file version, template file version, and style sheet file version.
		ConfigSetting setting = null;
		try {
			setting = new ConfigSetting(value);
		} catch (Exception exception) {
			throw new WikiFatalException(exception);
		}
		_server.resetWikiConfiguration(this, setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion());
	}
	
	private void respondToExportRequest(WikiDataRequest request, NodeWriter writer) throws org.dbwiki.exception.WikiException {
		int versionNumber = request.wiki().database().versionIndex().getLastVersion().number();
		if (request.parameters().hasParameter(RequestParameter.ParameterVersion)) {
			versionNumber = ((RequestParameterVersionSingle)RequestParameter.versionParameter(request.parameters().get(RequestParameter.ParameterVersion))).versionNumber();
		}
		
		try {
			if (request.isRootRequest()) {
				File tmpFile = File.createTempFile("dbwiki", "xml");
				BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
				writer.init(out);
				request.wiki().database().export(request.wri().resourceIdentifier(), versionNumber, writer);
				out.close();
				_server.sendXML(request.exchange(), new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				request.wiki().database().export(request.wri().resourceIdentifier(), versionNumber, writer);
				out.close();
				_server.sendXML(request.exchange(), new ByteArrayInputStream(buf.toString().getBytes("UTF-8")));
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

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
						request.wiki().database().annotate(request.wri().resourceIdentifier(), new Annotation(parameter.value(), new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new Date()), request.user()));
					}
				}
				// Only objects may be annotated, thus this is a get request
				isGetRequest = true;
			} else if (action.actionEntity()) {
				GroupEntity parent = null;
				if (!request.isRootRequest()) {
					parent = (GroupEntity)((DatabaseElementNode)_database.get(request.wri().resourceIdentifier())).entity();
				}
				_database.insertEntity(parent, request.parameters().get(ParameterEntityName).value(), Byte.parseByte(request.parameters().get(ParameterEntityType).value()), request.user());
				isGetRequest = !request.isRootRequest();
				isIndexRequest = ! isGetRequest;
			} else if (action.actionInsert()) {
				DocumentNode insertNode = this.getInsertNode(request);
				page = new RedirectPage(request, request.wiki().database().insertNode(request.wri().resourceIdentifier(), insertNode, request.user()));
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterWikiID)) {
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
						request.wiki().database().update(request.wri().resourceIdentifier(), update, request.user());
						if (request.node().isText()) {
							page = new RedirectPage(request, ((DatabaseTextNode)request.node()).parent().identifier());
						}
					}
					isGetRequest = true;
				}
			}
		} else if (request.type().isActivate()) {
			request.wiki().database().activate(request.wri().resourceIdentifier(), request.user());
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
			request.wiki().database().delete(request.wri().resourceIdentifier(), request.user());
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
			RequestContentGenerator contentGenerator = new RequestContentGenerator(this, request);
			if ((isGetRequest) || (request.type().isCopy())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentAnnotation, new ObjectAnnotationPrinter(request));
				//contentGenerator.put(DatabaseWikiContentGenerator.ContentProvenance, new VersionIndexPrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentProvenance, new ObjectProvenancePrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new DataValuePrinter(request, _layouter));
			} else if (isIndexRequest) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				if (IndexAZMultiPage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, request.wiki().database().content()));
				} else if (IndexAZSinglePage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, request.wiki().database().content()));
				} else if (IndexMultiColumn.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, request.wiki().database().content()));
				} else if (IndexPartialList.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, request.wiki().database().content()));
				} else {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FullIndexPrinter(request, request.wiki().database().content()));
				}
			} else if (request.type().isSearch()) {
				DatabaseContent content = null;
				String query = request.parameters().get(RequestParameter.ParameterSearch).value();
				if (query != null) {
					content = request.wiki().database().search(query);
				} else {
					content = request.wiki().database().content();
				}
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new DataMenuPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SearchResultPrinter(request, content));
			} else if ((request.type().isCreate()) || (request.type().isEdit())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new DataUpdateFormPrinter(request, _layouter));
			} else if (request.type().isCreateEntity()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new CreateEntityFormPrinter(request));
			} else if ((request.type().isTimemachineChanges()) || ((request.type().isTimemachinePrevious()))) {
				if (request.node() != null) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentObjectLink, new NodePathPrinter(request, _layouter));
				}
				contentGenerator.put(DatabaseWikiContentGenerator.ContentTimemachine, new TimemachinePrinter(request));
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new VersionIndexPrinter(request));
			} else if (request.type().isLayout()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new LayoutEditor(request));
			} else if (request.type().isPasteForm()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new InputFormPrinter(request, "Copy & Paste", "Insert source URL", RequestParameter.ParameterPaste, RequestParameter.ParameterURL));
			} else if (request.type().isStyleSheet()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit style sheet"));
			} else if (request.type().isTemplate()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit template"));
			} else if (request.type().isSettings()) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SettingsListingPrinter(request));
			} else {
				throw new WikiRequestException(WikiRequestException.InvalidRequest, request.exchange().getRequestURI().toASCIIString());
			}
			page = new HtmlTemplateDecorator().decorate(_template, contentGenerator);
		}
		
		// Send the resulting page to the user.
		HtmlSender.send(page,request.exchange());
	}

	private void respondToPageRequest(WikiPageRequest request) throws java.io.IOException, org.dbwiki.exception.WikiException {
		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();
		
		RequestParameterAction action = new RequestParameterActionCancel();
		if (request.type().isDelete()) {
			request.wiki().wiki().delete((PageIdentifier)request.wri().resourceIdentifier());
			HtmlSender.send(new RedirectPage(request.wri().databaseIdentifier().databaseHomepage()),request.exchange());
			return;
		} else if (request.type().isAction()) {
			action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			if (action.actionInsert()) {
				request.wiki().wiki().insert(this.getWikiPage(request), request.user());
			} else if (action.actionUpdate()) {
				if (request.parameters().hasParameter(ParameterWikiID)) {
					this.updateConfigurationFile(request);
					isGetRequest = !request.isRootRequest();
					isIndexRequest = !isGetRequest;
				} else {
					request.wiki().wiki().update(new PageIdentifier(request.parameters().get(RequestParameter.ActionValuePageID).value()), this.getWikiPage(request), request.user());
					isGetRequest = true;
					isIndexRequest = false;
				}
			}
		}
		
		RequestContentGenerator contentGenerator = new RequestContentGenerator(this, request);
		
		if (isGetRequest) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageContentPrinter(request, _layouter));
		} else if ((isIndexRequest) || (request.type().isDelete()) || (action.actionInsert())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			if (IndexAZMultiPage.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, request.wiki().wiki().content()));
			} else if (IndexAZSinglePage.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, request.wiki().wiki().content()));
			} else if (IndexMultiColumn.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, request.wiki().wiki().content()));
			} else if (IndexPartialList.equals(_layouter.indexType())) {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, request.wiki().wiki().content()));
			} else {
				contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FullIndexPrinter(request, request.wiki().wiki().content()));
			}
		} else if ((request.type().isCreate()) || (request.type().isEdit())) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageUpdateFormPrinter(request));
		} else if (request.type().isLayout()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new LayoutEditor(request));
		} else if (request.type().isStyleSheet()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit style sheet"));
		} else if (request.type().isTemplate()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new FileEditor(request, "Edit template"));
		} else if (request.type().isSettings()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new SettingsListingPrinter(request));
		} else if (request.type().isPageHistory()) {
			contentGenerator.put(DatabaseWikiContentGenerator.ContentMenu, new PageMenuPrinter(request));
			contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PageHistoryPrinter(request));
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidRequest, request.exchange().getRequestURI().toASCIIString());
		}
		HtmlSender.send(new HtmlTemplateDecorator().decorate(_template, contentGenerator),request.exchange());
	}
	
	private synchronized void updateConfigurationFile(WikiRequest request) throws org.dbwiki.exception.WikiException {
		int wikiID = Integer.valueOf(request.parameters().get(ParameterWikiID).value());
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
		}
	}
}
