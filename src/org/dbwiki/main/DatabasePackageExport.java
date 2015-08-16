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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;


import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.io.ExportNodeWriter;
import org.dbwiki.data.io.NodeWriter;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.wiki.DatabaseWikiPage;
import org.dbwiki.data.wiki.WikiPageDescription;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.server.WikiServerStandalone;
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
 * URL encoding
 * User 
 * 
 * Alternative usage:
 * Config file
 * Path to a directory that contains a "package descriptor", which is a properties file providing the above arguments.
 * FIXME #import: Refactor this and DatabasePackageImport and ImportPresentationFiles to avoid duplication.
 * TODO: Export wiki data (eventually including page history!)
 * TODO: Export full history, provenance, and annotations 
 * TODO: Make wiki and presentation import/export more modular - via wiki and presentation subdirectories with naming conventions
 * @author jcheney
 *
 */

/* Design of existing packages
 * 
 * A package is a directory that contains:
 * 
 * pkginfo - properties file specifying 
 *   NAME - short name
 *   TITLE - long name
 *   INPUT_XML - raw or zipped xml file with data
 *   PATH - path to entries in XML file 
 * 	 TEMPLATE - filename of template
 *   CSS - filename of CSS file
 *   LAYOUT - filename of layout, XML serialization 
 *   URLDECODING - filename of URL decoding rules file
 *   WIKI - directory name for wiki pages.
 *   
 *   The wiki subdirectory contains:
 * 
 * page1.xml ... page_n.xml
 * 
 * i.e. one file for each wiki page, tagged by id.   
 */
 

/* Design of new package design
 * 
 * A package is a directory that contains:
 * 
 * pkginfo - properties file specifying 
 *   NAME - short name
 *   TITLE - long name
 *   INPUT_XML - raw or zipped xml file with data
 *   PATH - path to entries in XML file 
 * 	 PRESENTATION - name of directory containing presentation files, by defaule "presentation"
 *   WIKI - name of directory containing wiki files, by default "wiki"
 * 
 * along with the named xml file and subdirectories.
 * 
 * The presentation file subdirectory contains:
 * pkginfo - properties file specifying:
 *   TEMPLATE - filename of template
 *   CSS - filename of CSS file
 *   LAYOUT - filename of layout, XML serialization 
 *   URLDECODING - filename of URL decoding rules file
 * along with the named files.
 * 
 * The wiki subdirectory contains:
 * 
 * page1.xml ... page_n.xml
 * 
 * i.e. one file for each wiki page, tagged by id.   
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
	private static String PackageInfoURLDecoding = "URLDECODING";
	private static String PackageInfoLayout = "LAYOUT";
	private static String PackageInfoWiki = "WIKI";
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
			case URLDecoding:
				configFile = _server.getURLDecoding(_wiki, version);
				break;
			default:
				assert(false);
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
		String urldecoding = null;
		String layout = null;
		String wikiDir = null;
		
		public Args (String[] args) throws IOException {
			configFile = new File(args[0]);
			if(args.length == 10) {
				name = args[1];
				title = args[2];
				path = args[3];
				xmlFile = args[4];
				htmlTemplate = args[5];
				cssTemplate = args[6];
				layout = args[7];
				urldecoding = args[8];
				wikiDir = args[9];
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
				// TODO: Make this create new pkginfo if the file is missing.
				Properties packageProperties = org.dbwiki.lib.IO.loadProperties(packageFile);
				name = packageProperties.getProperty(PackageInfoName);
				title = packageProperties.getProperty(PackageInfoTitle,name);
				path = packageProperties.getProperty(PackageInfoPath,name);
				xmlFile = packageDir + File.separator + packageProperties.getProperty(PackageInfoInputXML,name + ".xml");
				htmlTemplate = packageDir + File.separator+ packageProperties.getProperty(PackageInfoTemplate,File.separator+"presentation"+File.separator+name+".html");
				cssTemplate = packageDir + File.separator+ packageProperties.getProperty(PackageInfoCSS,File.separator+"presentation"+File.separator+name+".css");
				urldecoding = packageDir +  File.separator+ packageProperties.getProperty(PackageInfoURLDecoding,File.separator+"presentation"+File.separator+name+".urldecoding");
				layout = packageDir + File.separator + packageProperties.getProperty(PackageInfoLayout,File.separator+"presentation"+File.separator+name+".layout");
				wikiDir = packageDir + File.separator + packageProperties.getProperty(PackageInfoWiki,"wiki");
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

			_server = new WikiServerStandalone(properties);
			
			// attempt to generate a schema from the input file
			OutputStream out = null;
			File outputFile = new File(args.xmlFile);
			
			if (args.xmlFile.endsWith(".gz")) {
				out = new GZIPOutputStream(new FileOutputStream(outputFile));
			} else {
				out = new FileOutputStream(outputFile);
			}

			_wiki = _server.get(args.name);

			// Write out the xml
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
			savePresentationFile(args.urldecoding, _wiki.getURLDecodingVersion(), PresentationFileType.URLDecoding);

			//TODO: Move this to Wiki class.
			// Save the wiki pages to WIKI page path.
			assert(_wiki.wiki() != null);
			DatabaseContent wikiContent = _wiki.wiki().content();
			File wikiDirFile = new File(args.wikiDir);
			// Ensure directory for wiki pages exists
			if(!wikiDirFile.exists()) {
				System.err.println("Creating wiki directory " + args.wikiDir);
				wikiDirFile.mkdir();
			}
			if(wikiDirFile.isDirectory()) { 
				for(int i = 0; i < wikiContent.size(); i++) {
					WikiPageDescription wikiEntry = (WikiPageDescription)wikiContent.getByIndex(i);
					
					File wikiFile = new File(args.wikiDir+ File.separator + "page_" + i +".xml" );
					OutputStream wikioutstream = new FileOutputStream(wikiFile);
					OutputStreamWriter wikiout = new OutputStreamWriter(wikioutstream);
					
					DatabaseWikiPage content = _wiki.wiki().get(wikiEntry.identifier());
					content.write(wikiout);
					wikiout.close();
					wikioutstream.close();
				}
			} else {
				throw new Exception("Wiki directory path is not a directory");
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
}
