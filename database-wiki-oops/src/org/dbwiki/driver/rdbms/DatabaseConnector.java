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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.StringTokenizer;

import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.Entity;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.lib.JDBCConnector;

import org.dbwiki.user.User;

import org.dbwiki.web.server.WikiServerConstants;

public abstract class DatabaseConnector extends JDBCConnector implements DatabaseConstants, WikiServerConstants {
	/*
	 * Constructors
	 */
	
	public DatabaseConnector(String url, String user, String password) {
		super(url, user, password);
	}
	

	/*
	 * Protected Abstract Methods
	 */
	
	protected abstract String autoIncrementColumn(String name);
	protected abstract void createEntityIndexView(Connection con, String name) throws java.sql.SQLException;
	
	
	/*
	 * Public Methods
	 */
	
	public void createDatabase(Connection con, String name, DatabaseSchema schema, User user) throws org.dbwiki.exception.WikiException {
		try {
			this.createSchemaTable(con, name, schema, user);
			this.createDataTable(con, name);
			this.createAnnotationTable(con, name);
			this.createVersionTable(con, name);
			this.createTimestampTable(con, name);
			this.createPagesTable(con, name);
			this.createDataView(con, name);
			this.createEntityIndexView(con, name);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void createDatabase(Connection con, String name, User user) throws org.dbwiki.exception.WikiException {
		this.createDatabase(con, name, null, user);
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
		
		Connection con = this.getConnection();
		
		try {
			Statement stmt = con.createStatement();
	
			stmt.execute("CREATE TABLE " + RelationDatabase + " (" +
					this.autoIncrementColumn(RelDatabaseColID) + ", " +
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
	
			stmt.execute("CREATE TABLE " + RelationConfigFile + " (" +
					RelConfigFileColWikiID + " int NOT NULL, " +
					RelConfigFileColFileType + " int NOT NULL, " +
					RelConfigFileColFileVersion + " int NOT NULL, " +
					RelConfigFileColTime + " bigint NOT NULL," +
					RelConfigFileColUser + " int NOT NULL, " +
					RelConfigFileColValue + " text NOT NULL, " +
					"PRIMARY KEY (" + RelConfigFileColWikiID + ", " + RelConfigFileColFileType + "," + RelConfigFileColFileVersion + "))");

			stmt.execute("CREATE TABLE " + RelationUser + " (" +
					this.autoIncrementColumn(RelUserColID) + ", " +
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

	public void dropDatabase(Connection con, String name) throws org.dbwiki.exception.WikiException {
		try {
			Statement stmt = con.createStatement();
			
			this.dropView(stmt, name + ViewData);
			this.dropView(stmt, name + ViewEntityIndex);
			
			this.dropTable(stmt, name + RelationAnnotation);
			this.dropTable(stmt, name + RelationAnnotation);
			this.dropTable(stmt, name + RelationData);
			this.dropTable(stmt, name + RelationPages);
			this.dropTable(stmt, name + RelationSchema);
			this.dropTable(stmt, name + RelationTimestamp);
			this.dropTable(stmt, name + RelationVersion);
			
			stmt.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void dropServer(boolean dropDatabases) throws org.dbwiki.exception.WikiException {
		try {
		Connection con = this.getConnection();
		Statement stmt = con.createStatement();
		if (dropDatabases) {
			ResultSet rs = stmt.executeQuery("SELECT " + RelDatabaseColName + " FROM " + RelationDatabase);
			while (rs.next()) {
				String name = rs.getString(1);
				this.dropDatabase(con, name);
			}
			rs.close();
		}
		this.dropTable(stmt, RelationDatabase);
		this.dropTable(stmt, RelationConfigFile);
		this.dropTable(stmt, RelationUser);
		stmt.close();
		con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException); 
		}
	}

	
	/*
	 * Protected Methods
	 */
	
	protected void  createAnnotationTable(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = name + RelationAnnotation;
		
		stmt.execute("CREATE TABLE " + relName + "(" +
				this.autoIncrementColumn(RelAnnotationColID) + ", " +
				RelAnnotationColNode + " int NOT NULL, " +
				RelAnnotationColParent + " int, " +
				RelAnnotationColDate + " varchar(80) NOT NULL, " +
				RelAnnotationColText + " varchar(4000) NOT NULL, " +
				RelAnnotationColUser + " int NOT NULL, "+
				"PRIMARY KEY (" + RelAnnotationColID + "))");
		
		stmt.execute("CREATE INDEX idx_" + relName + "_" + RelAnnotationColNode + " ON " + relName + " (" + RelAnnotationColNode + ")");

		stmt.close();
	}

	protected void createDataTable(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = name + RelationData;
		
		stmt.execute("CREATE TABLE " + relName + " (" +	
				this.autoIncrementColumn(RelDataColID) + ", " +
				RelDataColEntity + " int NOT NULL, " +
				RelDataColParent + " int NOT NULL, " +
				RelDataColEntry + " int NOT NULL, " +
				RelDataColValue + " text, " +
				RelDataColTimestamp + " int NOT NULL DEFAULT (-1), " +				
				"PRIMARY KEY (" + RelDataColID + "))");
		
		stmt.execute("CREATE INDEX idx_" + relName + "_" + RelDataColEntity + " ON " + relName + " (" + RelDataColEntity + ")");

		stmt.close();
	}
	
	protected void createDataView(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE VIEW " + name + ViewData + " AS " +
				"SELECT " + 
					"d." + RelDataColID + " " + ViewDataColNodeID + ", " +
					"d." + RelDataColParent + " " + ViewDataColNodeParent + ", " +
					"d." + RelDataColEntity + " " + ViewDataColNodeEntity + ", " +
					"d." + RelDataColEntry + " " + ViewDataColNodeEntry + ", " +
					"d." + RelDataColValue + " " + ViewDataColNodeValue + ", " +
					"t." + RelTimestampColStart + " " + ViewDataColTimestampStart + ", " +
					"t." + RelTimestampColEnd + " " + ViewDataColTimestampEnd + ", " +
					"a." + RelAnnotationColID + " " + ViewDataColAnnotationID + ", " +
					"a." + RelAnnotationColDate + " " + ViewDataColAnnotationDate + ", " +
					"a." + RelAnnotationColUser + " " + ViewDataColAnnotationUser + ", " +
					"a." + RelAnnotationColText + " " + ViewDataColAnnotationText + " " +
				"FROM " + name + RelationData + " d " +
				"LEFT OUTER JOIN " + name + RelationTimestamp + " t ON (d." + RelDataColTimestamp + " = " + "t." + RelTimestampColID + ") " +
				"LEFT OUTER JOIN " + name + RelationAnnotation + " a ON (d." + RelDataColTimestamp + " = " + "a." + RelAnnotationColNode + ")");
		
		stmt.close();
	}
	
	protected void createPagesTable(Connection con, String name) throws java.sql.SQLException {	
		Statement stmt = con.createStatement();
		
		String relName = name + RelationPages;
		
		stmt.execute("CREATE TABLE " + relName + " (" +
				this.autoIncrementColumn(RelPagesColID) + ", " +
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

	protected void createSchemaTable(Connection con, String name, DatabaseSchema schema, User user) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = name + RelationSchema;
		
		stmt.execute("CREATE TABLE " + relName + " (" +
				RelSchemaColID + " int NOT NULL, " +
				RelSchemaColType + " int NOT NULL, " +
				RelSchemaColLabel + " varchar(255) NOT NULL, " +
				RelSchemaColParent + " int NOT NULL, " +
				RelSchemaColUser + " int NOT NULL, " +
				"PRIMARY KEY (" + RelSchemaColID + "), " +
				"UNIQUE(" + RelSchemaColLabel + ", " + RelSchemaColParent + "))");
		
		if (schema != null) {
			for (int iEntity = 0; iEntity < schema.size(); iEntity++) {
				Entity entity = schema.get(iEntity);
				String sql = "INSERT INTO " + relName + "(" +
					RelSchemaColID +", " +
					RelSchemaColType + ", " +
					RelSchemaColLabel + ", " +
					RelSchemaColParent + ", " +
					RelSchemaColUser + ") VALUES(";
				sql = sql + entity.id() + ", ";
				if (entity.isAttribute()) {
					sql = sql + RelSchemaColTypeValAttribute + ", ";
				} else {
					sql = sql + RelSchemaColTypeValGroup + ", ";				
				}
				sql = sql + "'" + entity.label() + "', ";
				if (entity.parent() != null) {
					sql = sql + entity.parent().id() + ", ";
				} else {
					sql = sql + "-1, ";
				}
				if (user != null) {
					sql = sql + user.id() + ")";
				} else {
					sql = sql + User.UnknownUserID + ")";
				}
				stmt.execute(sql);
			}
		}
		stmt.close();
	}
	
	protected void createTimestampTable(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = name + RelationTimestamp;

		stmt.execute("CREATE TABLE " + relName + "(" +
				autoIncrementColumn(RelTimestampColID) + ", " +
				RelTimestampColStart + " int NOT NULL, " +
				RelTimestampColEnd + " int NOT NULL, " +
				"PRIMARY KEY (" + RelTimestampColID + ", " + RelTimestampColStart + "))");
		
		stmt.close();
	}


	protected void createVersionTable(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE TABLE " + name + RelationVersion + " (" +
				RelVersionColNumber + " int NOT NULL, " +
				RelVersionColName + " varchar(80) NOT NULL, " +
				RelVersionColProvenance + " smallint NOT NULL, " +
				RelVersionColTimeMilliSec + " bigint NOT NULL, " +
				RelVersionColUser + " int NOT NULL, " +
				RelVersionColSourceURL + " varchar(80), " +
				RelVersionColNode + " int NOT NULL, " +
				"PRIMARY KEY (" + RelVersionColNumber + "))");
		
		stmt.close();
	}
	
	protected void dropTable(Statement stmt, String name) {
		try {
			stmt.execute("DROP TABLE " + name + " CASCADE");
		} catch (java.sql.SQLException sqlException) {
		}
	}

	protected void dropView(Statement stmt, String name) {
		try {
			stmt.execute("DROP VIEW " + name + " CASCADE");
		} catch (java.sql.SQLException sqlException) {
		}
	}
}
