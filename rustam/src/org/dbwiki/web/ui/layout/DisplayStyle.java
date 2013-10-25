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
package org.dbwiki.web.ui.layout;

public class DisplayStyle {
	/*
	 * Public Constants
	 */
	
	public static final String styleGroup = "group";
	public static final String styleList  = "list";
	public static final String styleTable = "table";

	public static final String DefaultStyle = styleList;
	
	
	/*
	 * Private Variables
	 */
	
	private String _style;
	
	
	/*
	 * Constructors
	 */
	
	public DisplayStyle(String style) {
		if ((!styleGroup.equalsIgnoreCase(style)) && (!styleList.equalsIgnoreCase(style)) && (!styleTable.equalsIgnoreCase(style))) {
			_style = DefaultStyle;
		} else {
			_style = style;
		}
	}
	
	public DisplayStyle() {
		this("");
	}
	
	/*
	 * Public Methods
	 */
	
	public boolean isGroupStyle() {
		return (styleGroup.equalsIgnoreCase(_style));
	}
	
	public boolean isListStyle() {
		return (styleList.equalsIgnoreCase(_style));
	}
	
	public boolean isTableStyle() {
		return (styleTable.equalsIgnoreCase(_style));
	}
}
