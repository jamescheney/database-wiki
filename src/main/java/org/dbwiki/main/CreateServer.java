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
import java.util.List;
import java.util.Properties;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.user.User;
import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.google.appengine.api.rdbms.AppEngineDriver;


/** Creates the top-level tables.  Needs to be run before starting the wiki.
 * 
 * @author jcheney
 *
 */
public class CreateServer {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "CreateServer <config-file> <user-listing>";
	
	
	/*
	 * Public Methods
	 */
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: " + commandLine);
			System.exit(0);
		}


		// Creates the following tables: _database, _presentation, _user
		try {
            Connection c = null;
            DriverManager.registerDriver(new AppEngineDriver());
            c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:intern/dbwiki");
				File configFile = new File(args[0]);
				File userFile = new File(args[1]);
				Properties configProperties = org.dbwiki.lib.IO.loadProperties(configFile);
				DatabaseConnector connector = new DatabaseConnectorFactory().getConnector(configProperties);
				List<User> users = User.readUsers(userFile);
				connector.createServer(users);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}

}
