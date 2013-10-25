package org.dbwiki.appengine;

import com.google.appengine.api.rdbms.AppEngineDriver; //the same     

import java.util.logging.Logger; //for writting logs to the administration console

import java.io.File; 
import java.io.FileInputStream;
import java.io.IOException; //the same
import java.io.InputStream;
import java.io.PrintWriter; //the same

import java.net.URL;

import java.sql.Connection;  //in GuestbookSQLServlet: import com.google.cloud.sql.jdbc.Connection;
import java.sql.DriverManager; //the same
import java.sql.PreparedStatement; //the same
import java.sql.SQLException; //the same

import java.util.Properties;
import java.util.zip.GZIPInputStream;

//import javax.servlet.http.*; instead ...
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//...

//imports of the dbwiki ...
import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.StructureParser;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.schema.DatabaseSchema;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseReader;
import org.dbwiki.driver.rdbms.MySQLDatabaseConnector; //we connect with the MySQL local backend of the dbwiki instead of PostgreSQL
import org.dbwiki.driver.rdbms.RDBMSDatabase;

import org.dbwiki.user.User;
//...
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.server.WikiServerAppEngine;




@SuppressWarnings("serial")
public class DatabaseWikiAppEngineServlet extends HttpServlet { //the same extension

	private static final Logger log = Logger.getLogger(DatabaseWikiAppEngineServlet.class.getName()); //write logs identified by this class

	User testuser = new User(1, "admin", "Admin", "admin" );

	/** Something to add to avoid code duplication in the rest of the methods in this class
	private Connection getConnection ( String url ){

		Connection c = null;
		try{

        // String db_url= "jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"; 
		// DatabaseConnector con = new MySQLDatabaseConnector(db_url , "", "");
		//	con.getConnection()
		// the three above lines do the same thing as the two following lines. The only exception
		// is that the three lines get a connection via the DatabaseConnector, while the following
		// two lines are doing it directly via the JDBC interface. The second one must be faster.  
		DriverManager.registerDriver( new AppEngineDriver() );
		c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki");

		}catch(SQLException e) {
			e.printStackTrace();
		}
		return c;
	}
	 **/

	/**
	 * The server part
	 * Starts here----
	 */
	//drop the server
	public void doDrop( DatabaseConnector connector, PrintWriter out ) {

		try {
			connector.dropServer(true);
			out.print("Dropped server; redirecting...");
		} catch (Exception  e) {
			out.print(e);
		}
	}

	//create a new server
	public void doCreate( DatabaseConnector connector, PrintWriter out ) {

		try {	
			connector.createServer( new File("users") );
			out.print("Created server; redirecting...");
		} catch (Exception  e) {
			out.print(e);
		}

	}
	/**
	 * The server part
	 * ----Ends here
	 */


