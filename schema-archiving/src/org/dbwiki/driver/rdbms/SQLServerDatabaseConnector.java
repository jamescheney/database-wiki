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

/** Subclass of DatabaseConnector providing SQL Server specific functionality */
public class SQLServerDatabaseConnector extends DatabaseConnector {
	/*
	 * Constructors
	 */
	
	public SQLServerDatabaseConnector(String url, String user, String password) {
		super(url, user, password);
	}
	
	
	/*
	 * Protected Methods
	 */
	
	protected String autoIncrementColumn(String name) {
		return name + " int IDENTITY(1,1)";
	}

	protected void createEntityIndexView(Connection con, String name) throws java.sql.SQLException {
		Statement stmt = con.createStatement();
		
		stmt.execute("CREATE VIEW " + name + ViewEntityIndex + " AS " +
				"SELECT " + RelDataColEntry + ", " + RelDataColEntity + ", MAX(cnt) " + ViewEntityIndexColMaxCount + " " + 
				"FROM (SELECT " + RelDataColEntry + ", " + RelDataColParent + ", " + RelDataColEntity + ", COUNT(*) cnt " +
				"FROM " + name + RelationData + " " +
				"WHERE " + RelDataColEntity + " >= 0 GROUP BY " + RelDataColEntry + ", " + RelDataColParent + ", " + RelDataColEntity + ") q " +
				"GROUP BY " + RelDataColEntry + ", " + RelDataColEntity);
		
		stmt.close();
	}
}
