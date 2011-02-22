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
package org.dbwiki.data.io;

import java.util.Stack;

import org.dbwiki.exception.data.WikiDataException;

public class NodeValueReader {
	/*
	 * Private Variables
	 */
	
	private Stack<String> _labelStack;
	private String _value;
	
	
	/*
	 * Constructors
	 */
	
	public NodeValueReader() {
		_labelStack = new Stack<String>();
		_value = "";
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void endElement() throws org.dbwiki.exception.WikiException {
		if (!_labelStack.isEmpty()) {
			_value = _value + "</" + _labelStack.pop() + ">";
		} else {
			throw new WikiDataException(WikiDataException.InvaldInputData, "Unmatched end element event in node value");
		}
	}
	
	public boolean hasOpenElements() {
		return (!_labelStack.isEmpty());
	}
	
	public void startElement(String label) {
		_value = _value + "<" + label + ">";
		_labelStack.push(label);
	}
	
	public void text(String value) throws org.dbwiki.exception.WikiException {
		_value = _value + value;
	}
	
	public String value() throws org.dbwiki.exception.WikiException {
		if (_labelStack.isEmpty()) {
			return _value;
		} else {
			throw new WikiDataException(WikiDataException.InvaldInputData, "Open elements at end of node value");
		}
	}
}
