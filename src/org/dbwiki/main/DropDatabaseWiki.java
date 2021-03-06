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
package org.dbwiki.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.Statement;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.driver.rdbms.DatabaseConstants;

import org.dbwiki.web.server.WikiServerConstants;

/** PERMANENTLY deletes a DatabaseWiki and removes all of its tables.
 * 
 * @author jcheney
 *
 */
public class DropDatabaseWiki implements DatabaseConstants, WikiServerConstants {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "DropDatabaseWiki <config-file>";
	
	
	/*
	 * Public Methods
	 */
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage " + commandLine);
			System.exit(0);
		}
		
		try {
			System.out.print("Enter the name of the wiki you want to delete: ");
			// jcheney: got rid of implicit upper-casing
			String name = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			DatabaseConnector connector = new DatabaseConnectorFactory().getConnector(org.dbwiki.lib.IO.loadProperties(new File(args[0])));
			Connection con = connector.getConnection();
			Statement stmt = con.createStatement();
			stmt.execute("DELETE FROM " + RelationDatabase + " WHERE " + RelDatabaseColName + " = '" + name + "'");
			System.out.println("DELETE FROM " + RelationDatabase + " WHERE " + RelDatabaseColName + " = '" + name + "'");
			stmt.close();
			connector.dropDatabase(con, name);
			/*Statement stmt = con.createStatement();
			stmt.execute("DELETE FROM " + RelationDatabase + " WHERE " + RelDatabaseColName + " = '" + name + "'");
			stmt.close();*/
			con.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
}
