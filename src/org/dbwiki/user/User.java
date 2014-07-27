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
package org.dbwiki.user;

/** A class representing user information, with fields:
 * @param _fullName - full name of the user
 * @param _id - user id (integer)
 * @param _login - login name of the user
 * @param _password - PLAIN TEXT password of the user 
 * 
 * TODO: make password handling more secure.
 * @author jcheney
 *
 */
public class User {
	/*
	 * Public Constants
	 */
	
	public static final int UnknownUserID = -1;
	
	public static final String UnknownUserName = "Unknown";
	
	
	/*
	 * Private Variables
	 */
	
	private String _fullName;
	private int _id;
	private String _login;
	private String _password;
	private boolean _is_admin;
	
	
	/*
	 * Constructors
	 */
	
	public User(int id, String login, String fullName, String password, boolean is_admin) {
		_fullName  = fullName;
		_id = id;
		_login = login;
		_password = password;
		_is_admin = is_admin;
	}
	
	
	/*
	 * Public Methods
	 */

	public void set_fullName(String _fullName) {
		this._fullName = _fullName;
	}


	public void set_id(int _id) {
		this._id = _id;
	}

	public void set_is_admin(boolean _is_admin) {
		this._is_admin = _is_admin;
	}

	public void set_login(String _login) {
		this._login = _login;
	}


	public void set_password(String _password) {
		this._password = _password;
	}

	public static int getUnknownuserid() {
		return UnknownUserID;
	}


	public static String getUnknownusername() {
		return UnknownUserName;
	}


	public String fullName() {
		return _fullName;
	}
	
	public int id() {
		return _id;
	}
	
	public String login() {
		return _login;
	}
	
	public String password() {
		return _password;
	}
	
	public boolean is_admin() {
		return _is_admin;
	}
}
