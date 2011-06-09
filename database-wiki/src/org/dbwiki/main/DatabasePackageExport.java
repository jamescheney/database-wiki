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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.dbwiki.data.io.ExportNodeWriter;
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.main.ImportPresentationFiles.PresentationFileType;

/** Imports a database "package".  Arguments:
 * Config file
 * Name of database
 * Title of database
 * Path to entries
 * XML file
 * HTML template
 * CSS
 * Layout
 * User 
 * 
 * Alternative usage:
 * Config file
 * Path to a directory that contains a "package descriptor", which is a properties file providing the above arguments.
 * FIXME #import: Refactor this and DatabasePackageImport and ImportPresentationFiles to avoid duplication.
 * @author jcheney
 *
 */
public class DatabasePackageExport {
	/*
	 * Private Constants
	 */
	
	private static final String commandLine = "DatabasePackageExport <config-file> [<packageinfo>|<name> <title> <path> <xml-file> <html-template> <css> <layout>]";
	private static WikiServer _server;
	private static DatabaseWiki _wiki;
	
	private static String PackageInfoName = "NAME";
	private static String PackageInfoTitle = "TITLE";
	private static String PackageInfoPath = "PATH";
	private static String PackageInfoInputXML = "INPUT_XML";
	private static String PackageInfoTemplate = "TEMPLATE";
	private static String PackageInfoCSS = "CSS";
	private static String PackageInfoLayout = "LAYOUT";
	/*
	 * Public Methods
	 */
	
	public static void savePresentationFile(String filename, int version, PresentationFileType type) throws IOException, WikiException {
		File outputFile = new File(filename);
		
		BufferedWriter writer = new BufferedWriter (new FileWriter(outputFile));
		assert(writer != null);
		String configFile = null;
		switch (type) {
			case CSS:
				configFile = _server.getStyleSheet(_wiki, version);
				break;
			case Layout:
				configFile = _server.getLayout(_wiki, version);
				break;
			case Template:
				configFile = _server.getTemplate(_wiki, version);
				break;
		}
		
		writer.write(configFile);
		writer.close();	
	}
	
	
	public static class Args {
		
		
		
		File configFile = null;
		String name = null;
		String title = null;
		String path = null;
		String xmlFile = null;
		String htmlTemplate = null;
		String cssTemplate = null;
		String layout = null;
		
		public Args (String[] args) throws IOException {
			configFile = new File(args[0]);
			if(args.length == 8) {
				name = args[1];
				title = args[2];
				path = args[3];
				xmlFile = args[4];
				htmlTemplate = args[5];
				cssTemplate = args[6];
				layout = args[7];
			} else if (args.length == 2) {
				String packageInfo = args[1];
				File packageFile = new File(packageInfo);
				File packageDir;
				if(packageFile.isDirectory()) {
					packageDir = packageFile;
					packageFile = new File(packageInfo + "pkginfo");
				} else {
					packageDir = new File(packageFile.getParent());
				}
				
				Properties packageProperties = org.dbwiki.lib.IO.loadProperties(packageFile);
				name = packageProperties.getProperty(PackageInfoName);
				title = packageProperties.getProperty(PackageInfoTitle,name);
				path = packageProperties.getProperty(PackageInfoPath,name);
				xmlFile = packageDir + File.separator + packageProperties.getProperty(PackageInfoInputXML,name + ".xml");
				htmlTemplate = packageDir + File.separator+ packageProperties.getProperty(PackageInfoTemplate,File.separator+"presentation"+File.separator+name+".html");
				cssTemplate = packageDir + File.separator+ packageProperties.getProperty(PackageInfoCSS,File.separator+"presentation"+File.separator+name+".css");
				layout = packageDir + File.separator+ packageProperties.getProperty(PackageInfoLayout,File.separator+"presentation"+File.separator+name+".layout");
			} else {
				System.out.println("Usage: " + commandLine);
				System.exit(0);
			}
		}	
	}
		
	public static void main(String[] argv) {
		
		try {
			
			// Parse the arguments. 
			Args args = new Args(argv);
			
			Properties properties = org.dbwiki.lib.IO.loadProperties(args.configFile);
			_server = new WikiServer(properties);
			
			// attempt to generate a schema from the input file
			OutputStream out = null;
			File outputFile = new File(args.xmlFile);
			
			if (args.xmlFile.endsWith(".gz")) {
				out = new GZIPOutputStream(new FileOutputStream(outputFile));
			} else {
				out = new FileOutputStream(outputFile);
			}

			_wiki = _server.get(args.name);

			// TODO: Write out the xml
			NodeWriter writer = new ExportNodeWriter();
			BufferedWriter outstr = new BufferedWriter(new OutputStreamWriter(out));
			
			writer.init(outstr);
			// new NodeIdentifier() gives a root node id
			_wiki.database().export(new NodeIdentifier(), _wiki.database().versionIndex().getLastVersion().number(), writer);
			
			outstr.close();
			out.close();
			// register the database with the server
			
						
			// [wiki] should never be null
			assert(_wiki != null);
			
			// Obtain and save the presentation files.
			
			
			savePresentationFile(args.cssTemplate, _wiki.getCSSVersion(), PresentationFileType.CSS);
			savePresentationFile(args.htmlTemplate, _wiki.getTemplateVersion(), PresentationFileType.Template);
			savePresentationFile(args.layout, _wiki.getLayoutVersion(), PresentationFileType.Layout);

		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
}
