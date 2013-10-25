package org.dbwiki.web.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;

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
import org.dbwiki.web.security.WikiAuthenticator;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.ServerResponseHandler;
import org.dbwiki.web.ui.printer.server.DatabaseWikiFormPrinter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WikiServerHttpHandler extends WikiServer implements HttpHandler {



	private Vector<DatabaseWikiHttpHandler> _wikiListing;
	protected HttpServer _webServer = null;
	protected int _port;
	protected int _threadCount;
	protected int _backlog;
	/*
	 * Constructors
	 */
	
	
	public WikiServerHttpHandler(Properties properties) throws WikiException {
		super(properties);
		// Web Server Properties
		_backlog = Integer.parseInt(properties.getProperty(propertyBacklog));
		_port = Integer.parseInt(properties.getProperty(propertyPort));
		_threadCount = Integer.parseInt(properties.getProperty(propertyThreadCount));
	}
	
	
	/** 
	 * Initialize list of DatabaseWikis from database
	 * @param con
	 * @throws SQLException
	 * @throws WikiException
	 */
	protected void getWikiListing (Connection con) throws SQLException, WikiException {
		_wikiListing = new Vector<DatabaseWikiHttpHandler>();
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
			WikiAuthenticator authenticator = new WikiAuthenticator("/" + name, rs.getInt(RelDatabaseColAuthentication), _users);
			int autoSchemaChanges = rs.getInt(RelDatabaseColAutoSchemaChanges);
			ConfigSetting setting = new ConfigSetting(layoutVersion, templateVersion, styleSheetVersion, urlDecodingVersion);
			_wikiListing.add(new DatabaseWikiHttpHandler(id, name, title, authenticator, autoSchemaChanges, setting, _connector, this));
		}
		rs.close();
		stmt.close();
	}
	
	/*
	 * Getters
	 */
	
	/** Get the DatabaseWiki with index i */
	public DatabaseWiki get(int index) {
		return _wikiListing.get(index);
	}
	
	/**
	 * 
	 * @return The number of DatabaseWikis
	 */
	public int size() {
		return _wikiListing.size();
	}

	
	
	/** Starts the web server and creates an individual handler
	  *	 for each of the current wikis.
	  * 
	  * @throws java.io.IOException
	  */
	public void start() throws java.io.IOException {
		_webServer = HttpServer.create(new InetSocketAddress(_port), _backlog);
		_webServer.setExecutor(Executors.newFixedThreadPool(_threadCount));

		HttpContext context = _webServer.createContext("/", this);
		context.setAuthenticator(new WikiAuthenticator("/", _authenticationMode, _users));

		for (int iWiki = 0; iWiki < _wikiListing.size(); iWiki++) {
			DatabaseWikiHttpHandler wiki = _wikiListing.get(iWiki);
			context = _webServer.createContext("/" + wiki.name(), wiki);
			context.setAuthenticator(wiki.authenticator());
		}
				
		System.out.println("START SERVER ON ADDRESS " + _webServer.getAddress() + " AT " + new java.util.Date().toString());

		_webServer.start();
	}
	
	
	/** Creates new database with a given schema and import given data into it
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
		CreateDatabaseRecord r = new CreateDatabaseRecord(name,title,authenticationMode,autoSchemaChanges,databaseSchema,user);
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		try {
			/*_connector.createDatabase(con, name, databaseSchema, user, versionIndex);

			PreparedStatement pStmt = con.prepareStatement(
					"INSERT INTO " + RelationDatabase + "(" +
					RelDatabaseColName + ", " + RelDatabaseColTitle + ", " + 
					RelDatabaseColAuthentication + ", " + RelDatabaseColAutoSchemaChanges + ", " +
					RelDatabaseColUser + ") VALUES(? , ? , ? , ? , ?)", Statement.RETURN_GENERATED_KEYS);
			pStmt.setString(1, name);
			pStmt.setString(2, title);
			pStmt.setInt(3, authenticationMode);
			pStmt.setInt(4, autoSchemaChanges);
			if (user != null) {
				pStmt.setInt(5, user.id());
			} else {
				pStmt.setInt(5, User.UnknownUserID);
			}
			pStmt.execute();
			ResultSet rs = pStmt.getGeneratedKeys();
			if (rs.next()) {
				wikiID = rs.getInt(1);
			} else {
				throw new WikiFatalException("There are no generated keys.");
			}
			rs.close();
			*/
			wikiID = r.createDatabase(con, versionIndex);
			con.commit();

			WikiAuthenticator authenticator = new WikiAuthenticator("/" + name, authenticationMode, _users);
			DatabaseWikiHttpHandler wiki = new DatabaseWikiHttpHandler(wikiID, name, title, authenticator, autoSchemaChanges, _connector, this,
									con, versionIndex);

			String realm = wiki.database().identifier().databaseHomepage();
			// HACK: for command line imports _webserver may be null
			if(_webServer != null) {
				HttpContext context = _webServer.createContext(realm, wiki);
				context.setAuthenticator(authenticator);
			}
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
	
	/** Implements HttpHandler.handle() which is called by the Web Server
	 * for every browser request (Note that requests for the individual
	 * wikis are handled by DatabaseWiki.handle())
	 * The HttpExchange is side-effected and the response part is eventually sent back to the client.
	 */
	public void handle(HttpExchange exchange) throws java.io.IOException {
		
		try {
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/")) {
				if (_serverLog != null) {
					_serverLog.logRequest(exchange.getRequestURI(),exchange.getRemoteAddress(),exchange.getResponseHeaders());
				}
				this.respondTo(exchange);
			} else if ((path.startsWith(SpecialFolderDatabaseWikiStyle + "/")) && (path.endsWith(".css"))) {
	    		this.sendCSSFile(path.substring(SpecialFolderDatabaseWikiStyle.length() + 1, path.length() - 4), exchange);
			} else if (path.equals(SpecialFolderLogin)) {
				//FIXME: #request This is a convoluted way of parsing the request parameter!
				HtmlSender.send(new RedirectPage(new RequestURL( new HttpExchangeWrapper(exchange),"").parameters().get(RequestParameter.ParameterResource).value()),exchange);
	    	// The following code is necessary if using only a single HttpContext
	    	// instead of multiple ones (i.e., one per Database Wiki).
	    	//} else if (path.length() > 1) {
	    	//	int pos = path.indexOf('/', 1);
	    	//	DatabaseWiki wiki = null;
	    	//	if (pos != -1) {
	    	//		wiki = this.get(path.substring(1, pos));
	    	//	} else {
	    	//		wiki = this.get(path.substring(1));
	    	//	}
	    	//	if (wiki != null) {
	    	//		wiki.handle(exchange);
	    	//	} else {
		    //		this.sendFile(exchange);
	    	//	}
			} else {
	    		this.sendFile(exchange);
	    	}
		} catch (Exception exception) {
			HtmlSender.send(new FatalExceptionPage(exception),exchange);
		}
	}
	
	
	/** Respond to a HttpExchange.
	 * First, parse the exchange into a ServerRequest and use its isX() methods 
	 * to figure out what is being requested.  
	 * In each case, construct an appropriate ServerResponseHandler.
	 * Then find the server template and decorate the template using the ServerResponseHandler
	 * This handles server-level requests only; DatabaseWiki-level requests are dispatched to the 
	 * DatabaseWiki object.  
	 * @param exchange
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void respondTo(HttpExchange exchange) throws java.io.IOException, org.dbwiki.exception.WikiException {
		ServerResponseHandler responseHandler = null;
		
		HttpRequest request = new HttpRequest(new RequestURL(new HttpExchangeWrapper(exchange),""), users());

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

		HtmlSender.send(HtmlTemplateDecorator.decorate(new BufferedReader(new FileReader(template)), responseHandler),exchange);
	}
	
	/** Sends CSS file for the server.
	 * 
	 * @param name - name of the wiki CSS file, in format "wikiID_version"
	 * @param exchange - exchange to respond to
	 * @throws java.io.IOException
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void sendCSSFile(String name, HttpExchange exchange) throws java.io.IOException, org.dbwiki.exception.WikiException {
		int wikiID = -1;
		int fileVersion = -1;
		
		try {
			int pos = name.indexOf("_");
			wikiID = Integer.valueOf(name.substring(0, pos));
			fileVersion = Integer.valueOf(name.substring(pos + 1));
		} catch (Exception exception ) {
			HtmlSender.send(new FileNotFoundPage(exchange.getRequestURI().getPath()),exchange);
			return;
		}
		
		String value = this.readConfigFile(wikiID, RelConfigFileColFileTypeValCSS, fileVersion);
		this.sendData(exchange, "text/css", new ByteArrayInputStream(value.getBytes("UTF-8")));
	}

	
/* From FileServer */
	


	
	private String contentType(HttpExchange exchange) {
		String filename = exchange.getRequestURI().getPath();
		
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
	protected void sendData(HttpExchange exchange, String contentType, InputStream is) throws java.io.IOException {
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", contentType);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
		OutputStream os = exchange.getResponseBody();
		int n;
		byte[] buf = new byte[2048];
		while ((n = is.read(buf)) > 0) {
			os.write(buf, 0, n);
		}
		is.close();
		os.close();
	}
	
	// FIXME #security: Seems like a bad idea for the default behavior to be to send files from file sys...
	protected void sendFile(HttpExchange exchange) throws java.io.IOException {
		String contentType = this.contentType(exchange);
		
		String path = exchange.getRequestURI().getPath();
		
		File file = new File(_directory.getAbsolutePath() + path);
		if ((file.exists()) && (!file.isDirectory())) {
			this.sendData(exchange, contentType, new FileInputStream(file));
		} else {
			System.out.println("File Not Found: " + path);
			HtmlSender.send(new FileNotFoundPage(path),exchange);
		}
	}
	
	
	protected void sendXML(HttpExchange exchange, InputStream is) throws java.io.IOException {
		this.sendData(exchange, "application/xml", is);
	}
	
	protected void sendJSON(HttpExchange exchange, InputStream is) throws java.io.IOException {
		this.sendData(exchange, "application/json", is);
	}


	//new
	@Override
	public void importData(String name, String title, String path,
			URL resource, DatabaseSchema databaseSchema, User user,
			int authenticationMode, int autoSchemaChanges)
			throws WikiException, SQLException {
		// TODO Auto-generated method stub
		
	}


	/**
	@Override
	public void importData(String name, String title, String path,
			URL resource, DatabaseSchema databaseSchema, User user,
			int authenticationMode, int autoSchemaChanges)
			throws WikiException, SQLException {
		// TODO Auto-generated method stub
		
	}
	**/
			
}
