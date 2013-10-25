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

import org.dbwiki.data.schema.Entity;

/** Alignment of labels in data rendering.  Left, none or top.
 * 
 * @author jcheney
 *
 */
public class LabelAlignment {
	/*
	 * Private Constants
	 */
	
	private static final String alignLeft = "left";
	private static final String alignNone = "none";
	private static final String alignTop = "top";
	
	
	/*
	 * Public Constants
	 */
	
	public static final String DefaultAlignment = alignLeft;
	
	
	/*
	 * Private Variables
	 */
	
	private String _alignment;
	
	
	/*
	 * Constructors
	 */
	
	public LabelAlignment(String alignment) {
		if ((!alignLeft.equalsIgnoreCase(alignment)) && (!alignNone.equalsIgnoreCase(alignment)) && (!alignTop.equalsIgnoreCase(alignment))) {
			_alignment = DefaultAlignment;
		} else {
			_alignment = alignment;
		}
	}
	
	public LabelAlignment(Entity entity) {
		if (entity.isAttribute()) {
			_alignment = alignLeft;
		} else {
			_alignment = alignNone;
		}
	}
	
	
	
	/*
	 * Public Methods
	 */
	
	public boolean isLeftAlign() {
		return (alignLeft.equalsIgnoreCase(_alignment));
	}
	
	public boolean isNoneAlign() {
		return (alignNone.equalsIgnoreCase(_alignment));
	}
	
	public boolean isTopAlign() {
		return (alignTop.equalsIgnoreCase(_alignment));
	}
}
