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
package org.dbwiki.exception;

/** A fatal exception
 * 
 * @author jcheney
 *
 */
public class WikiFatalException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8812L;

	public static final int InvalidFileFormat     = 1000;
	public static final int InvalidMethodCall = 2000;
	
	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiFatalException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}

	public WikiFatalException(String message) {
		super(message);
		
		_errorCode = -1;
	}
	
	public WikiFatalException(Exception exception) {
		super(exception);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public String errorCodeMessage() {
		switch(_errorCode) {
		case InvalidFileFormat:
			return "[Invalid file format]";
		case InvalidMethodCall:
			return "[Invalid method call]";
		default:
			return "";
		}
	}

	public String exceptionPrefix() {
		return "[FATAL]";
	}
}
