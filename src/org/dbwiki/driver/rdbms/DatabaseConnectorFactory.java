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

import java.util.Properties;

import org.dbwiki.exception.WikiFatalException;

/** 
 * Uses the database-related properties in the config file, generate a connection to the appropriate database.
 * @author jcheney
 *
 */
public class DatabaseConnectorFactory {
	/*
	 * Public Constants
	 */
	
	public static final String PASSWORD   = "JDBC_PASSWORD";
	public static final String RDBMS_TYPE = "RDBMS_TYPE";
	public static final String URL        = "JDBC_URL";
	public static final String USER       = "JDBC_USER";

	
	/*
	 * Private Variables
	 */
	
	private static final String rdbmsTypeMySQL      = "MYSQL";
	private static final String rdbmsTypePostgreSQL = "PSQL";
	private static final String rdbmsTypeSQLServer  = "SQLSERVER";

	
	/*
	 * Public Methods
	 */
	
	public DatabaseConnector getConnector(Properties properties) throws org.dbwiki.exception.WikiException {
		String rdbmsType = properties.getProperty(RDBMS_TYPE);

		String url = properties.getProperty(URL);
		String user = properties.getProperty(USER);
		String password = properties.getProperty(PASSWORD);

		try {
			if (rdbmsTypePostgreSQL.equalsIgnoreCase(rdbmsType)) {
				Class.forName("org.postgresql.Driver");
				return new PSQLDatabaseConnector(url, user, password);
			} else if (rdbmsTypeMySQL.equalsIgnoreCase(rdbmsType)) {
				Class.forName("com.mysql.jdbc.Driver");
				return new MySQLDatabaseConnector(url, user, password);
			} else if (rdbmsTypeSQLServer.equalsIgnoreCase(rdbmsType)) {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				return new SQLServerDatabaseConnector(url, user, password);
			} else {
				throw new WikiFatalException("Unknown property " + RDBMS_TYPE + "=" + rdbmsType);
			}
		} catch (ClassNotFoundException exception) {
			throw new WikiFatalException(exception);
		}
	}
}
