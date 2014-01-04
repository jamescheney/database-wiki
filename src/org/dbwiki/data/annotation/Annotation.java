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
package org.dbwiki.data.annotation;

import org.dbwiki.user.User;

/** A struct containing a date, id, annotaion text and user id.
 * 
 * @author jcheney
 *
 */
public class Annotation {
	/*
	 * Private Variables
	 */
	
	private String _date;
	private int _id;
	private String _text;
	private User _user;
	
	
	/*
	 * Constructors
	 */
	
	public Annotation(int id, String text, String date, User user) {
		_id = id;
		_text = text;
		_date = date;
		_user = user;
	}

	public Annotation(String text, String date, User user) {
		this(-1, text, date, user);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public int id() {
		return _id;
	}
	
	public String date() {
		return _date;
	}

	public String text() {
		return _text;
	}
	
	public User user() {
		return _user;
	}

	public boolean sameText(Object object) {
		if (!(object instanceof Annotation)) {
	        return false;
		}
		Annotation other = (Annotation) object;
		return (this._text.equals(other.text()));
	}

	@Override
	public String toString() {
		return _text;
	}
}
