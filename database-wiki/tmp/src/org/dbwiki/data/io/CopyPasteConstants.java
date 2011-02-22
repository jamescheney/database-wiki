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

public interface CopyPasteConstants {
	/*
	 * Public Constants
	 */

	public static final String AttributeLabelDatabaseName = "name";
	public static final String AttributeLabelEntityName   = "label";
	public static final String AttributeLabelID           = "id";
	public static final String AttributeLabelType         = "type";
	public static final String AttributeLabelVersion      = "version";
	
	public static final String ElementLabelDatabase = "database";
	public static final String ElementLabelNode     = "node";
	
	public static final int NodeTypeAttribute = 0;
	public static final int NodeTypeGroup     = 1;
	public static final int NodeTypeText      = 2;
}
