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
package org.dbwiki.driver.rdbms;

import java.util.Vector;

public class DatabaseQuery {
	/*
	 * Private Variables
	 */
	
	private Vector<String> _keywords;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseQuery(String query) {
		_keywords = new Vector<String>();
		
		String text = query.replace(';', ' ').replace('\'', ' ').trim();
		int pos = -1;
		
		while ((pos = this.nextDelimiter(text)) != -1) {
			String kw = "";
			if (text.charAt(pos) == ' ') {
				kw = text.substring(0, pos).trim();
				text = text.substring(pos + 1).trim();
			} else {
				int posClose = text.indexOf('"', pos + 1);
				if (posClose != -1) {
					if (pos > 0) {
						kw = text.substring(0, pos -1);
						if (!kw.equals("")) {
							_keywords.add(kw);
						}
					}
					kw = text.substring(pos + 1, posClose);
					text = text.substring(posClose + 1).trim();
				} else {
					text.replace('"', ' ');
				}
			}
			if (!kw.equals("")) {
				_keywords.add(kw);
			}
		}
		if (!text.equals("")) {
			_keywords.add(text);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String get(int index) {
		return _keywords.get(index);
	}
	
	public int size() {
		return _keywords.size();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private int nextDelimiter(String text) {
		int posSP = text.indexOf(' ');
		int posQM = text.indexOf('"');
		
		if ((posSP == -1) && (posQM == -1)) {
			return -1;
		} else if ((posSP != -1) && (posQM == -1)) {
			return posSP;
		} else if ((posSP == -1) && (posQM != -1)) {
			return posQM;
		} else {
			return Math.min(posSP, posQM);
		}
	}
}
