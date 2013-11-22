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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dbwiki.exception.data;

import org.dbwiki.exception.WikiException;

/**
 *
 * @author Tom
 */
public class WikiSecurityException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8816L;

	public static final int AuthenticationFailure  = 1000;

	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiSecurityException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}

	
	/*
	 * Public Methods
	 */
	
	@Override
	public String errorCodeMessage() {
		switch(_errorCode) {
		case AuthenticationFailure:
			return "[Authentication failure for requested operation]";
		default:
			return "";
		}
	}

	@Override
	public String exceptionPrefix() {
		return "[SECURITY]";
	}

}
