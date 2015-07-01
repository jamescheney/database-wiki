package org.dbwiki.data.security;

import java.util.ArrayList;
import java.util.HashMap;

import org.dbwiki.user.*;
import org.dbwiki.web.server.DatabaseWiki;

public class Role {

	private int _id;
	private String _name;
	private DatabaseWiki _wiki;
	private Permission _permission;
	private HashMap<Integer, Capability> _capabilities; // Entry ID and capabilities to this entry
	private ArrayList<Integer> _users; // user id and User, users who share same role
	private ArrayList<Integer> _conflictRoles; // roles that can not be assign at the same time
	
	public Role(int id, String name, DatabaseWiki wiki, Permission permission,
			HashMap<Integer, Capability> capabilities, ArrayList<Integer> users, ArrayList<Integer> conflictRoles) {
		
		_id = id;
		_name = name;
		_wiki = wiki;
		_permission = permission;
		_capabilities = capabilities;
		_users = users;
		_conflictRoles = conflictRoles;
	}
	
	public Role(int id, String name, DatabaseWiki wiki, Permission permission,
			HashMap<Integer, Capability> capabilities) {
		
		_id = id;
		_name = name;
		_wiki = wiki;
		_permission = permission;
		_capabilities = capabilities;
		_users = null;
		_conflictRoles = null;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public DatabaseWiki get_wiki() {
		return _wiki;
	}
	
	public Permission getpermission() {
		return _permission;
	}
	
	public ArrayList<Integer> getUsers() {
		return _users;
	}
	
	public boolean isCapabilityExist(int entryID) {
		return _capabilities.containsKey(entryID);
	}
	
	public Capability getCapability(int entryID) {
		return _capabilities.get(entryID);
	}
	
	public boolean isConflict(Role role) {
		return _conflictRoles.contains(role);
	}
	
	public void setCapability(int entryID, Capability capability) {
		_capabilities.put(entryID, capability);
	}
	
	public void addCapability(int entryID, Capability capability) {
		_capabilities.put(entryID, capability);
	}
	
	public void addUser(int userID) {
		_users.add(userID);
	}
	
	public void removeUser(User user) {
		_users.remove(user);
	}
	
	public void addConflictRoles(int roleID) {
		_conflictRoles.add(roleID);
	}
	
	public void removeConflictRoles(int role) {
		_conflictRoles.remove(role);
	}

}