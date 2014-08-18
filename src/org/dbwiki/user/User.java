package org.dbwiki.user;


/** An abstract class representing user information
 * 
 * @author o.cierny
 *
 */

public abstract class User {
	
	/*
	 * Publlic Variables
	 */
	
	public static final int UnknownUserID = -1;
	public static final String UnknownUserName = "Unknown";
	
	/*
	 * Protected Variables
	 */
	
	protected int _id;
	protected String _login;
	
	/*
	 * Constructors
	 */
	
	public User(int id, String login) {
		_id = id;
		_login = login;
	}
	
	/*
	 * Public Methods
	 */
	
	public int id() {
		return _id;
	}
	
	public String login() {
		return _login;
	}
	
	public abstract String fullName();

}
