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
package org.dbwiki.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.web.server.WikiServerConstants;

public class SQLUserListing extends UserListing implements WikiServerConstants {
	/*
	 * Constructors
	 */
	
	public SQLUserListing(Connection connection) throws org.dbwiki.exception.WikiException {
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + RelationUser);
			while (rs.next()) {
				this.add(new User(rs.getInt(RelUserColID), rs.getString(RelUserColLogin), rs.getString(RelUserColFullName), rs.getString(RelUserColPassword)));
			}
			rs.close();
			stmt.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}
}
