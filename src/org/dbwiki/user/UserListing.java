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

import java.util.Hashtable;
import java.util.Vector;

public abstract class UserListing {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, User> _userIndex;
	private Vector<User> _userListing;
	
	
	/*
	 * Constructors
	 */
	
	public UserListing() {
		_userIndex = new Hashtable<String, User>();
		_userListing = new Vector<User>();
	}

	
	
	/*
	 * Public Methods
	 */
	
	public void add(User user) {
		_userIndex.put(user.login(), user);
		_userListing.add(user);
	}
	
	public boolean contains(String login) {
		return _userIndex.containsKey(login);
	}
	
	public User get(int userID) {
		if (userID != User.UnknownUserID) {
			for (int iUser = 0; iUser < _userListing.size(); iUser++) {
				if (_userListing.get(iUser).id() == userID) {
					return _userListing.get(iUser);
				}
			}
		}
		return null;
	}
	
	public User get(String login) {
		return _userIndex.get(login);
	}
	
	public boolean isEmpty() {
		return (_userListing.size() == 0);
	}
}
