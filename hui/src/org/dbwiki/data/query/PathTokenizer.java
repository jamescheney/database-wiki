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
package org.dbwiki.data.query;

import java.util.Vector;

import org.dbwiki.exception.data.WikiQueryException;

/** 
 * A class to split a string into a sequence of tokens, each separated 
 * by "/" in the original string.
 * FIXME #query: Replace with an actual parser
 * @author jcheney
 *
 */
public class PathTokenizer {
	/*
	 * Private Variables
	 */
	
	private Vector<String> _tokens;
	
	
	/*
	 * Constructors
	 */
	
	public PathTokenizer(String path) throws org.dbwiki.exception.WikiException {
		if (path.startsWith("/")) {
			_tokens = new Vector<String>();
			StringBuffer buf = new StringBuffer();
			int index = 1;
			while (index < path.length()) {
				char c = path.charAt(index++);
				if (c == '/') {
					if (buf.length() > 0) {
						_tokens.add(buf.toString());
						buf = new StringBuffer();
					} else {
						throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path);
					}
				} 
				  else if (c == '[') {
					buf.append(c);
					int pos = this.parseCondition(path, index);
					buf.append(path.substring(index, pos + 1));
					index = pos + 1;
				} else if (c == ':') {
					buf.append(c);
					index++;
					if (c == ':') {
						buf.append(c);
					}
			//		_tokens.add(buf.toString());
			//		buf = new StringBuffer();
			//		int pos = this.parseAxis(path, index);
			//		buf.append(path.substring(0, index - 1));
			 		
			//		index = pos + 1;
				 	}
				else {
					buf.append(c);
				}
			}
			if (buf.length() > 0) {
				_tokens.add(buf.toString());
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String get(int index) {
		return _tokens.get(index);
	}
	
	public int size() {
		return _tokens.size();
	}
	
	
	/*
	 * Private Variables
	 */
	/** Parses a condition by finding where it ends.
	 * 
	 */
	private int parseCondition(String path, int index) throws org.dbwiki.exception.WikiException {
		int pos = path.indexOf('=', index);
		if (pos != -1) {
			try {
				while (path.charAt(pos) != '\'') {
					pos++;
				}
				pos++;
				while ((path.charAt(pos) != '\'') && (path.charAt(pos - 1) != '\\')) {
					pos++;
				}
				while (path.charAt(pos) != ']') {
					pos++;
				}
				return pos;
			} catch (ArrayIndexOutOfBoundsException exception) {
				throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path);
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path + "\t" + index);
		}
	}
	
	private int parseAxis(String path, int index) throws org.dbwiki.exception.WikiException {
		int pos = path.indexOf(':', index);
		if (pos != -1) {
			try {
				while (path.charAt(pos) != '\'') {
					pos++;
				}
				pos++;
				while ((path.charAt(pos) != '\'') && (path.charAt(pos - 1) != '\\')) {
					pos++;
				}
				while (path.charAt(pos) != ':') {
					pos++;
				}
				return pos;
			} catch (ArrayIndexOutOfBoundsException exception) {
				throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path);
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidPathExpression, path + "\t" + index);
		}
	}
	
	
}
