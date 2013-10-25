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
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.dbwiki.data.io.SAXCallbackInputHandler;
import org.dbwiki.data.io.StructureParser;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.web.server.WikiServer;

/** Imports a database serialized as an XML file, infers a schema and creates a new DBWiki.
 * TODO: Remove this in favor of package import/export
 * @author jcheney
 *
 */
public class DatabaseImport {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "DatabaseImport <config-file> <name> <title> <path> <xml-file> <user>";
	
	/*
	 * Public Methods
	 */
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		if (args.length != 6) {
			System.out.println("Usage: " + commandLine);
			System.exit(0);
		}
		
		File configFile = new File(args[0]);
		String name = args[1];
		String title = args[2];
		String path = args[3];
		String inputName = args[4];
		String user = args[5];
		
		try {
			Properties properties = org.dbwiki.lib.IO.loadProperties(configFile);
			WikiServer server = new WikiServer(properties);
			
			// attempt to generate a schema from the input file
			InputStream in = null;

			URL inputURL = new File(inputName).toURI().toURL();
			if (inputName.endsWith(".gz")) {
				in = new GZIPInputStream(inputURL.openStream());
			} else {
				in = inputURL.openStream();
			}
			// Generating schema
			StructureParser structureParser = new StructureParser();
			new SAXCallbackInputHandler(structureParser, false).parse(in, false, false);
			in.close();
			if (structureParser.hasException()) {
				throw structureParser.getException();
			}
			
			DatabaseSchema databaseSchema = structureParser.getDatabaseSchema(path);
			System.out.println(databaseSchema.printSchema() + " DatabaseImport line 88");
			// register the database with the server
			server.registerDatabase(name, title, path, inputURL, databaseSchema, server.users().get(user), 1, 0);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time of import is "+ (endTime-startTime+0.0)/1000);
	}
}
