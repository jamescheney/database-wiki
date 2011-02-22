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
import java.util.Properties;

import java.sql.Connection;

import org.dbwiki.data.io.XMLDocumentImportReader;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.driver.rdbms.DatabaseImportHandler;
import org.dbwiki.driver.rdbms.RDBMSDatabase;

import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;

public class DatabaseImport {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "DatabaseImport <config-file> <db-name> <target-path> <xml-file> <user>";
	
	
	/*
	 * Public Methods
	 */
	
	public static void main(String[] args) {
		if (args.length != 5) {
			System.out.println("Usage: " + commandLine);
			System.exit(0);
		}
		
		// jcheney: used more readable names
		File configFile = new File(args[0]);
		String wikiName = args[1];
		String targetPath = args[2];
		String inputName = args[3];
		File inputFile = new File(inputName);
		String user = args[4];
		
		try {
			Properties properties = org.dbwiki.lib.IO.loadProperties(configFile);
			DatabaseConnector connector = new DatabaseConnectorFactory().getConnector(properties);
			WikiServer server = new WikiServer(properties);
			DatabaseWiki wiki = null;
			for (int iWiki = 0; iWiki < server.size(); iWiki++) {
				if (server.get(iWiki).name().equalsIgnoreCase(wikiName)) {
					wiki = server.get(iWiki);
					break;
				}
			}
			RDBMSDatabase database = (RDBMSDatabase)wiki.database();
			XMLDocumentImportReader reader = new XMLDocumentImportReader("file://" + inputFile.getAbsolutePath(), database.schema(), targetPath, database.users().get(user), false, false);
			Connection con = connector.getConnection();
			con.setAutoCommit(false);
			DatabaseImportHandler importHandler = new DatabaseImportHandler(con, database);
			reader.setImportHandler(importHandler);
			reader.start();
			con.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
}
