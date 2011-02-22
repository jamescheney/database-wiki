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

import java.io.File;

import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;

public class CreateServer {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "CreateDatabaseWikiServer <config-file> <user-listing>";
	
	
	/*
	 * Public Methods
	 */
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: " + commandLine);
			System.exit(0);
		}
		
		// Creates the following tables in the database that is specified
		// by the parameters in the config-files:
		//
		// The listing of database wikis
		//
		// CREATE TABLE wiki_server_database (
		//   id SERIAL / AUTOINCREMENT,
		//   name varchar(16) NOT NULL,
		//   title varchar(80) NOT NULL,
		//   is_active int NOT NULL
		// )
		//
		//
		// The listing of users:
		//
		// CREATE TABLE wiki_server_user (
		//   id SERIAL / AUTO INCREMENT,
		//   login varchar(80) NOT NULL,
		//   full_name varchar(160) NOT NULL,
		//   password varchar(80) NOT NULL
		// )
		
		try {
			new DatabaseConnectorFactory().getConnector(org.dbwiki.lib.IO.loadProperties(new File(args[0]))).createServer(new File(args[1]));
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}

}
