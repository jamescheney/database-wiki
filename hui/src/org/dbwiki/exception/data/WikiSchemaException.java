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
package org.dbwiki.exception.data;

import org.dbwiki.exception.WikiException;

public class WikiSchemaException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8816L;

	public static final int DuplicateSchemaNode   = 1000;
	public static final int InvalidSchemaType = 2000;
	public static final int SyntaxError       = 3000;
	public static final int UnknownSchemaNode     = 4000;
	
	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiSchemaException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}
	
	public WikiSchemaException(Exception exception) {
		super(exception);
		
		_errorCode = -1;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String errorCodeMessage() {
		switch(_errorCode) {
		case DuplicateSchemaNode:
			return "[Duplicate schema node in list]";
		case InvalidSchemaType:
			return "[Invalid schema node type]";
		case SyntaxError:
			return "[Syntax error]";
		case UnknownSchemaNode:
			return "[Unknown schema node]";
		default:
			return "";
		}
	}

	public String exceptionPrefix() {
		return "[SCHEMA]";
	}
}
