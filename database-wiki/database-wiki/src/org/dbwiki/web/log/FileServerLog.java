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
package org.dbwiki.web.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileServerLog extends ServerLog {
	/*
	 * Private Methods
	 */
	
	private File _logFile;
	private BufferedWriter _out;
	
	
	/*
	 * Constructors
	 */
	
	public FileServerLog(File logFile) {
		_logFile = logFile;
	}
	
	
	/*
	 * Public Methods
	 */

	public void closeLog() throws IOException {
		_out.close();
	}

	public void openLog() throws IOException {
		_out = new BufferedWriter(new FileWriter(_logFile));
	}

	public synchronized void writeln(String line) throws IOException {
		_out.write(line);
		_out.newLine();
	}
}
