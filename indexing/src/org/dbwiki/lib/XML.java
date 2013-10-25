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

/** Some static utility functions for XML strings
 * 
 * @author jcheney
 *
 */
public final class XML {
	/*
	 * Public Methods
	 */
	
	public static int indexOfFirstSpecialChar(char[] text) {
		for (int iChar = 0; iChar < text.length; iChar++) {
			if (isSpecialChar(text[iChar])) {
				return iChar;
			}
		}
		return -1;
	}

	public static boolean isSpecialChar(char c) {
		if((c < 32) || (c == '&') || (c == '<') || (c == '>') || (c == '"') || (c == '\'') || (c == '\n') || (c == '\r')) {
			return true;
		}
		return false;
	}
	
	public static String maskChar(char c) {
		switch (c) {
		case '&':
			return "&amp;";
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '"':
			return "&quot;";
		case '\'':
			return "&apos;";
		case '\r':
			return "\r";
		case '\n':
			return "\n";
		default:
			return String.valueOf(c);
		}
	}

	public static String maskText(String value) {
		if (value != null) {
			char[] text = value.toCharArray();
			int pos = -1;
			if ((pos = indexOfFirstSpecialChar(text)) != -1) {
				StringBuilder buffer = new StringBuilder();
				for (int iChar = pos; iChar < text.length; iChar++) {
					char c = text[iChar];
					if (c == '\t') {
						buffer.append("&#009;");
					} else if (c == '\n') {
						buffer.append("&#010;");
					} else if (c == '\r') {
						buffer.append("&#013;");
					} else if (c == '&') {
						buffer.append("&amp;");
					} else if (c == '<') {
						buffer.append("&lt;");
					} else if (c == '>') {
						buffer.append("&gt;");
					} else if (c == '"') {
						buffer.append("&quot;");
					} else if (c == '\'') {
						buffer.append("&apos;");
					} else if (c >= 32) {
						buffer.append(c);
					}
				}
				char[] result = new char[pos + buffer.length()];
				for (int iChar = 0; iChar < pos; iChar++) {
					result[iChar] = text[iChar];
				}
				for (int iChar = 0; iChar < buffer.length(); iChar++) {
					result[pos + iChar] = buffer.charAt(iChar);
				}
				return new String(result);
			} else {
				return new String(text);
			}
		} else {
			return value;
		}
	}
}
