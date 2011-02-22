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
package org.dbwiki.web.html;

import java.util.Iterator;
import java.util.Vector;

public class LineSet {
	/*
	 * Private Variables
	 */
	
	private String _indention;
	private Vector<String> _lines;
	
	
	/*
	 * Constructors
	 */
	
	public LineSet() {
		_indention = "";
		_lines = new Vector<String>();
	}
	
	public LineSet(String line) {
		this();
		
		this.add(line);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(String line) {
		_lines.add(_indention + line);
	}
	
	public void add(LineSet lines) {
		for (int iLine = 0; iLine < lines.size(); iLine++) {
			this.add(lines.get(iLine));
		}
	}
	
	public String get(int index) {
		return _lines.get(index);
	}
	
	public Iterator<String> iterator() {
		return _lines.iterator();
	}
	
	public void setIndention(String indention) {
		_indention = indention;
	}
	
	public int size() {
		return _lines.size();
	}
}
