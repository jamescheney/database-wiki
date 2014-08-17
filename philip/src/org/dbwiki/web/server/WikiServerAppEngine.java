package org.dbwiki.web.server;

import java.net.URL; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
//import java.util.logging.Logger;

import org.dbwiki.appengine.DatabaseWikiAppEngineServlet;
import org.dbwiki.data.database.Database;
import org.dbwiki.data.io.ImportHandler;
import org.dbwiki.data.io.XMLDocumentImportReader;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.driver.rdbms.SQLVersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;

public class WikiServerAppEngine extends WikiServer {

	
	private Vector<DatabaseWikiAppEngine> _wikiListing;
	
	
	/*
	 * Constructors
	 */
	public WikiServerAppEngine(Properties properties) throws org.dbwiki.exception.WikiException {
		super(properties);

	}
	
	/**
	public WikiServerStandalone(Properties properties, Logger log) throws org.dbwiki.exception.WikiException {
		super(properties, log);

	}
	*/
	
	/** 
	 * Initialize list of DatabaseWikis from database
	 * @param con
	 * @throws SQLException
	 * @throws WikiException
	 */
	protected void getWikiListing (Connection con) throws SQLException, WikiException {
		_wikiListing = new Vector<DatabaseWikiAppEngine>();
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
			int authenticationMode = rs.getInt(RelDatabaseColAuthentication);
			int autoSchemaChanges = rs.getInt(RelDatabaseColAutoSchemaChanges);
			ConfigSetting setting = new ConfigSetting(layoutVersion, templateVersion, styleSheetVersion, urlDecodingVersion);
			_wikiListing.add(new DatabaseWikiAppEngine(id, name, title, authenticationMode, autoSchemaChanges, setting, _connector, this));
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
	public void registerDatabase(String name, String title, String path, URL resource, DatabaseSchema databaseSchema, User user, int authenticationMode, int autoSchemaChanges, Logger log )
		throws NumberFormatException, WikiException, SQLException {
		
		long start2 = 0;
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
			long start = System.currentTimeMillis();//new --for evaluation
			wikiID = r.createDatabase(con, versionIndex); //create the database
			long end2 = System.currentTimeMillis();
			con.commit();

			
			DatabaseWikiAppEngine wiki = new DatabaseWikiAppEngine(wikiID, name, title, autoSchemaChanges, authenticationMode,_connector, this,
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
				start2 = System.currentTimeMillis();
				reader.start(); //import the data
			}
			long end = System.currentTimeMillis();//new --for evaluation
			log.info("The creation of the database required: " + (end2 - start) +" ms");
			log.info("The importing of the data required: " +(end - start2) +" ms");
			log.info("The creation of the database and importing data in it took: " + (end-start)/1000 +" sec");//new --for evaluation
		} catch (java.sql.SQLException sqlException) {
			con.rollback();
			con.close();
			throw new WikiFatalException(sqlException);
		}
		con.commit();
		con.close();
	}
	
	
	//new --if the database is already created and we just want to import data into it
	public void importData( String name, String title, String path, URL resource, DatabaseSchema databaseSchema, User user, int authenticationMode, int autoSchemaChanges )
			throws WikiException, SQLException{
		//
		// Import data into created database wiki if the user specified an import resource.
		//
		
		Connection con = _connector.getConnection();
		
		//int wikiID = -1;
		
		DatabaseWikiAppEngine wiki = (DatabaseWikiAppEngine) get( name ); 
		Database database = wiki.database();
		database.initializeSchema(con,databaseSchema,user);
		
		if (resource != null) {
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
	}
	
	
	public void sortWikiListing() {
		Collections.sort(_wikiListing);
	}

	//TODO:perhaps i should delete this method and then delete the abstract method in WikiServer as they are not used
	//because WikiServerStandalone must implement all the abstract methods of abstract parent class WikiServer-not in use as i needed to use the Logger log
	@Override
	public void registerDatabase(String name, String title, String path,
			URL resourceURL, DatabaseSchema databaseSchema, User user,
			int authentication, int autoSchemaChanges)
			throws NumberFormatException, WikiException, SQLException {
		// TODO Auto-generated method stub
		
	}
		
			
}
