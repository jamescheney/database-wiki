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
package org.dbwiki.data.document;


/** Intermediate class for element nodes.  Provides string label.
 * 
 * @author jcheney
 *
 */
public abstract class PasteElementNode extends PasteNode {
	/*
	 * Private Variables
	 */
	
	private String _label;
	
	
	/*
	 * Constructors
	 */
	
	public PasteElementNode(PasteDatabaseInfo database, String label) {
		super(database);
		
		_label = label;
	}

	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isAttribute();

	
	/*
	 * Public Methods
	 */
	
	public boolean isElement() {
		return true;
	}
	
	public boolean isGroup() {
		return !this.isAttribute();
	}
	
	public String label() {
		return _label;
	}
	
	public String toString() {
		return _label;
	}
}
