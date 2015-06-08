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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
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
import org.dbwiki.data.io.ExportJSONNodeWriter;
import org.dbwiki.data.io.ExportNodeWriter;
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.PageIdentifier;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.security.DBPolicy;
import org.dbwiki.data.security.SimplePolicy;
import org.dbwiki.data.wiki.Wiki;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.web.WikiRequestException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.html.RedirectPage;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.HttpRequest;
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
import org.dbwiki.web.server.DatabaseWikiProperties;


/** Implements the DatabaseWiki functionality for a given database. 
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseWiki implements Comparable<DatabaseWiki> {
	
	/*
	 * Public Constants
	 */
	public static final String ParameterSchemaNodeName = "schema_node_name";
	public static final String ParameterSchemaNodeType = "schema_node_type";
	public static final String ParameterFileContent    = "file_content";
	public static final String ParameterFileType       = "file_type";
	public static final String ParameterDatabaseID     = "database_id";


	
	/*
	 * Private Variables
	 */
	protected int _autoSchemaChanges;
	protected Database _database;
	protected int _id;
	protected DatabaseLayouter _layouter = null;
	protected String _name;
	protected String _template = null;
	protected String _title;
	protected Wiki _wiki;
	//protected int _authenticationMode;
	protected SimplePolicy _policy;
	// FIXME: Remove?
	protected DatabaseConnector _connector;
	
	private CSSLinePrinter _cssLinePrinter;
	private int _cssVersion;
	private int _templateVersion;
	private int _layoutVersion;
	private int _urlDecodingVersion;
	private URLDecodingRules _urlDecoder;
	
	/*
	 * Constructors
	 */
	
	
	/*
	 * Public Methods
	 */

	
	
	/** Comparator.  Compare database wikis by title, to sort list of wikis.
	 * 
	 */
 	public int compareTo(DatabaseWiki wiki) {
	 	return this.getTitle().compareTo(wiki.getTitle());
	}
	
	/* Getters
	 * 
	 */

 // TODO: Build properties directly, removing dependence of DatabaseWikiProperties on DatabaseWiki
 	public DatabaseWikiProperties getProperties() {
 		
 		return new DatabaseWikiProperties(this);
 		
 	}
 	
 	public SimplePolicy policy() {
 		return _policy;
 	}
 	
	public int getAuthenticationMode() {
		return _policy.getAuthenticationMode();
	}
	
	public int getAutoSchemaChanges() {
		return _autoSchemaChanges;
	}
	
	public CSSLinePrinter cssLinePrinter() {
		return _cssLinePrinter;
	}
	
	public URLDecodingRules urlDecoder() throws org.dbwiki.exception.WikiException {
		if (_urlDecoder == null) {
			_urlDecoder = new URLDecodingRules(_database.schema(), server().getURLDecoding(this, _urlDecodingVersion));
		}
		return _urlDecoder;
	}
	

	public Database database() {
		return _database;
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
			return server().getStyleSheet(this, _cssVersion);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValURLDecoding) {
			return server().getURLDecoding(this, _urlDecodingVersion);
		} else {
			throw new WikiFatalException("Unknown configuration file type");
		}
	}
	
	public String getTitle() {
		return _title;
	}
	
	public UserListing users() {
		return server().users();
	}
	
	public Wiki wiki() {
		return _wiki;
	}
	
	public abstract WikiServer server() ;
	
	/**
	 * The list of all previous display settings for this wiki. Contains only
     * the file version numbers, not the actual data.
	 * @return Vector&lt;ConfigSetting&gt;
	 * @throws org.dbwiki.exception.WikiException
	 */
	public Vector<ConfigSetting> listSettings() throws org.dbwiki.exception.WikiException {
		
		return server().listSettings(this);
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
		assert(autoSchemaChanges == DatabaseWikiProperties.AutoSchemaChangesNever 
				|| autoSchemaChanges == DatabaseWikiProperties.AutoSchemaChangesIgnore 
				|| autoSchemaChanges == DatabaseWikiProperties.AutoSchemaChangesAllow);
		_autoSchemaChanges = autoSchemaChanges;
	}

	public void setTitle(String value) {
		_title = value;
	}

	public void setAuthenticationMode(int authMode) {
		_policy.setAuthenticationMode(authMode);
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
		_template = server().getTemplate(this, _templateVersion);
		_cssLinePrinter = new CSSLinePrinter(this.id(), _cssVersion);
		_urlDecoder = null;
		_layouter = new DatabaseLayouter(server().getLayout(this, _layoutVersion));
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
	
	
	/*
	 * Private Methods
	 */
	
	
	/** Gets the document node associated with an insert POST request.
	 * 
	 * @param request
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	protected DocumentNode getInsertNode(WikiDataRequest  request) throws org.dbwiki.exception.WikiException {
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
	protected Update getNodeUpdates(WikiDataRequest  request) throws org.dbwiki.exception.WikiException {
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
	protected void pasteURL(WikiDataRequest  request, String url) throws org.dbwiki.exception.WikiException {
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
	protected synchronized void resetConfiguration(String value) throws org.dbwiki.exception.WikiException {
		
		ConfigSetting setting = null;
		try {
			setting = new ConfigSetting(value);
		} catch (Exception exception) {
			throw new WikiFatalException(exception);
		}
		server().resetWikiConfiguration(this, setting.getLayoutVersion(), setting.getTemplateVersion(), setting.getStyleSheetVersion(), setting.getURLDecodingRulesVersion());
	}
	
	// TODO: Merge XML and JSON export to avoid code duplication

	/**
	 * Handles data export requests (generating XML)
	 * 
	 * @param request
	 * @param writer
	 * @throws org.dbwiki.exception.WikiException
	 */
	protected void respondToExportXMLRequest(WikiDataRequest request,
			NodeWriter writer, Exchange<?> exchange)
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
				exchange.sendXML(new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				exchange.sendXML(new ByteArrayInputStream(buf
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
	protected void respondToExportJSONRequest(WikiDataRequest request,
			NodeWriter writer, Exchange<?> exchange)
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
				exchange.sendJSON(new FileInputStream(tmpFile));
				tmpFile.delete();
			} else {
				StringWriter buf = new StringWriter();
				BufferedWriter out = new BufferedWriter(buf);
				writer.init(out);
				database().export(request.wri().resourceIdentifier(),
						versionNumber, writer);
				out.close();
				exchange.sendJSON(new ByteArrayInputStream(buf
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
	 * @throws SQLException 
	 */
	protected void respondToDataRequest(WikiDataRequest request, 
			Exchange<?> exchange) throws java.io.IOException,
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
			String sourceURL = "http://localhost" + ":"
					+ exchange.getLocalPort()
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
			exchange.setResponseHeader("Set-Cookie", HttpRequest.CookiePropertyCopyBuffer + "=" + URLEncoder.encode(sourceURL, "UTF-8") + "; path=/; ");
		
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
				url = URLDecoder.decode(request.copyBuffer(), "UTF-8");
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
					new ExportJSONNodeWriter(), 
					exchange);
			return;
		}

		// If the request is not redirected (in case of INSERT or DELETE) then
		// assemble appropriate
		// HtmlContentGenerator.
		if (page == null) {
			DatabaseWikiContentGenerator contentGenerator = new DatabaseWikiContentGenerator(
					request, this.getTitle(), this.cssLinePrinter());
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
				if (DatabaseLayouter.IndexAZMultiPage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZMultiPageIndexPrinter(request, database().content()));
				} else if (DatabaseLayouter.IndexAZSinglePage.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new AZSinglePageIndexPrinter(request, database().content()));
				} else if (DatabaseLayouter.IndexMultiColumn.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new MultiColumnIndexPrinter(request, database().content()));
				} else if (DatabaseLayouter.IndexPartialList.equals(_layouter.indexType())) {
					contentGenerator.put(DatabaseWikiContentGenerator.ContentContent, new PartialIndexPrinter(request, database().content()));
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
						WikiRequestException.InvalidRequest, exchange.getRequestURI().toString());
			}
			page = HtmlTemplateDecorator.decorate(_template, contentGenerator);
		}

		// Send the resulting page to the user.
		exchange.send(page);
	}

	/**
	 * Responds to requests for wiki pages 
	 * TODO: Factor this out so that wiki pages are handled at server level.
	 * 
	 * @param request
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	protected void respondToPageRequest(WikiPageRequest request,
			Exchange<?> exchange) throws java.io.IOException,
			org.dbwiki.exception.WikiException {
		// Again, we need to further distinguish the request type
		// for .isAction() requests (see above).
		boolean isGetRequest = request.type().isGet();
		boolean isIndexRequest = request.type().isIndex();

		RequestParameterAction action = new RequestParameterActionCancel();
		if (request.type().isDelete()) {
			wiki().delete((PageIdentifier) request.wri().resourceIdentifier());
			exchange.send(new RedirectPage(request.wri().databaseIdentifier()
					.databaseHomepage()));
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
		exchange.send(HtmlTemplateDecorator.decorate(_template, contentGenerator));
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
	protected void respondToSchemaRequest(WikiSchemaRequest request,
			Exchange<?> exchange) throws java.io.IOException,
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
				exchange.send(new RedirectPage(request, 
						request.schema().parent().identifier()));
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

		exchange.send(HtmlTemplateDecorator.decorate(_template, contentGenerator));
	}

	
	/** Handles POST requests that provide a new version of a config file.
	 * 
	 * @param request
	 * @throws org.dbwiki.exception.WikiException
	 */
	  
	protected synchronized void updateConfigurationFile(WikiRequest  request) throws org.dbwiki.exception.WikiException {
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
			server().updateConfigFile(wikiID, fileType, value, request.user());
			_layouter = new DatabaseLayouter(value);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValTemplate) {
			server().updateConfigFile(wikiID, fileType, value, request.user());
			_template = value;
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValCSS) {
			_cssVersion = server().updateConfigFile(wikiID, fileType, value, request.user());
			_cssLinePrinter = new CSSLinePrinter(this.id(), _cssVersion);
		} else if (fileType == WikiServerConstants.RelConfigFileColFileTypeValURLDecoding) {
			_urlDecoder = new URLDecodingRules(_database.schema(), value);
			_urlDecodingVersion = server().updateConfigFile(wikiID, fileType, value, request.user());
		}
	}
	
	
	/**
	 * Get entry permissions of a user to a database DB from DB_policy table
	 * @param user_id the id of a user
	 * @return Map<Integer, Map<Integer,DBPolicy>>
	 */
	@Deprecated
	public Map<Integer,Map<Integer,DBPolicy>> getDBPolicyListing(int user_id) {
		
		Map<Integer,Map<Integer,DBPolicy>> policyListing = new HashMap<Integer,Map<Integer,DBPolicy>>();
		try{
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ _name + DatabaseConstants.RelationPolicy
					+" WHERE " + DatabaseConstants.RelPolicyUserID + " = "+user_id);
			System.out.println("SELECT * FROM "
					+ _name + DatabaseConstants.RelationPolicy
					+" WHERE " + DatabaseConstants.RelPolicyUserID + " = "+user_id);
			while (rs.next()) {
				if(policyListing.get(rs.getInt(DatabaseConstants.RelPolicyUserID))==null){
					Map<Integer,DBPolicy> map = new HashMap<Integer,DBPolicy>();
					map.put(rs.getInt(DatabaseConstants.RelPolicyEntry), new DBPolicy(rs
						.getInt(DatabaseConstants.RelPolicyUserID), rs
						.getInt(DatabaseConstants.RelPolicyEntry), rs
						.getBoolean(DatabaseConstants.RelPolicyRead), rs
						.getBoolean(DatabaseConstants.RelPolicyInsert), rs
						.getBoolean(DatabaseConstants.RelPolicyDelete), rs
						.getBoolean(DatabaseConstants.RelPolicyUpdate)));
					policyListing.put(rs.getInt(DatabaseConstants.RelPolicyUserID), map);
				} else {
					policyListing.get(rs.getInt(DatabaseConstants.RelPolicyUserID)).put(rs.getInt(DatabaseConstants.RelPolicyEntry), new DBPolicy(rs
						.getInt(DatabaseConstants.RelPolicyUserID), rs
						.getInt(DatabaseConstants.RelPolicyEntry), rs
						.getBoolean(DatabaseConstants.RelPolicyRead), rs
						.getBoolean(DatabaseConstants.RelPolicyInsert), rs
						.getBoolean(DatabaseConstants.RelPolicyDelete), rs
						.getBoolean(DatabaseConstants.RelPolicyUpdate)));
				}
			}
			rs.close();
			stmt.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return policyListing;
	}

	/**
	 * Get entry listing of a specific database in DBWiki
	 * @return Map<Integer, Entry>
	 * @throws SQLException
	 * @throws WikiException
	 * FIXME: #security This duplicates functionality in Database 
	 */
	/*
	@Deprecated
	public Map<Integer, Entry> getEntryListing()
			throws SQLException, WikiException {
		Map<Integer, Entry> entryListing = new HashMap<Integer, Entry>();
		Connection con = _connector.getConnection();
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT DISTINCT ss." + DatabaseConstants.RelDataColEntry + ", ss." + DatabaseConstants.RelDataColValue +
				" FROM " + _name + DatabaseConstants.RelationData + " ss "+
				" JOIN " + _name + DatabaseConstants.RelationData + " s "+
				" ON ss." + DatabaseConstants.RelDataColParent + " = s." + DatabaseConstants.RelDataColID +
				" JOIN " + _name + DatabaseConstants.RelationData + " p "+
				" ON s." + DatabaseConstants.RelDataColParent + " = p." + DatabaseConstants.RelDataColID +
				" WHERE p." + DatabaseConstants.RelSchemaColParent + " = -1" +
				" AND s." + DatabaseConstants.RelDataColTimesequence + " = -1" +
				" ORDER BY ss." + DatabaseConstants.RelDataColValue + " ASC");
		System.out.println("SELECT DISTINCT ss." + DatabaseConstants.RelDataColEntry + ", ss." + DatabaseConstants.RelDataColValue +
				" FROM " + _name + DatabaseConstants.RelationData + " ss "+
				" JOIN " + _name + DatabaseConstants.RelationData + " s "+
				" ON ss." + DatabaseConstants.RelDataColParent + " = s." + DatabaseConstants.RelDataColID +
				" JOIN " + _name + DatabaseConstants.RelationData + " p "+
				" ON s." + DatabaseConstants.RelDataColParent + " = p." + DatabaseConstants.RelDataColID +
				" WHERE p." + DatabaseConstants.RelSchemaColParent + " = -1" +
				" AND s." + DatabaseConstants.RelDataColTimesequence + " = -1" +
				" ORDER BY ss." + DatabaseConstants.RelDataColValue + " ASC");
		while (rs.next()) {
			if(rs.getString(DatabaseConstants.RelDataColValue)!= null){
			Entry entry = new Entry(rs.getInt(DatabaseConstants.RelDataColEntry), rs.getString(DatabaseConstants.RelDataColValue));
			entryListing.put(entry.entry_id(),entry);
			}else{
				break;
			}
		}
		rs.close();
		stmt.close();
		return entryListing;
	}
	*/
	
	/** The sorted entry ids */
	/*
	@Deprecated
	public ArrayList<Integer> getSortedKeys() throws SQLException, WikiException {
		Map<Integer, Entry> _entryListing = getEntryListing();
		ArrayList<Integer> keys = new ArrayList<Integer>(_entryListing.keySet());
		Collections.sort(keys);
		return keys;
	}
	*/

	@Deprecated
	protected void initializePolicy() {
		try{
			Connection con = _connector.getConnection();
			con.setAutoCommit(false);
			_policy.getDBPolicyListing(con, this);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
