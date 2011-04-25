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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.URL;

import java.util.StringTokenizer;
import java.util.Vector;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.web.html.HtmlPage;

public class HtmlTemplateDecorator {
	/*
	 * Private Constants
	 */
	
	private static final String ContentIndicator   = "@";
	private static final String ConditionIndicator = "if ";
	private static final String ConditionEnd       = "}";
	private static final String ConditionStart     = "{";


	/*
	 * Public Methods
	 */
	
	public HtmlPage decorate(BufferedReader in, HtmlContentGenerator contentPrinters) throws org.dbwiki.exception.WikiException {
		HtmlPage page = new HtmlPage();
		
		boolean skipping = false;
		String line;
		
		try {
			while ((line = in.readLine()) != null) {
				if ((!skipping) && (line.trim().startsWith(ContentIndicator))) {
					String indention = line.substring(0, line.indexOf(ContentIndicator));
					String key = line.trim().substring(ContentIndicator.length());
					Vector<String> args = null;
					int pos = key.indexOf("(");
					if (pos != -1) {
						args = this.getArguments(key, pos);
						key = key.substring(0, pos).trim();
					}
					contentPrinters.print(key, args, page, indention);
				} else if ((!skipping) && (line.trim().startsWith(ConditionIndicator)) && (line.trim().endsWith(ConditionStart))) {
					String condition = line.trim().substring(ConditionIndicator.length()).trim();
					if (condition.startsWith(ContentIndicator)) {
						String key = condition.substring(ContentIndicator.length(), condition.length() - ConditionStart.length()).trim();
						skipping = !contentPrinters.contains(key);
					}
				} else if (line.trim().equals(ConditionEnd)) {
					skipping = false;
				} else if (!skipping) {
					page.add(line);
				}			
			}
			in.close();
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
		return page;
	}
	
	public HtmlPage decorate(File file, HtmlContentGenerator contentPrinters) throws org.dbwiki.exception.WikiException {
		try {
			return this.decorate(new BufferedReader(new FileReader(file)), contentPrinters);
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	public HtmlPage decorate(URL url, HtmlContentGenerator contentPrinters) throws org.dbwiki.exception.WikiException {
		try {
			return this.decorate(new BufferedReader(new InputStreamReader(url.openStream())), contentPrinters);
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public HtmlPage decorate(String template, HtmlContentGenerator contentPrinters) throws org.dbwiki.exception.WikiException {
		return this.decorate(new BufferedReader(new StringReader(template)), contentPrinters);
	}


	
	/*
	 * Private Methods
	 */
	
	private Vector<String> getArguments(String line, int pos) {
		Vector<String> args = null;
		if (pos != -1) {
			String parameterList = line.substring(pos + 1, line.length() - 1).trim();
			args = new Vector<String>();
			StringTokenizer tokens = new StringTokenizer(parameterList, ",");
			while (tokens.hasMoreTokens()) {
				args.add(tokens.nextToken().trim());
			}
		}
		return args;
	}
}
