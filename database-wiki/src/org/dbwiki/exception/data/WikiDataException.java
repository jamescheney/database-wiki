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

public class WikiDataException extends WikiException {
	/*
	 * Public Constants
	 */
	
	public static final long serialVersionUID = 8813L;

	public static final int DuplicateNodeID    = 1000;
	public static final int InactiveParent     = 1250;
	public static final int InvaldIdentifier   = 1500;
	public static final int InvaldIndexValue   = 2000;
	public static final int InvaldInputData    = 3000;
	public static final int UnexcpectedEOD     = 3500;
	public static final int UnknownDriverName  = 3750;
	public static final int UnknownNodeID      = 4000;
	public static final int InvalidPasteTarget = 4500;
	public static final int UnknownSchemaNode      = 4750;
	public static final int UnknownResource    = 5000;

	
	/*
	 * Private Variables
	 */
	
	private int _errorCode;
	
	
	/*
	 * Constructors
	 */
	
	public WikiDataException(int errorCode, String message) {
		super(message);
		
		_errorCode = errorCode;
	}

	
	/*
	 * Public Methods
	 */
	
	public String errorCodeMessage() {
		switch(_errorCode) {
		case DuplicateNodeID:
			return "[Duplicate node identifier]";
		case InactiveParent:
			return "[Inactive parent node]";
		case InvaldIdentifier:
			return "[Invalid resource identifier]";
		case InvaldIndexValue:
			return "[Invalid node index value]";
		case InvaldInputData:
			return "[Invalid input data]";
		case InvalidPasteTarget:
			return "[Invalid target node type for copy/paste operation]";
		case UnexcpectedEOD:
			return "[Unexpected end of document]";
		case UnknownDriverName:
			return "[Unknown driver name]";
		case UnknownNodeID:
			return "[Unknown node identifier]";
		case UnknownSchemaNode:
			return "[Unknown schema node]";
		case UnknownResource:
			return "[Unknown resource]";
		default:
			return "";
		}
	}

	public String exceptionPrefix() {
		return "[DATA]";
	}

}
