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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
	
	
	/*
	 * Constructors
	 */
	
	public User(int id, String login, String fullName, String password) {
		_fullName  = fullName;
		_id = id;
		_login = login;
		_password = password;
	}
	
	
	/*
	 * Public Methods
	 */
	
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
	
	public static List<User> readUsers(File file) throws IOException  {
		List<User> users = new ArrayList<User>();
		if (file != null) {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("#")) {
					StringTokenizer tokens = new StringTokenizer(line, "\t");
					String login = tokens.nextToken();
					String fullName = tokens.nextToken();
					String password = tokens.nextToken();
					users.add(new User(-1,login, fullName, password));
				}
			}
			in.close();
		}
		return users;
	}
	
}
