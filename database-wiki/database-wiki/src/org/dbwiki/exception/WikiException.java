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

public abstract class WikiException extends Exception {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8811L;

	
	/*
	 * Private  Variables
	 */
	
	private Exception _exception;
	private String _message;

	
	/*
	 * Constructors
	 */
	
	public WikiException(String message) {
		_message = message;
		
		_exception = null;
	}
	
	public WikiException(Exception exception) {
		_exception = exception;
		
		_message = null;
	}
	
	
	/*
	 * Abstract Method
	 */
	
	public abstract String exceptionPrefix();
	public abstract String errorCodeMessage();
	
	
	/*
	 * Public Methods
	 */
	
	public void printStackTrace() {
		if (_exception != null) {
			_exception.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}
	
	public String toString() {
		if (_exception == null) {
			String errorCodeMsg = this.exceptionPrefix() + " " + this.errorCodeMessage();
			if (_message != null) {
				return errorCodeMsg.trim() + ": " + _message;
			} else {
				return errorCodeMsg.trim();
			}
		} else {
			return this.exceptionPrefix() + " " + _exception.toString() + " in " + _exception.getStackTrace()[0].toString();
		}
	}
}
