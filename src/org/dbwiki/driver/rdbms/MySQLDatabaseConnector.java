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

import java.sql.Connection;
import java.sql.Statement;
import java.util.Vector;

/** Subclass of DatabaseConnector to provide MySQL specific functionality */

public class MySQLDatabaseConnector extends DatabaseConnector {
	/*
	 * Private Constants
	 */
	
	private static final String viewSchemaIndexBase = "_veindex_base";
	
	
	/*
	 * Constructors
	 */
	
	public MySQLDatabaseConnector(String url, String user, String password) {
		super(url, user, password);
	}
	
	/*
	 * Public Methods
	 */
	
	public String joinMatchSQLStatements(Vector<String> sqlStatements, String database) {
		String sql = null;
		if (sqlStatements.size() > 0) {
			sql = "SELECT DISTINCT " + RelDataColEntry + " FROM (SELECT DISTINCT " + RelDataColEntry + " FROM "
					+  database + RelationData;
			for (int iStatement = 0; iStatement < sqlStatements.size(); iStatement++) {
				sql = sql + " INNER JOIN (" + sqlStatements.get(iStatement) + ") i" + iStatement + " USING (" + RelDataColEntry + ")";
			}
			sql = sql + ") q ORDER BY " + RelDataColEntry;
		} else {
			sql = "SELECT DISTINCT " + RelDataColEntry + " FROM " + database + RelationData + " ORDER BY " + RelDataColEntry;
		}
		return sql;
	}
	
	/*
	 * Protected Methods
	 */
	
	protected String autoIncrementColumn(String name) {
		return name + " int NOT NULL AUTO_INCREMENT";
	}
	
	protected void createSchemaIndexView(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE VIEW " + name + viewSchemaIndexBase + " AS " +
				"SELECT " + RelDataColEntry + ", " + RelDataColParent + ", " + RelDataColSchema + ", COUNT(*) cnt " +
				"FROM " + name + RelationData + " " +
				"WHERE " + RelDataColSchema + " >= 0 GROUP BY " + RelDataColEntry + ", " + RelDataColParent + ", " + RelDataColSchema);
		stmt.execute("CREATE VIEW " + name + ViewSchemaIndex + " AS " +
				"SELECT " + RelDataColEntry + ", " + RelDataColSchema + ", MAX(cnt) " + ViewSchemaIndexColMaxCount + " " + 
				"FROM " + name + viewSchemaIndexBase + " " +
				"GROUP BY " + RelDataColEntry + ", " + RelDataColSchema);
		stmt.execute("DROP VIEW " + name + viewSchemaIndexBase);
		
		stmt.close();
	}
	
	protected void createSchemaTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement statement = con.createStatement();
		String relName = dbName + RelationSchema;
		
		statement.execute("CREATE TABLE " + relName + " (" +
				autoIncrementColumn(RelSchemaColID) + " int NOT NULL, " +
				RelSchemaColType + " int NOT NULL, " +
				RelSchemaColLabel + " varchar(255) NOT NULL, " +
				RelSchemaColParent + " int NOT NULL, " +
				RelSchemaColUser + " int NOT NULL, " +
				RelSchemaColTimesequence + " int NOT NULL DEFAULT -1, " +
				"PRIMARY KEY (" + RelSchemaColID + "), " +
				"UNIQUE(" + RelSchemaColLabel + ", " + RelSchemaColParent + "))");

		statement.close();
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
				RelDataColTimesequence + " int NOT NULL DEFAULT -1, " +	
				RelDataColPre + " int NOT NULL, " +
				RelDataColPost + " int NOT NULL, " +
				"PRIMARY KEY (" + RelDataColID + "), " +
				"KEY idx" + RelationData + " (" + RelDataColPost + ", " + RelDataColParent +
				", " + RelDataColSchema + ", " + RelDataColPre + ", " + RelDataColEntry + "))");

		stmt.close();
	}
	
	protected void createAnnotationTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationAnnotation;
		
		stmt.execute("CREATE TABLE " + relName + "(" +
				autoIncrementColumn(RelAnnotationColID) + ", " +
				RelAnnotationColNode + " int NOT NULL, " +
				RelAnnotationColParent + " int, " +
				RelAnnotationColDate + " varchar(80) NOT NULL, " +
				RelAnnotationColText + " varchar(4000) NOT NULL, " +
				RelAnnotationColUser + " int NOT NULL, " +
				"PRIMARY KEY (" + RelAnnotationColID + "), " +
				"KEY idx" + RelationAnnotation + "(" + RelAnnotationColNode + "))");
		
		stmt.close();
	}
	
	protected void createTimestampTable(Connection con, String dbName) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		String relName = dbName + RelationTimesequence;

		stmt.execute("CREATE TABLE " + relName + "(" +
				autoIncrementColumn(RelTimesequenceColID) + ", " +
				RelTimesequenceColStart + " int NOT NULL, " +
				RelTimesequenceColStop + " int NOT NULL, " +
				"PRIMARY KEY (" + RelTimesequenceColID + "), " +
				"KEY idx" + RelationTimesequence + " (" + RelTimesequenceColStart + ", " + RelTimesequenceColStop + "))");
		
		stmt.close();
	}
}
