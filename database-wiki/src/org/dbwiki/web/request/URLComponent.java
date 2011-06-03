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
package org.dbwiki.web.request;

import java.net.URLDecoder;

public class URLComponent {
	/*
	 * Private Variables
	 */
	
	private String _decodedText;
	private String _encodedText;
	
	
	/*
	 * Constructors
	 */
	
	public URLComponent(String text) {
		_encodedText = text;
		try {
			_decodedText = URLDecoder.decode(text, "UTF-8");
		} catch (java.io.UnsupportedEncodingException uee) {
			_decodedText = null;
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String decodedText() {
		return _decodedText;
	}
	
	public String encodedText() {
		return _encodedText;
	}
}
