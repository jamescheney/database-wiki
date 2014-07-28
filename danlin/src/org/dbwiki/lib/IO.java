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
package org.dbwiki.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Static methods for reading and writing properties files
 * FIXME #properties Make classes representing the arguments loaded in from properties.
 * @author jcheney
 *
 */
public final class IO {
	/*
	 * Static Methods
	 */
	
	public static Properties loadProperties(File file) throws java.io.IOException {
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(file);
		properties.load(in);
		in.close();
		return properties;
	}
	
	public static void writeProperties(Properties properties, File file) throws java.io.IOException {
		FileOutputStream out = new FileOutputStream(file);
		properties.store(out, "WikiDatabase Property Listing");
		out.close();
	}
}
