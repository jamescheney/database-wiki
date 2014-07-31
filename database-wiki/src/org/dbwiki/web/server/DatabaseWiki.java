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

import java.io.StringWriter;

import java.net.URL;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.NodeUpdate;
import org.dbwiki.data.database.Update;

import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.io.CopyPasteInputHandler;
import org.dbwiki.data.io.SAXCallbackInputHandler;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.data.wiki.Wiki;
import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.exception.web.WikiRequestException;

import org.dbwiki.user.UserListing;

import org.dbwiki.web.request.URLDecodingRules;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.request.WikiRequest;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.ui.layout.DatabaseLayouter;

import org.dbwiki.web.ui.printer.CSSLinePrinter;
import org.dbwiki.web.ui.printer.server.DatabaseWikiProperties;


/** Implements the DatabaseWiki functionality for a given database. 
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseWiki implements Comparable<DatabaseWiki> {
	/*
	 * Public Constants
	 */
	// TODO: These belong elsewhere.
	
	// Parameter values that indicate whether automatic schema
	// changes are allowed when importing (copy/paste, merge)
	// data
	public static final int AutoSchemaChangesAllow  = 0;
	public static final int AutoSchemaChangesIgnore = 1;
	public static final int AutoSchemaChangesNever  = 2;
	
	public static final int AuthenticateAlways = 0;
	public static final int AuthenticateNever = 1;
	public static final int AuthenticateWriteOnly = 2;
	
	
	
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
	//protected WikiServer _server;
	protected String _template = null;
	protected String _title;
	protected Wiki _wiki;
	protected int _authenticationMode;
	
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

	
	// TODO: Build properties directly, removing dependence of DatabaseWikiProperties on DatabaseWiki
	public DatabaseWikiProperties getProperties() {
		
		return new DatabaseWikiProperties(this);
		
	}
	/** Comparator.  Compare database wikis by title, to sort list of wikis.
	 * 
	 */
 	public int compareTo(DatabaseWiki wiki) {
	 	return this.getTitle().compareTo(wiki.getTitle());
	}
	
	/* Getters
	 * 
	 */

	public int getAuthenticationMode() {
		return _authenticationMode;
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
	
	// TODO: #delete
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
		assert(autoSchemaChanges == AutoSchemaChangesNever || autoSchemaChanges == AutoSchemaChangesIgnore || autoSchemaChanges == AutoSchemaChangesAllow);
		_autoSchemaChanges = autoSchemaChanges;
	}

	public void setTitle(String value) {
		_title = value;
	}
	
	public void setAuthenticationMode(int authMode) {
		_authenticationMode = authMode;
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
}
