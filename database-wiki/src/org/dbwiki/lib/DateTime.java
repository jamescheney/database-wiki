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

package org.dbwiki.lib;

import java.text.SimpleDateFormat;

import java.util.Date;

/** Static utility functions for transforming strings into date
 * objects and vice versa
 * 
 * @author hmueller
 *
 */

public final class DateTime {

	/*
	 * Static Methods
	 */
	
	public static Date getDate(String value) throws java.text.ParseException {
		
		String dateValue = value;
		
		int pos = dateValue.indexOf(' ');
		if (pos > 0) {
			String time = value.substring(pos + 1).trim();
			while (time.length() < 9) {
				time = time + "0";
			}
			dateValue = value.substring(0, pos) + " " + time;
		} else {
			dateValue = dateValue + " 000000000";
		}
		return new SimpleDateFormat("yyyy-MM-dd HHmmssSSS").parse(dateValue);
	}
	
	public static String getValue(Date date) {
		
		return new SimpleDateFormat("yyyy-MM-dd HHmmssSSS").format(date);
	}
}
