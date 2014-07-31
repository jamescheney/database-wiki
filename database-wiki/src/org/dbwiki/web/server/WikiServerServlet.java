package org.dbwiki.web.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.web.WikiRequestException;
import org.dbwiki.user.User;
import org.dbwiki.web.html.FatalExceptionPage;
import org.dbwiki.web.html.FileNotFoundPage;
import org.dbwiki.web.html.RedirectPage;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;
import org.dbwiki.web.security.WikiServletAuthenticator;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.ServerResponseHandler;
import org.dbwiki.web.ui.printer.server.DatabaseWikiFormPrinter;

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
		_authenticator = new WikiServletAuthenticator(_authenticationMode, _users);
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
			WikiServletAuthenticator authenticator = new WikiServletAuthenticator(rs.getInt(RelDatabaseColAuthentication), _users);
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
		CreateDatabaseRecord r = new CreateDatabaseRecord(name, title, authenticationMode, autoSchemaChanges, databaseSchema, user);
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		try {
			
			wikiID = r.createDatabase(con, versionIndex);
			con.commit();
			WikiServletAuthenticator authenticator = new WikiServletAuthenticator(authenticationMode, _users);
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
				ImportHandler importHandler = database.createImportHandler(con);
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
		try {
			String path = request.getRequestURI();
			if (path.equals("/")) {
				if (_serverLog != null) {
					_serverLog.logRequest(request);
				}
				if(_authenticator.authenticate(request)) {
					this.respondTo(request, response);
				} else {
					response.setHeader("WWW-Authenticate", "Basic realm=\"/\"");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
				}
			} else if ((path.startsWith(SpecialFolderDatabaseWikiStyle + "/")) && (path.endsWith(".css"))) {
				this.sendCSSFile(path.substring(SpecialFolderDatabaseWikiStyle.length() + 1, path.length() - 4), path, response);
			} else if (path.equals(SpecialFolderLogin)) {
				//FIXME: #request This is a convoluted way of parsing the request parameter!
				if(_authenticator.authenticate(request)) {
					HtmlServletSender.send(new RedirectPage(new RequestURL(new ServletExchangeWrapper(request, response),"").parameters().get(RequestParameter.ParameterResource).value()), response);
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
					this.sendFile(path, response);
				}
			} else {
				this.sendFile(path, response);
			}
		} catch (Exception exception) {
			try {
				HtmlServletSender.send(new FatalExceptionPage(exception), response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Respond to a HttpServletRequest.
	 * First, parse the exchange into a ServerRequest and use its isX() methods 
	 * to figure out what is being requested.  
	 * In each case, construct an appropriate ServerResponseHandler.
	 * Then find the server template and decorate the template using the ServerResponseHandler
	 * This handles server-level requests only; DatabaseWiki-level requests are dispatched to the 
	 * DatabaseWiki object.  
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondTo(HttpServletRequest req, HttpServletResponse response) throws java.io.IOException, org.dbwiki.exception.WikiException {
		ServerResponseHandler responseHandler = null;
		
		HttpRequest request = new HttpRequest(new RequestURL(new ServletExchangeWrapper(req, response),""), users());

		if (request.type().isIndex()) {
			responseHandler = this.getHomepageResponseHandler(request);
		} else if (request.type().isCreate()) {
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Create Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter("Create Database Wiki"));
		} else if (request.type().isEdit()) {
			DatabaseWiki wiki = this.getRequestWiki(request, RequestParameter.ParameterEdit);
			responseHandler = new ServerResponseHandler(request, _wikiTitle + " - Edit Database Wiki");
			responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseWikiFormPrinter(wiki.getProperties(), RequestParameterAction.ActionUpdate, "Edit Database Wiki"));
		} else if (request.type().isReset()) {
			this.resetWikiConfiguration(this.getRequestWiki(request, RequestParameter.ParameterReset));
			responseHandler = this.getHomepageResponseHandler(request);
		} else if (request.type().isAction()) {
			RequestParameterAction action = RequestParameter.actionParameter(request.parameters().get(RequestParameter.ParameterAction));
			if (action.actionInsert()) {
				responseHandler = this.getInsertWikiResponseHandler(request);
			} else if (action.actionCancel()) {
				responseHandler = this.getHomepageResponseHandler(request);
			} else if (action.actionUpdate()) {
				responseHandler = this.getUpdateWikiResponseHandler(request);
			} else {
				throw new WikiRequestException(WikiRequestException.InvalidRequest, request.toString());
			}
		} else {
			throw new WikiRequestException(WikiRequestException.InvalidRequest, request.toString());
		}
		
		File template = null;

		//
		// TODO: Improve handling of individual home pages.
		//
		// This part is still a bit tricky. In order to identify whether the response handler results from
		// a call to getHomepageResponseHandler() we rely on the fact that only getHomepageResponseHandler()
		// adds content handler for Menu and Content.
		//
		if (responseHandler.contains(HtmlContentGenerator.ContentMenu) && responseHandler.contains(HtmlContentGenerator.ContentContent)) {
			template = _homepageTemplate;
		} else {
			template = _formTemplate;
		}

		HtmlServletSender.send(HtmlTemplateDecorator.decorate(new BufferedReader(new FileReader(template)), responseHandler), response);
	}
	
	/** Sends CSS file for the server.
	 * 
	 * @param name - name of the wiki CSS file, in format "wikiID_version"
	 * @param exchange - exchange to respond to
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void sendCSSFile(String name, String uri, HttpServletResponse response) throws java.io.IOException, org.dbwiki.exception.WikiException {
		int wikiID = -1;
		int fileVersion = -1;
		
		try {
			int pos = name.indexOf("_");
			wikiID = Integer.valueOf(name.substring(0, pos));
			fileVersion = Integer.valueOf(name.substring(pos + 1));
		} catch (Exception exception ) {
			HtmlServletSender.send(new FileNotFoundPage(uri), response);
			return;
		}
		
		String value = this.readConfigFile(wikiID, RelConfigFileColFileTypeValCSS, fileVersion);
		this.sendData(response, "text/css", new ByteArrayInputStream(value.getBytes("UTF-8")));
	}
		
	protected void sendData(HttpServletResponse response, String contentType, InputStream is) throws java.io.IOException {
		response.setContentType(contentType);
    	response.setStatus(HttpURLConnection.HTTP_OK);
		OutputStream os = response.getOutputStream();
		int n;
		byte[] buf = new byte[2048];
		while ((n = is.read(buf)) > 0) {
			os.write(buf, 0, n);
		}
		is.close();
	}
	
	// FIXME #security: Seems like a bad idea for the default behavior to be to send files from file sys...
	protected void sendFile(String uri, HttpServletResponse response) throws java.io.IOException {
		String contentType = this.contentType(uri);
		File file = new File(_directory.getAbsolutePath() + uri);
		if ((file.exists()) && (!file.isDirectory())) {
			this.sendData(response, contentType, new FileInputStream(file));
		} else {
			System.out.println("File Not Found: " + uri);
			HtmlServletSender.send(new FileNotFoundPage(uri), response);
		}
	}
	
	protected void sendXML(HttpServletResponse response, InputStream is) throws java.io.IOException {
		this.sendData(response, "application/xml", is);
	}
	
	protected void sendJSON(HttpServletResponse response, InputStream is) throws java.io.IOException {
		this.sendData(response, "application/json", is);
	}
		
	private String contentType(String filename) {
		int pos = filename.lastIndexOf('.');
		if (pos != -1) {
			String suffix = filename.substring(pos);
			if (suffix.equalsIgnoreCase(".uu")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".exe")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".ps")) {
				return "application/postscript";
			} else if (suffix.equalsIgnoreCase(".zip")) {
				return "application/zip";
			} else if (suffix.equalsIgnoreCase(".sh")) {
				return "application/x-shar";
			} else if (suffix.equalsIgnoreCase(".tar")) {
				return "application/x-tar";
			} else if (suffix.equalsIgnoreCase(".snd")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".au")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".wav")) {
				return "audio/x-wav";
			} else if (suffix.equalsIgnoreCase(".gif")) {
				return "image/gif";
			} else if (suffix.equalsIgnoreCase(".jpg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".jpeg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".htm")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".html")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".text")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".c")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".cc")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".css")) {
				return "text/css";
			} else if (suffix.equalsIgnoreCase(".c++")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".h")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".pl")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".txt")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".java")) {
				return "text/plain";
			} else {
				return "content/unknown";
			}
		} else {
			return "content/unknown";
		}
	}
}
