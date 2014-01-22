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
package org.dbwiki.data.database;

import java.util.Vector;

/** A vector of timestamped values representing the possible values of an attribute over time.
 * 
 * @author jcheney
 *
 */
public class DatabaseNodeValue {
	/*
	 * Private Variables
	 */
	
	private Vector<DatabaseTextNode> _values;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseNodeValue() {
		_values = new Vector<DatabaseTextNode>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(DatabaseTextNode value) {
		_values.add(value);
	}
	
	public DatabaseTextNode get(int index) {
		return _values.get(index);
	}

	/**
	 * Returns null if attribute is deleted.
	 */
	public DatabaseTextNode getCurrent() {
		for (int iValue = 0; iValue < _values.size(); iValue++) {
			DatabaseTextNode text = _values.get(iValue);
			if (text.getTimestamp().isCurrent()) {
				return text;
			}
		}
		return null;
	}
	
	public DatabaseTextNode getMostRecent() {
		if (_values.size() > 0) {
			if (this.size() > 1) {
				DatabaseTextNode result = _values.get(0);
				if (!result.getTimestamp().isCurrent()) {
					for (int iValue = 1; iValue < _values.size(); iValue++) {
						DatabaseTextNode value = _values.get(iValue);
						if (value.getTimestamp().isCurrent()) {
							return value;
						} else if (value.getTimestamp().lastValue() > result.getTimestamp().lastValue()) {
							result = value;
						}
					}
				}
				return result;
			} else {
				return _values.get(0);
			}
		} else {
			return null;
		}
	}

	public String getValueAt(int version) {
		for (int iValue = 0; iValue < _values.size(); iValue++) {
			DatabaseTextNode text = _values.get(iValue);
			if (text.getTimestamp().contains(version)) {
				return text.value();
			}
		}
		return "";
	}

	public void remove(int index) {
		_values.remove(index);
	}
	
	public int size() {
		return _values.size();
	}
}