	/**
	 * The database part
	 * Starts here----
	 */
	//create a new database with name and title taken from the databases.jsp
	public void doCreateDB( String name, String title, PrintWriter out ) {

		long start = System.currentTimeMillis(); //new --for evaluation
		Connection c = null;		
		try {

			String db_url= "jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"; //the url for the database to be created
			DatabaseConnector con = new MySQLDatabaseConnector(db_url , "", "");  
			con.createDatabase( con.getConnection(), name, testuser ); //create the new database and the connection to it via the url

			//then write the name and the title to the _database table in the dbwiki
			//.....
			DriverManager.registerDriver( new AppEngineDriver() );
			c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"); //perhaps change the database from dbwiki to _database?

			String statement = "INSERT INTO _database (name, title, authentication, auto_schema_changes, uid) VALUES( ?, ?, 1, 1, 0 )"; //not successful solution to add authentication field and set value 1 to it
			PreparedStatement stmt = c.prepareStatement( statement );
			stmt.setString(1, name);
			stmt.setString(2, title);
			//.....
			int success = stmt.executeUpdate();
			long end = System.currentTimeMillis(); //new --for evaluation
			log.info("Creating the database needed: " + (end-start) +" ms in order to get completed"); //new --for evaluation

			if(success == 1) {
				out.println("Success! The database has been created. \n Redirecting in 3 seconds...");
			} else if (success == 0) {
				out.println("Failure! Please try again! Redirecting in 3 seconds...");
			}
		} catch ( Exception e ) {
			out.print(e);
		}
		try {
			if( c!=null)
				c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	


	//drop a database with name take from databases.jsp which can be found in _databases table
	public void doDropDB( String name, PrintWriter out ) {

		Connection c = null;		
		try {

			String db_url= "jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"; //the url for the database to be created
			DatabaseConnector con = new MySQLDatabaseConnector(db_url , "", "");  
			con.dropDatabase( con.getConnection(), name ); //drop a database and the connection to it via the url

			//then delete the entry of this database from the _database table in the dbwiki
			//.....
			DriverManager.registerDriver( new AppEngineDriver() );
			c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"); //perhaps change the database from dbwiki to _database?

			String statement = "DELETE FROM _database WHERE name=?";
			PreparedStatement stmt = c.prepareStatement( statement );
			stmt.setString(1, name);
			//.....
			int success = stmt.executeUpdate();
			log.info("Log message: The deletion of DB was finished.");
			if(success == 1) {
				out.println("Success! The database has been deleted. \n Redirecting in 3 seconds...");
			} else if (success == 0) {
				out.println("Failure! Please try again! Redirecting in 3 seconds...");
			}
		} catch ( Exception e ) {
			out.print(e);
		}
		try {
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * The database part
	 * ----Ends here
	 */


	/**
	 * The user part
	 * Starts here-------
	 */	
	//adds a new user to the system
	public void doAdduser(String name, String username, String password, String password2, PrintWriter out) {

		Connection c = null;
		try {
			//Class.forName("com.google.appengine.api.rdbms.AppEngineDriver");
			DriverManager.registerDriver(new AppEngineDriver()); //register a new driver in DriverManager for Google Cloud SQL
			c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki"); //the DriverManager attempts to establish a connection with Google Cloud SQL using the appropriate url for this database (dbwiki)

			// TODO: Check validity
			//checks on the validity of username and password the user provided to the system ...
			if (name.equals("") || username.equals("") || password.equals("") || password2.equals("")) {
				out.println("Username, name, and password must not be empty. Try again! Redirecting in 3 seconds...");
			} else if (!password.equals(password2)) {
				out.println("Passwords must match. Try again! Redirecting in 3 seconds...");
			} else {
				//...
				String statement ="INSERT INTO _user (full_name,login,password) VALUES( ? , ?, ? )"; //we create a statement which inserts the data into the table _user of the database dbwiki. this string will be used in next line as a prepared statement
				PreparedStatement stmt = c.prepareStatement( statement ); //create the prepared statement
				stmt.setString( 1, name ); //allocates the value of the name parameter to full_name
				stmt.setString( 2, username ); //allocates the value of the username parameter to login
				stmt.setString( 3, password ); //allocates the value of the password parameter to password

				int success = stmt.executeUpdate();
				if(success == 1) {
					out.println("Success! The user has been added. \n Redirecting in 3 seconds...");
				} else if (success == 0) {
					out.println("Failure! Please try again! Redirecting in 3 seconds...");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} /*catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/ finally {
			if (c != null) { 
				try {
					c.close(); //close the connection and release resources of Connection and JDBC driver
				} catch (SQLException ignore) {
				}
			}
		}
	}

	//delete a user from the users database _user --new
	public void doDropUser( int id, PrintWriter out ) {

		//Integer ident = id;
		Connection c = null;
		try {
			DriverManager.registerDriver( new AppEngineDriver() );
			c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki");

			if( id != 0 ){				
				String statement = "DELETE FROM _user WHERE id = ?";
				PreparedStatement stmt = c.prepareStatement( statement );
				stmt.setInt( 1, id );

				int success = stmt.executeUpdate();
				if(success == 1) {
					out.println("Success! The user has been removed. \n Redirecting in 3 seconds...");
				} else if (success == 0) {
					out.println("Failure! Please try again! Redirecting in 3 seconds...");
				}
			} else 
				out.print("Please define the id of the user");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if( c != null )
				try {
					c.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}	
	/**
	 *The user part
	 *------Ends here 
	 */


	/**
	 * The data processing part starts here----
	 */
	//import xml file with data into a database
	public void doImportData( String name, String title, String path, String inputName, PrintWriter out ){

		long start=0L; //new--for evaluation
		//long start = System.currentTimeMillis(); //new --for evaluation
		//define the path where the configuration file is (war/config for the app engine dbwiki)
		String configPath = "config";
		//take the configuration file by creating a new file representing the given configuration file
		File configFile = new File( configPath );

		String user = "Admin"; //i gave a default user
		//log.info("Log message: First stage.");.................................
		try {
			//load the properties which can be found in the configuration file
			//Properties properties = org.dbwiki.lib.IO.loadProperties( configFile ); caused error in GAE because of the loadProperties method
			Properties properties = new Properties();//new
			FileInputStream fin = new FileInputStream( configFile );//new

			properties.load( fin );//new

			fin.close();//new

			//create a new wiki server which will use these properties
			//WikiServer server = new WikiServerStandalone( properties, log );
			WikiServer server = new WikiServerAppEngine( properties );

			// attempt to generate a schema from the input file
			// 1. get input file stream
			InputStream in = null;

			//URL inputURL = new File(inputName).toURI().toURL(); caused an error of concatenating the url with the file name used for importing
			URL inputURL = new URL(inputName); //new

			if ( inputName.endsWith(".gz") ) {
				in = new GZIPInputStream(inputURL.openStream());
			} else {
				in = inputURL.openStream();
			}

			// 2.  parse to infer schema
			StructureParser structureParser = new StructureParser();
			new SAXCallbackInputHandler(structureParser, false).parse(in, false, false);
			in.close();

			if ( structureParser.hasException() ) {
				throw structureParser.getException();
			}
			DatabaseSchema databaseSchema = structureParser.getDatabaseSchema( path );

			//new
			if ( server.get(name) != null ){
				start = System.currentTimeMillis(); //new--for evaluation
				server.importData( name, title, path, inputURL, databaseSchema, server.users().get(user), 1, 0);
			}else{
				// register the database with the server
				server.registerDatabase( name, title, path, inputURL, databaseSchema, server.users().get(user), 1, 0, log );
			}
			long end = System.currentTimeMillis(); //new --for evaluation
			log.info("Importing needed: " + (end-start) +" ms in order to get completed"); //new --for evaluation
			//log.info("Log message: Final stage.");....................................
			out.println("The data was successfully imported in database: " + name); 
		} catch (Exception exception) {
			exception.printStackTrace();
			//System.exit(0); //system calls like exit() are not supported by GAE
		}

	}

	/**
	 * Create a database and import data into 
	 * it in a standalone dbwiki style
	 * @param name
	 * @param title
	 * @param path
	 * @param inputName
	 * @param out
	 */
	public void doCreateAndImport( String name, String title, String path, String inputName, PrintWriter out ){

		String configPath = "config";
		File configFile = new File( configPath );
		String user = "Admin"; //a default user

		try {
			Properties properties = new Properties();
			FileInputStream fin = new FileInputStream( configFile );
			properties.load( fin );
			fin.close();

			WikiServer server = new WikiServerAppEngine( properties );
			// 1. get input file stream
			InputStream in = null;
			URL inputURL = new URL(inputName); 

			if ( inputName.endsWith(".gz") ) {
				in = new GZIPInputStream(inputURL.openStream());
			} else {
				in = inputURL.openStream();
			}
			// 2.  parse to infer schema
			StructureParser structureParser = new StructureParser();
			new SAXCallbackInputHandler(structureParser, false).parse(in, false, false);
			in.close();
			if ( structureParser.hasException() ) {
				log.info("optional error: " + structureParser.getException());
				throw structureParser.getException();
			}
			DatabaseSchema databaseSchema = structureParser.getDatabaseSchema( path );
			// register the database with the server
			server.registerDatabase( name, title, path, inputURL, databaseSchema, server.users().get(user), 1, 0, log );

			out.println("The data was successfully imported in database: " + name); 
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Execute query given in query.jsp
	 * @param name
	 * @param query
	 * @param con
	 * @param out
	 */
	public void doExecuteQuery( String name, String query, DatabaseConnector con, PrintWriter out ){

		String configPath = "config";
		File configFile = new File( configPath );		
		try{

			Properties properties = new Properties();
			FileInputStream fin = new FileInputStream( configFile );
			properties.load( fin );
			fin.close();

			WikiServer server = new WikiServerAppEngine( properties );
			RDBMSDatabase database= new RDBMSDatabase(server.get(name), con);
			log.info(" the rdb is: " +database.name()); //NEW--FOR TESTING--DELETE
			NodeIdentifier nodeidentifier= new NodeIdentifier();
			DatabaseReader.get(con.getConnection(), database, nodeidentifier);
			//long start1 = System.currentTimeMillis(); //for evaluation
			//QueryResultSet result = database.query(query);
			//database.query( query, log ); //NEW--FOR TESTING --DELETE log
			long start1 = System.currentTimeMillis();
			database.query("nid://D1");
			long end1 = System.currentTimeMillis(); //for evaluation
			long start2 = System.currentTimeMillis();
			database.query("nid://197C");
			long end2 = System.currentTimeMillis();
			long start3 = System.currentTimeMillis();
			database.query("nid://2EEA");
			long end3 = System.currentTimeMillis();
			long start4 = System.currentTimeMillis();
			database.query("nid://7D01");
			long end4 = System.currentTimeMillis();
			long start5 = System.currentTimeMillis();
			database.query("nid://987A");
			long end5 = System.currentTimeMillis();
			log.info("Query 1 was executed in: " + (end1-start1) + " ms"); //for evaluation
			log.info("Query 2 was executed in: " + (end2-start2) + " ms"); //for evaluation
			log.info("Query 3 was executed in: " + (end3-start3) + " ms"); //for evaluation
			log.info("Query 4 was executed in: " + (end4-start4) + " ms"); //for evaluation
			log.info("Query 5 (987A) was executed in: " + (end5-start5) + " ms"); //for evaluation
			//long end = System.currentTimeMillis(); //for evaluation
			//log.info("The query was executed in: " + (end-start) + " ms"); //for evaluation
			//System.out.println(server.size());
			//for( int iResult = 0; iResult <= result.size(); iResult++){
			//	System.out.println(result.get(iResult));
			//}
			out.println("The query has successfully been executed.");

		}catch( Exception exception){
			exception.printStackTrace();
		}
	}


	//gets (probably only by view) data  from the database--at least it should return sth more meaningful that this...
	public void doGet( HttpServletRequest req, HttpServletResponse resp )
			throws IOException {
		PrintWriter out = resp.getWriter();
		out.print("<html><head><title>DBWiki test</title></head><body>");
		out.print("Redirecting...");		
		out.print("</body></html>");
		resp.setHeader("Refresh","3; url=/server.jsp");

	}

	/**
	 * Some kind of a main method, allocating 
	 * actions to respective methods
	 */
	//sends/posts data to the database
	@Override
	public void doPost( HttpServletRequest req, HttpServletResponse resp )
			throws IOException {

		DatabaseConnector connector = new MySQLDatabaseConnector("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki", "", ""); //why MySQLDatabaseConnector and not the way done in doAddUser
		PrintWriter out = resp.getWriter(); //for printing messages on the web page
		out.print("<html><head><title>DBWiki test</title></head><body>");

		if( req.getServletPath().equals("/drop") ) {
			doDrop(connector,out);
		}
		else if ( req.getServletPath().equals("/create") ) {
			doCreate(connector,out);
		}
		else if ( req.getServletPath().equals("/adduser") ) {
			String name = req.getParameter("name");
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String password2 = req.getParameter("password2");
			doAdduser( name,username,password,password2,out );
		} 
		else if( req.getServletPath().equals("/dropuser") ) {
			int id = Integer.parseInt( req.getParameter("id") );
			doDropUser( id, out );
		}
		else if( req.getServletPath().equals("/adddatabase") ) {
			String name = req.getParameter("name");
			String title = req.getParameter("title");
			doCreateDB( name, title, out);
		}
		else if( req.getServletPath().equals("/dropdatabase") ){
			String name = req.getParameter("name");
			doDropDB( name, out );
		}
		else if( req.getServletPath().equals("/importData")){
			String name = req.getParameter("name");
			String inputName = req.getParameter("file");
			String path = req.getParameter("path");
			String title = req.getParameter("title");
			doImportData( name, title, path, inputName, out  );
		}
		else if( req.getServletPath().equals("/createDatabase")){
			String name = req.getParameter("name");
			String title = req.getParameter("title");
			String inputName = req.getParameter("file");
			String path = req.getParameter("path");
			doCreateAndImport( name, title, path, inputName, out );
		}
		else if( req.getServletPath().equals("/executeQuery")){
			String query = req.getParameter("query");
			String name = req.getParameter("name");
			doExecuteQuery( name, query, connector, out );
		}
		out.print("</body></html>");

		resp.setHeader("Refresh","3; url=/server.jsp");
	}
}
