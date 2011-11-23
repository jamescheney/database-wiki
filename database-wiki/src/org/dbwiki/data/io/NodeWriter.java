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
package org.dbwiki.data.io;

import java.io.BufferedWriter;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.exception.WikiFatalException;

/** Abstract class providing the capability to write a node out as XML/JSON.
 * Instantiated by ExportNodeWriter and CopyPasteNodeWriter
 * 
 * @author jcheney
 *
 */

public abstract class NodeWriter {
	/*
	 * Private Variables
	 */
	
	private BufferedWriter _out;
		
	
	/*
	 * Abstract Methods
	 */
	
	public abstract void writeInit() throws java.io.IOException;
	public abstract void endDatabase(Database database) throws org.dbwiki.exception.WikiException;
	public abstract void endGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException;
	public abstract void startDatabase(Database database, int version) throws org.dbwiki.exception.WikiException;
	public abstract void startGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException;
	public abstract void writeAttributeNode(DatabaseAttributeNode node, DatabaseTextNode value) throws org.dbwiki.exception.WikiException;
	public abstract void writeTextNode(DatabaseTextNode node) throws org.dbwiki.exception.WikiException;
	
	
	/*
	 * Public Methods
	 */
	
	/*
	 * Public Methods
	 */
	
	public void init(BufferedWriter out) throws org.dbwiki.exception.WikiException {
		_out = out;
		
		try {
			writeInit();
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public void write(String value) throws java.io.IOException {
		_out.write(value);
	}
	
	public void writeln(String value) throws java.io.IOException {
		_out.write(value);
		_out.newLine();
	}
}
