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

public class WikiQueryException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8815L;

	public static final int DuplicateVariableDefinition = 125;
	public static final int InvalidIndexCondition       = 250;
	public static final int InvalidNIDQuery             = 500;
	public static final int InvalidPathExpression       = 575;
	public static final int InvalidQueryFormat          = 1000;
	public static final int InvalidQueryStatement       = 1250;
	public static final int InvalidWikiPathComponent    = 1500;
	public static final int InvalidWikiQuery            = 1625;
	public static final int QueryFormatNotSupported     = 1750;
	public static final int UnknownQueryFormat          = 2000;
	public static final int UnknownVariable             = 3000;
	
	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiQueryException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}
	
	public WikiQueryException(Exception exception) {
		super(exception);
		
		_errorCode = -1;
	}
	
	
	/*
	 * Public Methods
	 */
	
	@Override
	public String errorCodeMessage() {
		switch(_errorCode) {
		case DuplicateVariableDefinition:
			return " {Duplicate variable definition]";
		case InvalidIndexCondition:
			return " [Invalid node index value]";
		case InvalidNIDQuery:
			return " [Invalid node identifier format]";
		case InvalidPathExpression:
			return " [Invalid path expression]";
		case InvalidQueryFormat:
			return " [Invalid query string format]";
		case InvalidQueryStatement:
			return " [Invalid query statement]";
		case InvalidWikiPathComponent:
			return " [Invalid wiki-path component]";
		case InvalidWikiQuery:
			return " [Invalid wiki-path query format]";
		case QueryFormatNotSupported:
			return " [Query format not supported by database implementation]";
		case UnknownQueryFormat:
			return " [Unknown query format]";
		case UnknownVariable:
			return " [Unknown variable]";
		default:
			return "";
		}
	}

	@Override
	public String exceptionPrefix() {
		return "[QUERY]";
	}
}
