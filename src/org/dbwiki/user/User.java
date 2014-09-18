package org.dbwiki.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/** An abstract class representing user information
 * 
 * @author o.cierny
 *
 */

public class User {
	
	/*
	 * Publlic Variables
	 */
	
	public static final int UnknownUserID = -1;
	public static final String UnknownUserName = "Unknown";
	
	/*
	 * Protected Variables
	 */
	
	private int _id;
	private String _login;
	private String _fullName;
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
	
	public int id() {
		return _id;
	}
	
	public String login() {
		return _login;
	}
	
	public String fullName() {
		return _fullName;
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
					String is_admin = tokens.nextToken();
					users.add(new User(UnknownUserID, login, fullName, password, is_admin.equals("true") ? true : false));
				}
			}
			in.close();
		}
		return users;
	}
	
	public boolean is_admin() {
		return _is_admin;
	}
	
	public void set_is_admin(boolean _is_admin) {
		this._is_admin = _is_admin;
	}
	
	public void set_fullName(String _fullName) {
		this._fullName = _fullName;
	}

}
