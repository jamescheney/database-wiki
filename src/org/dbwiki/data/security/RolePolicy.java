package org.dbwiki.data.security;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServerConstants;

public class RolePolicy extends SimplePolicy {

	private TreeMap<Integer, Role> _roleMap; 
	private TreeMap<Integer, ArrayList<Integer>> _userRoleMap; 
	private ArrayList<Pair<Integer, Integer>> _mutexRolePairs;
	private DatabaseWiki _wiki;

	public RolePolicy(int mode, DatabaseWiki wiki) {
		super(mode);
		_wiki = wiki;
	}

	public void initialize(Connection connection) throws SQLException {
		initMutexRolePairs(connection);
		initRoleMap(connection);
		initUserRoleMap(connection);
	}

	protected void initRoleMap(Connection connection) throws SQLException {
		_roleMap = new TreeMap<Integer, Role>();

		Statement stmt = connection.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRole);


		while (rs.next()) {

			Permission permission = new Permission(
					rs.getInt(DatabaseConstants.RelRoleRead),
					rs.getInt(DatabaseConstants.RelRoleInsert),
					rs.getInt(DatabaseConstants.RelRoleUpdate),
					rs.getInt(DatabaseConstants.RelRoleDelete));
			
			int roleID = rs.getInt(DatabaseConstants.RelRoleID);
			
			Role role = new Role(
					rs.getInt(DatabaseConstants.RelRoleID),
					rs.getString(DatabaseConstants.RelRoleName),
					_wiki,
					permission,
					getCapabilities(connection, roleID),
					getUsers(connection, roleID),
					getMutexRoles(roleID),
					getSuperRoles(connection, roleID));

			_roleMap.put(role.getID(), role);
		}
		rs.close();
		stmt.close();
	}

	protected void initUserRoleMap(Connection connection) throws SQLException {
		_userRoleMap = new TreeMap<Integer, ArrayList<Integer>>();

		Statement stmt = connection.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleAssignment);

		while (rs.next()) {
			int userID = rs.getInt(DatabaseConstants.RelRoleAssignmentUserID);
			int roleID = rs.getInt(DatabaseConstants.RelRoleAssignmentRoleID);
			if (_userRoleMap.containsKey(userID)) {
				_userRoleMap.get(userID).add(roleID);
			} else {
				_userRoleMap.put(userID, new ArrayList<Integer>());
				_userRoleMap.get(userID).add(roleID);
			}
		}
	}
	
	protected void initMutexRolePairs(Connection connection) throws SQLException {
		_mutexRolePairs = new ArrayList<Pair<Integer, Integer>>();
		
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleMutex);
		while(rs.next()) {
			int role1ID = rs.getInt(DatabaseConstants.RelaMutexRole1ID);
			int role2ID = rs.getInt(DatabaseConstants.RelaMutexRole2ID);
			Pair<Integer, Integer> pair = new Pair<Integer, Integer>(role1ID, role2ID);
			
			if(role1ID != role2ID && !_mutexRolePairs.contains(pair)) {
				_mutexRolePairs.add(pair);
			}
		}
	}
	
	protected HashMap<Integer, Capability> getCapabilities(
			Connection connection, int roleID) throws SQLException {
		HashMap<Integer, Capability> roleCapabilities = new HashMap<Integer, Capability>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleCapability + " WHERE "
				+ DatabaseConstants.RelRoleCapabilityRoleID + " = " + roleID);

		while (rs.next()) {
			Capability capability = new Capability(
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityRead),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityInsert),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityDelete),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityUpdate));
			roleCapabilities.put(
					rs.getInt(DatabaseConstants.RelRoleCapabilityEntryID),
					capability);
		}

		return roleCapabilities;
	}
	
	protected ArrayList<Integer> getMutexRoles(int roleID) {
		if(_mutexRolePairs == null) {
			return null;
		}
		ArrayList<Integer> mutexRoles = new ArrayList<Integer>();
		
		for(Pair<Integer, Integer> pair : _mutexRolePairs) {
			int mutexRoleID = 0;
			if(pair.getLeft().equals(roleID)) {
				mutexRoleID = pair.getRight();
			} else if(pair.getRight().equals(roleID)) {
				mutexRoleID = pair.getLeft();
			} else {
				continue;
			}
			
			if(!pair.getRight().equals(pair.getLeft()) && !mutexRoles.contains(mutexRoleID)) {
				mutexRoles.add(mutexRoleID);
			}
		}
		
		return mutexRoles;
	}
	
	protected ArrayList<Integer> getSuperRoles(Connection connection, int roleID) throws SQLException {
		ArrayList<Integer> superRoles = new ArrayList<Integer>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleInheritance + " WHERE "
				+ DatabaseConstants.RelaSubRoleID + " = " + roleID);
		while(rs.next()) {
			int superRoleID = rs.getInt(DatabaseConstants.RelaSuperRoleID);
			if(!superRoles.contains(superRoleID)) {
				superRoles.add(superRoleID);
			}
		}
		
		return superRoles;
	}

	protected ArrayList<Integer> getUsers(Connection connection, int roleID)
			throws SQLException {
		ArrayList<Integer> users = new ArrayList<Integer>();

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleAssignment + " WHERE "
				+ DatabaseConstants.RelRoleAssignmentRoleID + " = " + roleID);
		while (rs.next()) {
			users.add(rs.getInt(DatabaseConstants.RelRoleAssignmentUserID));
		}

		return users;
	}

	public Set<Integer> getRoleIDListing() {
		return _roleMap.keySet();
	}
	
	public Set<Integer> getUserIDListing() {
		return _userRoleMap.keySet();
	}
	
	public ArrayList<Pair<Integer, Integer>> getMutexRolePairs() {
		return _mutexRolePairs;
	}

	public Role getRole(int role_id) {
		return _roleMap.get(role_id);
	}
	
	public ArrayList<Integer> getUserRoles(int userID) {
		return _userRoleMap.get(userID);
	}

	// whether a role exists according to role id
	public boolean isRoleExist(int role_id) {
		if (_roleMap.containsKey(role_id)) {
			return true;
		}
		return false;
	}

	// whether a role name has been used
	public boolean isRoleNameExist(String role_name) {
		for (Integer id : _roleMap.keySet()) {
			if (_roleMap.get(id).getName().equals(role_name)) {
				return true;
			}
		}

		return false;
	}

	public synchronized void insertRole(Connection connection, String name)
			throws SQLException {
		if (!isRoleNameExist(name)) {

			PreparedStatement pStmt = connection
					.prepareStatement("INSERT INTO " + _wiki.name()
							+ DatabaseConstants.RelationRole + "("
							+ DatabaseConstants.RelRoleName + " , "
							+ DatabaseConstants.RelRoleRead + " , "
							+ DatabaseConstants.RelRoleInsert + " , "
							+ DatabaseConstants.RelRoleUpdate + " , "
							+ DatabaseConstants.RelRoleDelete
							+ ") VALUES ( ?, -1, -1, -1, -1 )");

			pStmt.setString(1, name);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}

	public synchronized void deleteRole(Connection connection, int role_id)
			throws SQLException {
		if (isRoleExist(role_id)) {

			PreparedStatement pStmt1 = connection
					.prepareStatement("DELETE FROM " + _wiki.name()
							+ DatabaseConstants.RelationRole + " WHERE "
							+ DatabaseConstants.RelRoleID + " = ?");

			PreparedStatement pStmt2 = connection
					.prepareStatement("DELETE FROM " + _wiki.name()
							+ DatabaseConstants.RelationRoleCapability
							+ " WHERE "
							+ DatabaseConstants.RelRoleCapabilityRoleID
							+ " = ?");

			pStmt1.setInt(1, role_id);
			pStmt2.setInt(1, role_id);

			pStmt1.execute();
			pStmt2.execute();

			pStmt1.close();
			pStmt2.close();

			initialize(connection);
		}
	}

	public synchronized void updateRoleEntryCapability(Connection con,
			int role_id, int entry, Capability cap) throws SQLException {

		PreparedStatement pStmt = null;
		Role role = getRole(role_id);
		if (role.isCapabilityExist(entry)) {
			pStmt = con.prepareStatement("UPDATE " + _wiki.name()
					+ DatabaseConstants.RelationRoleCapability + " " + "SET "
					+ DatabaseConstants.RelRoleCapabilityRead + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityInsert + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityUpdate + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityDelete + " = ? "
					+ "WHERE " + DatabaseConstants.RelRoleCapabilityEntryID
					+ " = ? " + "AND "
					+ DatabaseConstants.RelRoleCapabilityRoleID + " = ?");

			pStmt.setBoolean(1, cap.isRead());
			pStmt.setBoolean(2, cap.isInsert());
			pStmt.setBoolean(3, cap.isDelete());
			pStmt.setBoolean(4, cap.isUpdate());
			pStmt.setInt(5, entry);
			pStmt.setInt(6, role_id);
			pStmt.execute();
			pStmt.close();
		} else {
			pStmt = con.prepareStatement("INSERT INTO " + _wiki.name()
					+ DatabaseConstants.RelationRoleCapability + "("
					+ DatabaseConstants.RelRoleCapabilityEntryID + ", "
					+ DatabaseConstants.RelRoleCapabilityRoleID + ", "
					+ DatabaseConstants.RelRoleCapabilityRead + ", "
					+ DatabaseConstants.RelRoleCapabilityInsert + ", "
					+ DatabaseConstants.RelRoleCapabilityUpdate + ", "
					+ DatabaseConstants.RelRoleCapabilityDelete
					+ ") VALUES(?, ?, ?, ?, ?, ?)");

			pStmt.setInt(1, entry);
			pStmt.setInt(2, role_id);
			pStmt.setBoolean(3, cap.isRead());
			pStmt.setBoolean(4, cap.isInsert());
			pStmt.setBoolean(5, cap.isDelete());
			pStmt.setBoolean(6, cap.isUpdate());
			pStmt.execute();
			pStmt.close();
		}
		con.commit();

		updateRoleEntryCapability(role_id, entry, cap);
	}

	protected synchronized void updateRoleEntryCapability(int role_id,
			int entry, Capability capability) {
		Role role = getRole(role_id);
		if (role.isCapabilityExist(entry)) {
			role.setCapability(entry, capability);
		} else {
			role.addCapability(entry, capability);
		}
	}

	public synchronized void updateRolePermission(Connection connection,
			int role_id, Permission permission) throws SQLException {
		if (isRoleExist(role_id)) {
			PreparedStatement pStmt = connection.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
					+ DatabaseConstants.RelRoleRead + " = ?, "
					+ DatabaseConstants.RelRoleInsert + " = ?, "
					+ DatabaseConstants.RelRoleUpdate + " = ?, "
					+ DatabaseConstants.RelRoleDelete + " = ? " + " WHERE "
					+ DatabaseConstants.RelRoleID + " = ?");

			pStmt.setInt(1, permission.getReadPermission());
			pStmt.setInt(2, permission.getInsertPermission());
			pStmt.setInt(3, permission.getUpdatePermission());
			pStmt.setInt(4, permission.getDeletePermission());
			pStmt.setInt(5, role_id);
			pStmt.execute();
			pStmt.close();
		}
	}

	public synchronized void updateRoleName(Connection connection, int role_id,
			String role_name) throws SQLException {
		if (isRoleExist(role_id)) {
			PreparedStatement pStmt = connection.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
					+ DatabaseConstants.RelRoleName + " = ? " + " WHERE "
					+ DatabaseConstants.RelRoleID + " = ? ");

			pStmt.setString(1, role_name);
			pStmt.setInt(2, role_id);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}

	protected boolean isAssignmentExist(int role_id, int user_id)
			throws SQLException {
		if (getRole(role_id).getUsers().contains(user_id)) {
			return true;
		}

		return false;
	}

	public synchronized void assignUser(Connection connection, int role_id,
			int user_id) throws SQLException {
		if(_userRoleMap.containsKey(user_id)) {
			for(int roleID : _userRoleMap.get(user_id)) {
				if(this.getRole(roleID).isMutex(role_id) || this.getRole(role_id).isMutex(roleID)) {
					return;
				}
			}
		}
		
		if (isRoleExist(role_id) && _wiki.users().contains(user_id)
				&& !isAssignmentExist(role_id, user_id)) {
			PreparedStatement pStmt = connection
					.prepareStatement("INSERT INTO " + _wiki.name()
							+ DatabaseConstants.RelationRoleAssignment + " ( "
							+ DatabaseConstants.RelRoleAssignmentRoleID + " , "
							+ DatabaseConstants.RelRoleAssignmentUserID + " ) "
							+ " VALUES " + "( ? , ? )");

			pStmt.setInt(1, role_id);
			pStmt.setInt(2, user_id);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}

	public synchronized void unassignUser(Connection connection, int role_id,
			int user_id) throws SQLException {
		if (isRoleExist(role_id)) {
			PreparedStatement pStmt = connection
					.prepareStatement("DELETE FROM " + _wiki.name()
							+ DatabaseConstants.RelationRoleAssignment
							+ " WHERE "
							+ DatabaseConstants.RelRoleAssignmentRoleID
							+ " = ? AND "
							+ DatabaseConstants.RelRoleAssignmentUserID
							+ " = ? ");

			pStmt.setInt(1, role_id);
			pStmt.setInt(2, user_id);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}

	protected Permission getUserPermission(int userID) {
		Permission permission = new Permission(Permission.negativePermission,
				Permission.negativePermission, Permission.negativePermission,
				Permission.negativePermission);

		for (int roleID : _userRoleMap.get(userID)) {
			Permission temp = getRole(roleID).getpermission();

			if (temp.getReadPermission() > permission.getReadPermission()) {
				permission.setReadPermission(temp.getReadPermission());
			}

			if (temp.getInsertPermission() > permission.getInsertPermission()) {
				permission.setInsertPermission(temp.getInsertPermission());
			}

			if (temp.getUpdatePermission() > permission.getUpdatePermission()) {
				permission.setUpdatePermission(temp.getUpdatePermission());
			}

			if (temp.getDeletePermission() > permission.getDeletePermission()) {
				permission.setDeletePermission(temp.getDeletePermission());
			}
		}

		return permission;

	}

	protected Capability getUserCapability(int userID, int entryID) {
		boolean read = false, insert = false, update = false, delete = false;

		for (int roleID : _userRoleMap.get(userID)) {
			if (getRole(roleID).isCapabilityExist(entryID)) {
				Capability temp = getRole(roleID).getCapability(entryID);

				if (temp.isRead()) {
					read = true;
				}

				if (temp.isInsert()) {
					insert = true;
				}

				if (temp.isUpdate()) {
					update = true;
				}

				if (temp.isDelete()) {
					delete = true;
				}
			}
		}

		return new Capability(read, insert, update, delete);
	}

	public synchronized void insertMutexRolePairs(Connection connection, int role1ID, int role2ID) throws SQLException {
		if (role1ID != role2ID && isRoleExist(role1ID) && isRoleExist(role2ID) && !this.getRole(role1ID).isMutex(role2ID)) {

			PreparedStatement pStmt = connection
					.prepareStatement("INSERT INTO " + _wiki.name()
							+ DatabaseConstants.RelationRoleMutex + "("
							+ DatabaseConstants.RelaMutexRole1ID + " , "
							+ DatabaseConstants.RelaMutexRole2ID
							+ ") VALUES ( ?, ? )");

			pStmt.setInt(1, role1ID);
			pStmt.setInt(2, role2ID);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}
	
	public synchronized void deleteMutexRolePairs(Connection connection, int role1ID, int role2ID) throws SQLException {
		if (isRoleExist(role1ID) && isRoleExist(role2ID) && this.getRole(role1ID).isMutex(role2ID)) {

			PreparedStatement pStmt = connection
					.prepareStatement("DELETE FROM " + _wiki.name()
							+ DatabaseConstants.RelationRoleMutex + " WHERE "
							+ DatabaseConstants.RelaMutexRole1ID + " = ? AND "
							+ DatabaseConstants.RelaMutexRole2ID + " = ?");

			pStmt.setInt(1, role1ID);
			pStmt.setInt(2, role2ID);
			pStmt.execute();
			pStmt.close();

			initialize(connection);
		}
	}
	
	public synchronized void addSuperRoles(Connection connection, int subRoleID, int superRoleID) throws SQLException {
		if(subRoleID == superRoleID 
				|| !isRoleExist(subRoleID) || !isRoleExist(superRoleID)
				|| this.getRole(subRoleID).hasSuperRole(superRoleID)
				|| this.getRole(superRoleID).hasSuperRole(subRoleID)) {
			return;
		}
		
		PreparedStatement pStmt = connection.prepareStatement("INSERT INTO " 
						+ _wiki.name() + DatabaseConstants.RelationRoleInheritance + "("
						+ DatabaseConstants.RelaSubRoleID + " , "
						+ DatabaseConstants.RelaSuperRoleID
						+ ") VALUES ( ?, ? )");

		pStmt.setInt(1, subRoleID);
		pStmt.setInt(2, superRoleID);
		pStmt.execute();
		pStmt.close();

		initialize(connection);
	}
	
	public synchronized void deleteSuperRoles(Connection connection, int subRoleID, int superRoleID) throws SQLException {
		if (!isRoleExist(subRoleID) || !isRoleExist(superRoleID) 
				|| this.getRole(subRoleID).hasSuperRole(superRoleID)) {
			return;
		}

		PreparedStatement pStmt = connection.prepareStatement("DELETE FROM " 
						+ _wiki.name()
						+ DatabaseConstants.RelationRoleInheritance + " WHERE "
						+ DatabaseConstants.RelaSubRoleID + " = ? AND "
						+ DatabaseConstants.RelaSuperRoleID + " = ?");

		pStmt.setInt(1, subRoleID);
		pStmt.setInt(2, superRoleID);
		pStmt.execute();
		pStmt.close();

		initialize(connection);	
	}
	
	private Integer isEntryLevelRequest(URI uri) {
		String path = uri.getPath();
		if (uri.getRawQuery() != null) {
			path = path + "?" + uri.getRawQuery();
		}
		String[] items = path.split("/");
		if (items.length >= 3) {
			if (items[2].contains("?")) {
				if (items[2].split("\\?")[0].length() == 0) {
					return null;
				} else {
					try {
						return new Integer(Integer.parseInt(
								items[2].split("\\?")[0], 16));
					} catch (NumberFormatException e) {
						return null;
					}
				}
			} else {
				try {
					// FIXME: DatabaseContent doesn't allow lookup by id
					DatabaseContent entries = _wiki.database().content();
					try {
						int entry = Integer.parseInt(items[2], 16);
						for (int i = 0; i < entries.size(); i++) {
							if (entries.get(i).id() == entry) {
								return new Integer(entry);
							}
						}
					} catch (NumberFormatException e) {
						return null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public boolean checkRequest(User user, Exchange<?> exchange) {
		if (user.is_admin()) {
			return true;
		} else if (_userRoleMap.containsKey(user.id())) {

			URI uri = exchange.getRequestURI();
			Integer entryID = isEntryLevelRequest(uri);
			Permission permission = getUserPermission(user.id());

			if (isProtectedRequest(exchange)) {
				if (isInsertRequest(exchange)) {
					if (entryID != null) {
						if (permission.getInsertPermission() == Permission.positivePermission) {
							return true;
						} else if (permission.getInsertPermission() == Permission.negativePermission) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.isInsert();
						}
					} else {
						if (permission.getInsertPermission() == Permission.positivePermission) {
							return true;
						} 
						return false;
					}
				} else if (isUpdateRequest(exchange)) {
					if (entryID != null) {
						if (permission.getUpdatePermission() == Permission.positivePermission) {
							return true;
						} else if (permission.getUpdatePermission() == Permission.negativePermission) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.isUpdate();
						}
					} else {
						return true;
					}
				} else if (isDeleteRequest(exchange)) {
					if (entryID != null) {
						if (permission.getDeletePermission() == Permission.positivePermission) {
							return true;
						} else if (permission.getDeletePermission() == Permission.negativePermission) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.isDelete();
						}
					} else {
						return true;
					}
				} else {
					return false;
				}
			} else {
				if (entryID != null) {
					if (permission.getReadPermission() == Permission.positivePermission) {
						return true;
					} else if (permission.getReadPermission() == Permission.negativePermission) {
						return false;
					} else {
						Capability capability = getUserCapability(
								user.id(), entryID);
						return capability.isRead();
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkRequest(User user) {
		if(user == null) {
			return false;
		} else if(user.is_admin()) {
			return true;
		} else if(_userRoleMap.containsKey(user.id())) {
			for(int roleID : _userRoleMap.get(user.id())) {
				if(roleID == Role.ownerID || roleID == Role.assistantID) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static HashMap<Integer, HashMap<String, ArrayList<String>>> getWikiServerRoleAssignment(Connection connection) throws SQLException {
		HashMap<Integer, HashMap<String, ArrayList<String>>> roleAssignment = new HashMap<Integer, HashMap<String, ArrayList<String>>>();
		Statement stmt1 = connection.createStatement();
		Statement stmt2 = connection.createStatement();
		Statement stmt3 = connection.createStatement();
		Statement stmt4 = connection.createStatement();
		
		ResultSet rs_user = stmt1.executeQuery("SELECT * FROM " + WikiServerConstants.RelationUser);
		while(rs_user.next()) {
			roleAssignment.put(rs_user.getInt(WikiServerConstants.RelUserColID), new HashMap<String, ArrayList<String>>());
		}
		
		ResultSet rs_db = stmt2.executeQuery("SELECT * FROM " + WikiServerConstants.RelationDatabase);
		while(rs_db.next()) {
			String dbName = rs_db.getString(WikiServerConstants.RelDatabaseColName);
			
			ResultSet rs_roleAssignment = stmt3.executeQuery("SELECT * FROM " + dbName + DatabaseConstants.RelationRoleAssignment);
			while(rs_roleAssignment.next()) {
				
				int userID = rs_roleAssignment.getInt(DatabaseConstants.RelRoleAssignmentUserID);
				int roleID = rs_roleAssignment.getInt(DatabaseConstants.RelRoleAssignmentRoleID);
				
				
				
				ResultSet rs_roleName = stmt4.executeQuery("SELECT * FROM " + dbName + DatabaseConstants.RelationRole 
						+ " WHERE " + DatabaseConstants.RelRoleID + " = " + roleID);
				String roleName = "";
				while(rs_roleName.next()) {
					roleName = rs_roleName.getString(DatabaseConstants.RelRoleName);
				}
				
				if(roleAssignment.containsKey(userID) && roleAssignment.get(userID).containsKey(dbName)) {
					roleAssignment.get(userID).get(dbName).add(roleName);
				}
				else if(!roleAssignment.containsKey(userID)) {
					HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>();
					temp.put(dbName, new ArrayList<String>());
					temp.get(dbName).add(roleName);
					roleAssignment.put(userID, temp);
				}
				else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(roleName);
					roleAssignment.get(userID).put(dbName, temp);
				}
			}
		}
		return roleAssignment;
		
		
	}
}
