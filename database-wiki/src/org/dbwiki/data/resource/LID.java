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
package org.dbwiki.data.resource;

import org.dbwiki.exception.data.WikiNodeException;

/** LIDs are pairs label:index, used as components in path identifiers
 * Currently not used!
 * @author jcheney
 *
 */
@Deprecated
public class LID {
	/*
	 * Private Variables
	 */
	
	private int _index;
	private String _label;
	
	
	/*
	 * Constructors
	 */
	
	public LID(String label, int index) {
		_label = label;
		_index = index;
	}

	public LID(int index) {
		this(null, index);
	}

	public LID(String text) throws org.dbwiki.exception.WikiException {
		int pos = text.indexOf(PID.NodeIdentifierKeyDelimiter);
		if (pos != -1) {
			_label = text.substring(0, pos);
			try {
				_index = Integer.parseInt(text.substring(pos + PID.NodeIdentifierKeyDelimiter.length()));
			} catch (NumberFormatException nfException) {
				throw new WikiNodeException(WikiNodeException.InvalidIdentifierFormat, text);
			}
		} else {
			try {
				_index = Integer.parseInt(text);
				_label = null;
			} catch (NumberFormatException nfException) {
				throw new WikiNodeException(WikiNodeException.InvalidIdentifierFormat, text);
			}
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean equals(LID lid) {
		if (this.index() == lid.index()) {
			if (this.label() != null) {
				return this.label().equals(lid.label());
			} else {
				return (lid.label() == null);
			}
		} else {
			return false;
		}
	}
	
	public int index() {
		return _index;
	}
	
	public String label() {
		return _label;
	}
	
	public String toURLString() {
		if (_label != null) {
			return _label + PID.NodeIdentifierKeyDelimiter + _index;
		} else {
			return Integer.toString(_index);
		}
	}
}
