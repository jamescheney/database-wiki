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
package org.dbwiki.web.ui;

import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.web.html.HtmlLinePrinter;
import org.dbwiki.web.html.HtmlPage;

import org.dbwiki.web.ui.printer.HtmlContentPrinter;

/** Class for generating HTML content to respond to an HTTP Request
 *  This is basically a symbol table, used to fill in the "@foo" symbols in 
 *  the templates used by DBWiki.  Some of the symbols have predefined behavior,  
 *  the rest are handled by symbol table entries.
 * 
 * 
 * @author jcheney
 *
 */

public abstract class HtmlContentGenerator {
	/*
	 * Public Constants
	 */
	/* Constant names of parts of template */
	public static final String ContentAnnotation   = "annotation";
	public static final String ContentContent      = "content";
	public static final String ContentCSS          = "css";
	public static final String ContentDatabaseLink = "database_home_link";
	public static final String ContentLogin        = "login";
	public static final String ContentMenu         = "menu";
	public static final String ContentObjectLink   = "object_path_link";
	public static final String ContentProvenance   = "provenance";
	public static final String ContentSearch       = "search";
	public static final String ContentTimemachine  = "timemachine";
	public static final String ContentTitle        = "title";
	public static final String ContentImport       = "import"; 
	
	/*
	 * Private Variables
	 */
	/** _contentPrinter maps keys to actual printers that will be used.  */
	private Hashtable<String, HtmlContentPrinter> _contentPrinter;
	
	
	/*
	 * Constructors
	 */
	
	
	public HtmlContentGenerator() {
		_contentPrinter = new Hashtable<String, HtmlContentPrinter>();

	}
	
	
	/*
	 * Public Methods
	 */
	
	/** Tests whether a content printer key is present in this generator */
	public boolean contains(String key) {
		return _contentPrinter.containsKey(key);
	}
	
	/** Adds a new HtmlContentPrinter @printer to the mapping with key @key 
	 */
	public void put(String key, HtmlContentPrinter printer) {
		_contentPrinter.put(key, printer);
	}

	/** Prints content with key key with arguments @args to a page @page, indenting by @indention
	 * TODO #htmlgeneration Consider moving indention into HtmlPage class, or using HtmlLinePrinter instead of HtmlPage.
	 * @param key - the name of the content generator to use
	 * @param args - the arguments to the content generator (unused here but used in subclasses)
	 * @param page - the HTML page to send the content to
	 * @param indention - the indentation prefix to use.  
	 * @throws org.dbwiki.exception.WikiException
	 */
	
	public void print(String key, Vector<String> args, HtmlPage page, String indention) throws org.dbwiki.exception.WikiException {

		if (_contentPrinter.containsKey(key)) {
			_contentPrinter.get(key).print(new HtmlLinePrinter(page, indention),args);	
		}
	}
}
