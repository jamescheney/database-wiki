package org.dbwiki.data.security;

import java.util.ArrayList;
import java.util.HashMap;

import org.dbwiki.user.*;
import org.dbwiki.web.server.DatabaseWiki;

public class Role {
	public static final int ownerID = 0;
	public static final int assistantID = -1;
	
	private int _id;
	private String _name;
	private DatabaseWiki _wiki;
	private Permission _permission;
	private HashMap<Integer, Capability> _capabilities; // Entry ID and capabilities to this entry
	private ArrayList<Integer> _users; // user id and User, users who share same role
	private ArrayList<Integer> _mutexRoles; // roles that can not be assign at the same time
	private ArrayList<Integer> _superRoles;
	
	public Role(int id, String name, DatabaseWiki wiki, Permission permission,
			HashMap<Integer, Capability> capabilities, ArrayList<Integer> users, ArrayList<Integer> mutexRoles, ArrayList<Integer> superRoles) {
		_id = id;
		_name = name;
		_wiki = wiki;
		_permission = permission;
		_capabilities = capabilities;
		_users = users;
		_mutexRoles = mutexRoles;
		_superRoles = superRoles;
	}
	
	public Role(int id, String name, DatabaseWiki wiki, Permission permission,
			HashMap<Integer, Capability> capabilities) {
		_id = id;
		_name = name;
		_wiki = wiki;
		_permission = permission;
		_capabilities = capabilities;
		_users = null;
		_mutexRoles = null;
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
	
	public boolean isMutex(int roleID) {
		return _mutexRoles.contains(roleID);
	}
	
	public boolean hasSuperRole(int roleID) {
		return _superRoles.contains(roleID);
	}
	
	public ArrayList<Integer> getMutexRoles() {
		return _mutexRoles;
	}
	
	public ArrayList<Integer> getSuperRoles() {
		return _superRoles;
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
		_mutexRoles.add(roleID);
	}
	
	public void removeConflictRoles(int role) {
		_mutexRoles.remove(role);
	}

}