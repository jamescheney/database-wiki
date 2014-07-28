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
package org.dbwiki.exception.web;

import org.dbwiki.exception.WikiException;

public class WikiRequestException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8819L;

	public static final int DuplicateParameter          = 1000;
	public static final int FileNotFound				= 1250;
	public static final int InvalidParameterCombination = 1500;
	public static final int InvalidParameterFormat      = 2000;
	public static final int InvalidParameterValue       = 3000;
	public static final int InvalidRequest              = 3500;
	public static final int InvalidUrl                  = 3750;
	public static final int MissingParameterValue       = 4000;
	public static final int UnknownParameter            = 5000;
	public static final int UnknownWiki                 = 6000;
	
	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiRequestException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}

	
	/*
	 * Public Methods
	 */
	
	@Override
	public String errorCodeMessage() {
		switch (_errorCode) {
		case DuplicateParameter:
			return "[Duplicate request parameter]";
		case FileNotFound:
			return "[File not found]";
		case InvalidParameterCombination:
			return "[Invalid parameter combination in request]";
		case InvalidParameterFormat:
			return "[Invalid parameter format]";
		case InvalidParameterValue:
			return "[Invalid parameter value]";
		case InvalidRequest:
			return "[Invalid request]";
		case InvalidUrl:
			return "[Invalid request url]";
		case MissingParameterValue:
			return "[Missing parameter value]";
		case UnknownParameter:
			return "[Unknown parameter value]";
		case UnknownWiki:
			return "[Unknown Database Wiki]";
		default:
			return "";
		}
	}

	@Override
	public String exceptionPrefix() {
		return "[HTTP REQUEST]";
	}
}
