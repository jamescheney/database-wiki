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
package org.dbwiki.driver.rdbms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.StringTokenizer;

import org.dbwiki.data.provenance.ProvenanceUnknown;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.time.Version;

import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.user.User;

import org.dbwiki.web.server.WikiServerConstants;

/** A connection to a database, representing the basic tables of a DatabaseWiki 
 * 
 * @author jcheney
 *
 */
public abstract class DatabaseConnector implements DatabaseConstants, WikiServerConstants {

	private String _password;
	private String _url;
	private String _user;
	
	/*
	 * Constructors
	 */
	
	public DatabaseConnector(String url, String user, String password) {
		
		_password = password;
		_url = url;
		_user = user;
	}
	

	/*
	 * Protected Abstract Methods
	 */
	
	protected abstract String autoIncrementColumn(String dbName);
	protected abstract void createSchemaIndexView(Connection con, String dbName) throws java.sql.SQLException;
	
	
	
	
	/*
	 * Public Methods
	 */
	
	public Connection getConnection() throws org.dbwiki.exception.WikiException {
		try {
			return DriverManager.getConnection(_url, _user, _password);	
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
	
	
	public void createDatabase(Connection con, String dbName, DatabaseSchema schema, User user, SQLVersionIndex versionIndex) throws org.dbwiki.exception.WikiException {
		try {
			createSchemaTable(con, dbName);
			createDataTable(con, dbName);
			createAnnotationTable(con, dbName);
			createVersionTable(con, dbName);
			createTimestampTable(con, dbName);
			createPagesTable(con, dbName);
			createDataView(con, dbName);
			createSchemaIndexView(con, dbName);
			
			// store the schema, generating a version number and timestamp for the root
			storeSchema(con, dbName, schema, user, versionIndex);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void createDatabase(Connection con, String dbName, User user) throws org.dbwiki.exception.WikiException {
		createDatabase(con, dbName, null, user, null);
	}
	
	public void createServer(File file) throws org.dbwiki.exception.WikiException {
		// Creates wiki server main tables in MySQL database:
		//
		// The listing of database wikis
		//
		// CREATE TABLE wiki_server_database (
		//   id SERIAL,
		//   name varchar(16) NOT NULL,
		//   title varchar(80) NOT NULL,
		//   authentication int NOT NULL,
		//   auto_schema_changes int NOT NULL,
		//   def_css int NOT NULL DEFAULT 1,
		//   def_layout int NOT NULL DEFAULT -1,
		//   def_template int NOT NULL DEFAULT 1,
		//   user int NOT NULL,
		//   is_active int NOT NULL DEFAULT 1
		// )
		//
		//
		// The listing of users:
		//
		// CREATE TABLE wiki_server_user (
		//   id SERIAL,
		//   login varchar(80) NOT NULL,
		//   full_name varchar(160) NOT NULL,
		//   password varchar(80) NOT NULL
		// )
		
		Connection con = getConnection();
		
		try {
			Statement stmt = con.createStatement();
	
			stmt.execute("CREATE TABLE " + RelationDatabase + " (" +
					autoIncrementColumn(RelDatabaseColID) + ", " +
					RelDatabaseColName + " varchar(16) NOT NULL UNIQUE, " +
					RelDatabaseColTitle + " varchar(80) NOT NULL, " +
					RelDatabaseColAuthentication + " int NOT NULL, " +
					RelDatabaseColAutoSchemaChanges + " int NOT NULL, " +
					RelDatabaseColCSS + " int NOT NULL DEFAULT " + RelConfigFileColFileVersionValUnknown + ", " +
					RelDatabaseColLayout + " int NOT NULL DEFAULT " + RelConfigFileColFileVersionValUnknown + ", " +
					RelDatabaseColTemplate + " int NOT NULL DEFAULT " + RelConfigFileColFileVersionValUnknown + ", " +
					RelDatabaseColUser + " int NOT NULL, " +
					RelDatabaseColIsActive + " int NOT NULL DEFAULT " + RelDatabaseColIsActiveValTrue + ", " +
					"PRIMARY KEY (" + RelDatabaseColID + "))");
	
			stmt.execute("CREATE TABLE " + RelationPresentation + " (" +
					RelPresentationColDatabase + " int NOT NULL, " +
					RelPresentationColType + " int NOT NULL, " +
					RelPresentationColVersion + " int NOT NULL, " +
					RelPresentationColTime + " bigint NOT NULL," +
					RelPresentationColUser + " int NOT NULL, " +
					RelPresentationColValue + " text NOT NULL, " +
					"PRIMARY KEY (" + RelPresentationColDatabase + ", " + RelPresentationColType + "," + RelPresentationColVersion + "))");

			stmt.execute("CREATE TABLE " + RelationUser + " (" +
					autoIncrementColumn(RelUserColID) + ", " +
					RelUserColLogin + " varchar(80) NOT NULL UNIQUE, " +
					RelUserColFullName + " varchar(80) NOT NULL, " +
					RelUserColPassword + " varchar(80) NOT NULL, " +
					"PRIMARY KEY (" + RelUserColID + "))");
			
			if (file != null) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line;
				while ((line = in.readLine()) != null) {
					if (!line.startsWith("#")) {
						StringTokenizer tokens = new StringTokenizer(line, "\t");
						String login = tokens.nextToken();
						String fullName = tokens.nextToken();
						String password = tokens.nextToken();
						String sql = "INSERT INTO " + RelationUser + "(" +
							RelUserColLogin + ", " +
							RelUserColFullName + ", " +
							RelUserColPassword + ") VALUES(";
						sql = sql + "'" + login + "', '" + fullName + "', '" + password + "')";
						stmt.execute(sql);
					}
				}
				in.close();
			}
			
			stmt.close();
			
			con.close();
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void dropDatabase(Connection con, String dbName) throws org.dbwiki.exception.WikiException {
		try {
			Statement stmt = con.createStatement();
			
			dropView(stmt, dbName + ViewData);
			dropView(stmt, dbName + ViewSchemaIndex);
			
			dropTable(stmt, dbName + RelationAnnotation);
			dropTable(stmt, dbName + RelationAnnotation);
			dropTable(stmt, dbName + RelationData);
			dropTable(stmt, dbName + RelationPages);
			dropTable(stmt, dbName + RelationSchema);
			dropTable(stmt, dbName + RelationTimesequence);
			dropTable(stmt, dbName + RelationVersion);
			
			stmt.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void dropServer(boolean dropDatabases) throws org.dbwiki.exception.WikiException {
		try {
		Connection con = getConnection();
		Statement stmt = con.createStatement();
		if (dropDatabases) {
			ResultSet rs = stmt.executeQuery("SELECT " + RelDatabaseColName + " FROM " + RelationDatabase);
			while (rs.next()) {
				String dbName = rs.getString(1);
				dropDatabase(con, dbName);
			}
			rs.close();
		}
		dropTable(stmt, RelationDatabase);
		dropTable(stmt, RelationPresentation);
		dropTable(stmt, RelationUser);
		stmt.close();
		con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException); 
		}
	}

	
	/*
	 * Protected Methods
	 */
	
	protected void createAnnotationTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationAnnotation;
		
		stmt.execute("CREATE TABLE " + relName + "(" +
				autoIncrementColumn(RelAnnotationColID) + ", " +
				RelAnnotationColNode + " int NOT NULL, " +
				RelAnnotationColParent + " int, " +
				RelAnnotationColDate + " varchar(80) NOT NULL, " +
				RelAnnotationColText + " varchar(4000) NOT NULL, " +
				RelAnnotationColUser + " int NOT NULL, "+
				"PRIMARY KEY (" + RelAnnotationColID + "))");
		
		stmt.execute("CREATE INDEX idx_" + relName + "_" + RelAnnotationColNode + " ON " + relName + " (" + RelAnnotationColNode + ")");

		stmt.close();
	}

	protected void createDataTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationData;
		
		stmt.execute("CREATE TABLE " + relName + " (" +	
				autoIncrementColumn(RelDataColID) + ", " +
				RelDataColSchema + " int NOT NULL, " +
				RelDataColParent + " int NOT NULL, " +
				RelDataColEntry + " int NOT NULL, " +
				RelDataColValue + " text, " +
				RelDataColTimesequence + " int NOT NULL DEFAULT (-1), " +	
				RelDataColPre + " int NOT NULL, " +
				RelDataColPost + " int NOT NULL, " +
				"PRIMARY KEY (" + RelDataColID + "))");
		
		stmt.execute("CREATE INDEX idx_pre_" + relName + "_" + RelDataColPre + " ON " + relName + " USING btree " + " (" + RelDataColPre + ")" );
		stmt.execute("CREATE INDEX idx_post_" + relName + "_" + RelDataColPost + " ON " + relName + " USING btree "+ " (" + RelDataColPost +")");
		stmt.execute("CREATE INDEX idx_par_" + relName + "_" + RelDataColParent + " ON " + relName + " USING hash "+ " (" + RelDataColParent +")");
		stmt.execute("CREATE INDEX idx_schema_" + relName + "_" + RelDataColSchema + " ON " + relName + " USING hash "+ " (" + RelDataColSchema +")");		
		stmt.execute("CREATE INDEX idx_ID_" + relName + "_" + RelDataColID + " ON " + relName + " USING hash "+ " (" + RelDataColID +")");
		stmt.execute("CREATE INDEX idx_entry_" + relName + "_" + RelDataColEntry + " ON " + relName + " USING hash "+ " (" + RelDataColEntry +")");

		stmt.close();
	}
	
	protected void createDataView(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE VIEW " + dbName + ViewData + " AS " +
				"SELECT " + 
					"d." + RelDataColID + " " + ViewDataColNodeID + ", " +
					"d." + RelDataColParent + " " + ViewDataColNodeParent + ", " +
					"d." + RelDataColSchema + " " + ViewDataColNodeSchema + ", " +
					"d." + RelDataColEntry + " " + ViewDataColNodeEntry + ", " +
					"d." + RelDataColValue + " " + ViewDataColNodeValue + ", " +
					"d." + RelDataColPre + " " + ViewDataColNodePre + ", " +
					"d." + RelDataColPost + " " + ViewDataColNodePost + ", " +
					"t." + RelTimesequenceColStart + " " + ViewDataColTimestampStart + ", " +
					"t." + RelTimesequenceColStop + " " + ViewDataColTimestampEnd + ", " +
					"a." + RelAnnotationColID + " " + ViewDataColAnnotationID + ", " +
					"a." + RelAnnotationColDate + " " + ViewDataColAnnotationDate + ", " +
					"a." + RelAnnotationColUser + " " + ViewDataColAnnotationUser + ", " +
					"a." + RelAnnotationColText + " " + ViewDataColAnnotationText + " " +
				"FROM " + dbName + RelationData + " d " +
				"LEFT OUTER JOIN " + dbName + RelationTimesequence + " t ON (d." + RelDataColTimesequence + " = " + "t." + RelTimesequenceColID + ") " +
				"LEFT OUTER JOIN " + dbName + RelationAnnotation + " a ON (d." + RelDataColID + " = " + "a." + RelAnnotationColNode + ")");
		
		stmt.close();
	}

	protected void createPagesTable(Connection con, String dbName) throws java.sql.SQLException {	
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationPages;
		
		stmt.execute("CREATE TABLE " + relName + " (" +
				autoIncrementColumn(RelPagesColID) + ", " +
				RelPagesColName + " varchar(255) NOT NULL, " +
				RelPagesColContent + " text NOT NULL, " +
				RelPagesColTimestamp + " bigint NOT NULL, " +
				RelPagesColUser + " int NOT NULL, " +
				"PRIMARY KEY (" + RelPagesColID + "))");
		
		String frontPageName = "FrontPage";
		String frontPageContent = "# The front page";
		
		// For now we use the number of milliseconds since
		// 1970/01/01
		String timestamp = Long.toString(new Date().getTime());
		int uid = User.UnknownUserID;
		
		stmt.execute("INSERT INTO " + relName + "(" +
				RelPagesColName +", " +
				RelPagesColContent + ", " +
				RelPagesColTimestamp + ", " + 
				RelPagesColUser + ") VALUES('" +
				frontPageName + "', '" +
				frontPageContent + "', '" +
				timestamp + "', " +
				uid + ")");
		
		stmt.close();
	}

	protected void createSchemaTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement statement = con.createStatement();
		String relName = dbName + RelationSchema;
		
		statement.execute("CREATE TABLE " + relName + " (" +
				RelSchemaColID + " int NOT NULL, " +
				RelSchemaColType + " int NOT NULL, " +
				RelSchemaColLabel + " varchar(255) NOT NULL, " +
				RelSchemaColParent + " int NOT NULL, " +
				RelSchemaColUser + " int NOT NULL, " +
				RelSchemaColTimesequence + " int NOT NULL default(-1), " +
				"PRIMARY KEY (" + RelSchemaColID + "), " +
				"UNIQUE(" + RelSchemaColLabel + ", " + RelSchemaColParent + "))");

		statement.close();
	}
	
	protected void storeSchema(Connection con, String dbName, DatabaseSchema schema, User user, SQLVersionIndex versionIndex) throws java.sql.SQLException, WikiException {
		Statement statement = con.createStatement();	
		String schemaTable = dbName + RelationSchema;
		
		if (schema != null) {
			for (int i = 0; i < schema.size(); i++) {
				SchemaNode node = schema.get(i);
				String newNode = "INSERT INTO " + schemaTable + "(" +
					RelSchemaColID +", " +
					RelSchemaColType + ", " +
					RelSchemaColLabel + ", " +
					RelSchemaColParent + ", " +
					RelSchemaColUser + ") VALUES(";
				newNode = newNode + node.id() + ", ";
				if (node.isAttribute()) {
					newNode = newNode + RelSchemaColTypeValAttribute + ", ";
				} else {
					newNode = newNode + RelSchemaColTypeValGroup + ", ";				
				}
				newNode = newNode + "'" + node.label() + "', ";
				if (node.parent() != null) {
					newNode = newNode + node.parent().id() + ", ";
				} else {
					newNode = newNode + "-1, ";
				}
				if (user != null) {
					newNode = newNode + user.id() + ")";
				} else {
					newNode = newNode + User.UnknownUserID + ")";
				}
				statement.execute(newNode);
			}
			
			// generate a version number for the schema root
			Version version = versionIndex.getNextVersion(new ProvenanceUnknown(user));
			versionIndex.add(version);
			versionIndex.store(con);
			
			// generate a timestamp for the schema root
			int timestamp = -1;
			String makeTimestamp =
					"INSERT INTO " + dbName + RelationTimesequence + "(" +
							RelTimesequenceColStart + ", " +
							RelTimesequenceColStop + ") VALUES(" + version.number() + " , -1)";
			statement.execute(makeTimestamp, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next()) {
				timestamp = rs.getInt(1);
				rs.close();
		    } else {
		        throw new WikiFatalException("There are no generated keys.");
		    }	
			
			// update the schema root with the generated timestamp
			GroupSchemaNode root = schema.root();
			String updateTimestamp =
					"UPDATE " + schemaTable + " " +
						"SET " + RelSchemaColTimesequence + " = " + timestamp +
						" WHERE " + RelSchemaColID + " = " + root.id();
			statement.execute(updateTimestamp);
		}
		
		statement.close();
	}

	
	protected void createTimestampTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationTimesequence;

		stmt.execute("CREATE TABLE " + relName + "(" +
				autoIncrementColumn(RelTimesequenceColID) + ", " +
				RelTimesequenceColStart + " int NOT NULL, " +
				RelTimesequenceColStop + " int NOT NULL, " +
				"PRIMARY KEY (" + RelTimesequenceColID + ", " + RelTimesequenceColStart + "))");
		
		stmt.close();
	}


	protected void createVersionTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE TABLE " + dbName + RelationVersion + " (" +
				RelVersionColNumber + " int NOT NULL, " +
				RelVersionColName + " varchar(80) NOT NULL, " +
				RelVersionColProvenance + " smallint NOT NULL, " +
				RelVersionColTime + " bigint NOT NULL, " +
				RelVersionColUser + " int NOT NULL, " +
				RelVersionColSource + " text, " +
				RelVersionColNode + " int NOT NULL, " +
				"PRIMARY KEY (" + RelVersionColNumber + "))");
		
		stmt.close();
	}
	
	protected void dropTable(Statement stmt, String dbName) {
		try {
			stmt.execute("DROP TABLE " + dbName + " CASCADE");
		} catch (java.sql.SQLException sqlException) {
		}
	}

	protected void dropView(Statement stmt, String dbName) {
		try {
			stmt.execute("DROP VIEW " + dbName + " CASCADE");
		} catch (java.sql.SQLException sqlException) {
		}
	}
}
