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
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConnectorFactory;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;

/**
 * Import presentation files into a database wiki.
 * 
 *   ImportPresentationFiles <config-file> <db-name> <path> <user>
 * 
 * The filenames are generated by appending in turn:
 * 
 *   <db-name>.css
 *   <db-name>.layout
 *   <db-name>.template
 *   
 * to the path. Any missing files are reported.
 * 
 * TODO: Unify this with the DatabaseImport command.
 */

public class ImportPresentationFiles {
	/*
	 * Private Constants
	 */
	public enum Type {
		CSS(1), Layout(2), Template(3);	
		private int _number;
		Type(int number) {
			_number = number;
		}
		public int getNumber() {return _number;}
	}
	
	private static final String commandLine = "ImportPresentationFile <config-file> <db-name> <path> <user>";

	private WikiServer _server;
	private DatabaseWiki _wiki;
	private RDBMSDatabase _database;
	private String _username;
	
	/*
	 * Public Methods
	 */
	public ImportPresentationFiles(WikiServer server, DatabaseWiki wiki, RDBMSDatabase database, String username) {
		_server = server;
		_wiki = wiki;
		_database = database;
		_username = username;
	}
		
	public void loadPresentationFile(String filename, Type type) throws IOException, WikiException {
		File inputFile = new File(filename);
		
		if(inputFile.exists()) {
			BufferedReader reader = new BufferedReader (new FileReader(inputFile));
			char[] buf = new char[(int)inputFile.length()];
			reader.read(buf);
			String contents = new String(buf);
			
			_server.updateConfigFile(_wiki.id(), type.getNumber(), contents, _database.users().get(_username));
		} else {
			System.out.println("File not found: " + filename);
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: " + commandLine);
			System.exit(0);
		}
		
		File configFile = new File(args[0]);
		String wikiName = args[1];	
		String path = args[2];
		String username = args[3];
		
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
			
			// [wiki] should never be null
			assert(wiki != null);
			
			RDBMSDatabase database = (RDBMSDatabase)wiki.database();
						
			Connection con = connector.getConnection();
			con.setAutoCommit(false);

			ImportPresentationFiles p = new ImportPresentationFiles(server, wiki, database, username);
			p.loadPresentationFile(path + wikiName + ".css", Type.CSS);
			p.loadPresentationFile(path + wikiName + ".layout", Type.Layout);
			p.loadPresentationFile(path + wikiName + ".template", Type.Template);
			
			con.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
}
