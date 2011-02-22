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
package org.dbwiki.lib;

import java.sql.Connection;
import java.sql.DriverManager;

import org.dbwiki.exception.WikiFatalException;

public class JDBCConnector {
	/*
	 * Private Variables
	 */
	
	private String _password;
	private String _url;
	private String _user;
	
	
	/*
	 * Constructors
	 */
	
	public JDBCConnector(String url, String user, String password) {
/*
		_rdbmsType = properties.getProperty(RDBMS_TYPE);
		
		try {
			if (this.isPostgreSQL()) {
				Class.forName("org.postgresql.Driver");
			} else if (this.isMySQL()) {
				Class.forName("com.mysql.jdbc.Driver");
			} else {
				throw new WikiFatalException("Unknown property " + RDBMS_TYPE + "=" + _rdbmsType);
			}
		} catch (ClassNotFoundException exception) {
			throw new WikiFatalException(exception);
		}
		
		_url = properties.getProperty(URL);
		_user = properties.getProperty(USER);
		_password = properties.getProperty(PASSWORD);
*/
		_url = url;
		_user = user;
		_password = password;
	}

	
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
/*
 * 
	public boolean isMySQL() {
		return rdbmsTypeMySQL.equalsIgnoreCase(_rdbmsType);
	}

	public boolean isPostgreSQL() {
		return rdbmsTypePostgreSQL.equalsIgnoreCase(_rdbmsType);
	}
*/
}
