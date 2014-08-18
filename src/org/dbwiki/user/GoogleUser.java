package org.dbwiki.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** A class representing a Google user, with fields:
 * @param _level - permission level of user
 * 
 * @author o.cierny
 * 
 */

public class GoogleUser extends User {
	
	/*
	 * Private Variables
	 */
	
	private int _level;
	
	/*
	 * Constructors
	 */
	
	public GoogleUser(int id, String login, int level) {
		super(id, login);
		_level = level;
	}
	
	/*
	 * Public Methods
	 */
	
	public int level() {
		return _level;
	}
	
	public String fullName() {
		return _login.substring(0, _login.indexOf('@'));
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
					int level = Integer.parseInt(tokens.nextToken());
					users.add(new GoogleUser(UnknownUserID, login, level));
				}
			}
			in.close();
		}
		return users;
	}
}
