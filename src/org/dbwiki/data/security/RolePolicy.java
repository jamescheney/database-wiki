package org.dbwiki.data.security;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.exception.WikiException;
import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServerConstants;

public class RolePolicy extends SimplePolicy {
	public static final int MaximumOwnerNum = 1;
	SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss:SSS"); // for performance evaluation
	
	// Mapping between role id and role object
	private TreeMap<Integer, Role> _roleMap;  
	
	// Mapping between user id and user's role listing
	private TreeMap<Integer, ArrayList<Integer>> _userRoleMap; 
	
	// A list of mutex role object pairs
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
					getRoleCapabilities(connection, roleID),
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
	
	protected HashMap<Integer, Capability> getRoleCapabilities(Connection connection, int roleID) throws SQLException {
		HashMap<Integer, Capability> roleCapabilities = new HashMap<Integer, Capability>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _wiki.name()
				+ DatabaseConstants.RelationRoleCapability + " WHERE "
				+ DatabaseConstants.RelRoleCapabilityRoleID + " = " + roleID);

		while (rs.next()) {
			Capability capability = new Capability(
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityRead),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityInsert),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityUpdate),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityDelete));
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
		if(_roleMap == null) {
			return null;
		}
		return _roleMap.keySet();
	}
	
	public Set<Integer> getUserIDListing() {
		if(_userRoleMap == null) {
			return null;
		}
		return _userRoleMap.keySet();
	}
	
	public ArrayList<Pair<Integer, Integer>> getMutexRolePairs() {
		if(_mutexRolePairs == null) {
			return null;
		}
		return _mutexRolePairs;
	}

	public Role getRole(int role_id) {
		if(_roleMap == null || _roleMap.isEmpty()) {
			return null;
		}
		return _roleMap.get(role_id);
	}
	
	public ArrayList<Integer> getUserRoles(int userID) {
		if(_userRoleMap == null || _userRoleMap.isEmpty()) {
			return null;
		}
		return _userRoleMap.get(userID);
	}


	public Permission getUserPermission(int userID) {
		if(_userRoleMap == null || _userRoleMap.isEmpty()) {
			return null;
		}
		
		Permission permission = new Permission();
		for (int roleID : _userRoleMap.get(userID)) {
			Permission temp = this.getRoleEventualPermission(roleID);

			if (temp.getRead() > permission.getRead()) {
				permission.setRead(temp.getRead());
			}

			if (temp.getInsert() > permission.getInsert()) {
				permission.setInsert(temp.getInsert());
			}

			if (temp.getUpdate() > permission.getUpdate()) {
				permission.setUpdate(temp.getUpdate());
			}

			if (temp.getDelete() > permission.getDelete()) {
				permission.setDelete(temp.getDelete());
			}
		}

		return permission;

	}

	public Capability getUserCapability(int userID, int entryID) {
		
		if(_userRoleMap == null || _userRoleMap.isEmpty()) {
			return null;
		}
		
		Capability cap = new Capability();
		for (int roleID : _userRoleMap.get(userID)) {
			
			if(roleID == Role.assistantID || roleID == Role.ownerID) {
				return new Capability(true, true, true, true);
			} else if (this.getRole(roleID).isCapabilityExist(entryID)) {
				Capability temp = this.getRoleEventualCapability(roleID, entryID);

				if (!cap.getRead() && temp.getRead()) {
					cap.setRead(true);
				}

				if (!cap.getInsert() && temp.getInsert()) {
					cap.setInsert(true);
				}

				if (!cap.getUpdate() && temp.getUpdate()) {
					cap.setUpdate(true);
				}

				if (!cap.getDelete() && temp.getDelete()) {
					cap.setDelete(true);
				}
			}
		}

		return cap;
	}
	
	public Capability getUserAuthority(int userID, int entryID) {
		Permission per = this.getUserPermission(userID);
		Capability cap = this.getUserCapability(userID, entryID);
		Capability newCap = cap;
		
		if(per.isPositiveRead()) {
			newCap.setRead(true);
		} else if(per.isNegativeRead()) {
			newCap.setRead(false);
		}
		
		if(per.isPositiveInsert()) {
			newCap.setInsert(true);
		} else if(per.isNegativeInsert()) {
			newCap.setInsert(false);
		}
		
		if(per.isPositiveUpdate()) {
			newCap.setUpdate(true);
		} else if(per.isNegativeUpdate()) {
			newCap.setUpdate(false);
		}
		
		if(per.isPositiveDelete()) {
			newCap.setDelete(true);
		} else if(per.isNegativeDelete()) {
			newCap.setDelete(false);
		}
		
		return newCap;
	}
	
	public Permission getSuperRolePermission(int roleID) {
		Permission newP = new Permission();
		if(this.getRole(roleID).hasSuperRole()) {
			for(int tempID : this.getRole(roleID).getSuperRoles()) {
				
				if (newP.isFullAccess()) {
					return newP;
				}
				
				Permission tempP = this.getRole(tempID).getpermission();
				
				if(tempP.getRead() > newP.getRead()) {
					newP.setRead(tempP.getRead());
				}
				
				if(tempP.getInsert() > newP.getInsert()) {
					newP.setInsert(tempP.getInsert());
				}
				
				if(tempP.getUpdate() > newP.getUpdate()) {
					newP.setUpdate(tempP.getUpdate());
				}
				
				if(tempP.getDelete() > newP.getDelete()) {
					newP.setDelete(tempP.getDelete());
				}
				
				if(this.getRole(tempID).hasSuperRole()) {
					tempP = getSuperRolePermission(tempID);
					
					if(tempP.getRead() > newP.getRead()) {
						newP.setRead(tempP.getRead());
					}
					
					if(tempP.getInsert() > newP.getInsert()) {
						newP.setInsert(tempP.getInsert());
					}
					
					if(tempP.getUpdate() > newP.getUpdate()) {
						newP.setUpdate(tempP.getUpdate());
					}
					
					if(tempP.getDelete() > newP.getDelete()) {
						newP.setDelete(tempP.getDelete());
					}
				}
			}
		}
		return newP;
	}
	
	public Capability getSuperRoleCapability(int roleID, int entryID) {
		Capability newC = new Capability();
		if(this.getRole(roleID).hasSuperRole()) {
			for(int tempID : this.getRole(roleID).getSuperRoles()) {
				if(newC.isFullAccess()) {
					return newC;
				}
				Capability tempC = new Capability();;
				if(this.getRole(tempID).isCapabilityExist(entryID)) {
					tempC = this.getRole(tempID).getCapability(entryID);
					
					if(tempC.getRead()) {
						newC.setRead(tempC.getRead());
					}
					
					if(tempC.getInsert()) {
						newC.setInsert(tempC.getInsert());
					}
					
					if(tempC.getUpdate()) {
						newC.setUpdate(tempC.getUpdate());
					}
					
					if(tempC.getDelete()) {
						newC.setDelete(tempC.getDelete());
					}
				}
				if(this.getRole(tempID).hasSuperRole()) {
					tempC = getSuperRoleCapability(tempID, entryID);
					
					if(tempC.getRead()) {
						newC.setRead(tempC.getRead());
					}
					
					if(tempC.getInsert()) {
						newC.setInsert(tempC.getInsert());
					}
					
					if(tempC.getUpdate()) {
						newC.setUpdate(tempC.getUpdate());
					}
					
					if(tempC.getDelete()) {
						newC.setDelete(tempC.getDelete());
					}
				}
			}
		}
		return newC;
	}
	
	public Permission getRoleEventualPermission(int roleID) {
		Permission ownP = this.getRole(roleID).getpermission();
		Permission superRoleP = this.getSuperRolePermission(roleID);
		
		Permission eventualP = new Permission(ownP.getRead(), ownP.getInsert(), ownP.getUpdate(), ownP.getDelete());
		
		if(ownP.getRead() < superRoleP.getRead()) {
			eventualP.setRead(superRoleP.getRead());
		}
		
		if(ownP.getInsert() < superRoleP.getInsert() ) {
			eventualP.setInsert(superRoleP.getInsert());
		}
		
		if(ownP.getUpdate() < superRoleP.getUpdate() ) {
			eventualP.setUpdate(superRoleP.getUpdate());
		}
		
		if(ownP.getDelete() < superRoleP.getDelete() ) {
			eventualP.setDelete(superRoleP.getDelete());
		}
		
		return eventualP;
	}
	
	public Capability getRoleEventualCapability(int roleID, int entryID) {
		if(roleID == Role.assistantID || roleID == Role.ownerID) {
			return new Capability(true, true, true, true);
		}
		
		Capability ownC = this.getRole(roleID).getCapability(entryID);
		
		Capability superRoleC = this.getSuperRoleCapability(roleID, entryID);
		
		Capability eventualC = new Capability(
				ownC.getRead() || superRoleC.getRead(),
				ownC.getInsert() || superRoleC.getInsert(),
				ownC.getUpdate() || superRoleC.getUpdate(),
				ownC.getDelete() || superRoleC.getDelete());
		return eventualC;
	}
	
	public Capability getRoleAuthority(int roleID, int entryID) {
		Permission per = this.getRoleEventualPermission(roleID);
		Capability cap = this.getRoleEventualCapability(roleID, entryID);
		Capability newCap = cap;
		
		if(per.isPositiveRead()) {
			newCap.setRead(true);
		} else if(per.isNegativeRead()) {
			newCap.setRead(false);
		}
		
		if(per.isPositiveInsert()) {
			newCap.setInsert(true);
		} else if(per.isNegativeInsert()) {
			newCap.setInsert(false);
		}
		
		if(per.isPositiveUpdate()) {
			newCap.setUpdate(true);
		} else if(per.isNegativeUpdate()) {
			newCap.setUpdate(false);
		}
		
		if(per.isPositiveDelete()) {
			newCap.setDelete(true);
		} else if(per.isNegativeDelete()) {
			newCap.setDelete(false);
		}
		
		return newCap;
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
	
	public boolean isEnoughOwner() {
		if(this.getRole(Role.ownerID).getUsers().size() > MaximumOwnerNum - 1) {
			return true;
		}
		return false;
	}
	
	public boolean isAssignmentExist(int role_id, int user_id)
			throws SQLException {
		if (getRole(role_id).getUsers().contains(user_id)) {
			return true;
		}

		return false;
	}
	
	public boolean canBeAssigned(int userID, int roleID) {
		if(this._userRoleMap.containsKey(userID) && !this._userRoleMap.get(userID).isEmpty()) {
			for(int tempRoleID : this._userRoleMap.get(userID)) {
				if(this.getRole(tempRoleID).isMutex(roleID) || this.getRole(roleID).isMutex(tempRoleID)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isMutexExist(int role1ID ,int role2ID) {
		if(isRoleExist(role1ID) && isRoleExist(role2ID) && this.getRole(role1ID).isMutex(role2ID)) {
			return true;
		}
		/* 
		Role tempRole1 = this.getRole(role1ID);
		Role tempRole2 = this.getRole(role2ID);
		if(tempRole1.hasSuperRole() && tempRole2.hasSuperRole()) {
			for(int tempSuper1 : tempRole1.getSuperRoles()) {
				for(int tempSuper2 : tempRole2.getSuperRoles()) {
					if(this.isMutexExist(tempSuper1, tempSuper2)) {
						return true;
					}
				}
			}
		} else if(tempRole1.hasSuperRole()) {
			for(int tempSuper1 : tempRole1.getSuperRoles()) {
				if(this.isMutexExist(tempSuper1, role2ID)) {
					return true;
				}
			}
		} else if(tempRole2.hasSuperRole()) {
			for(int tempSuper2 : tempRole2.getSuperRoles()) {
				if(this.isMutexExist(role1ID, tempSuper2)) {
					return true;
				}
			}
		}*/
		return false;
	}
	
	// check whether sub is super's super, inheritance can only be one-way
	public boolean hasCircle(int subRoleID, int superRoleID) {
		for(int temp : this.getRole(superRoleID).getSuperRoles()) {
			if(temp == subRoleID) {
				return true;
			}
			
			if(this.getRole(temp).hasSuperRole()) {
				return hasCircle(subRoleID, temp);
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
		}
	}

	public synchronized void updateRoleEntryCapability(Connection con,
			int roleID, int entryID, Capability cap) throws SQLException {

		PreparedStatement pStmt = null;
		Role role = getRole(roleID);
		if (role.isCapabilityExist(entryID)) {
			pStmt = con.prepareStatement("UPDATE " + _wiki.name()
					+ DatabaseConstants.RelationRoleCapability + " " + "SET "
					+ DatabaseConstants.RelRoleCapabilityRead + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityInsert + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityUpdate + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityDelete + " = ? "
					+ "WHERE " + DatabaseConstants.RelRoleCapabilityEntryID
					+ " = ? " + "AND "
					+ DatabaseConstants.RelRoleCapabilityRoleID + " = ?");

			pStmt.setBoolean(1, cap.getRead());
			pStmt.setBoolean(2, cap.getInsert());
			pStmt.setBoolean(3, cap.getUpdate());
			pStmt.setBoolean(4, cap.getDelete());
			pStmt.setInt(5, entryID);
			pStmt.setInt(6, roleID);
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

			pStmt.setInt(1, entryID);
			pStmt.setInt(2, roleID);
			pStmt.setBoolean(3, cap.getRead());
			pStmt.setBoolean(4, cap.getInsert());
			pStmt.setBoolean(6, cap.getUpdate());
			pStmt.setBoolean(5, cap.getDelete());
			pStmt.execute();
			pStmt.close();
		}
		con.commit();

		updateRoleEntryCapability(roleID, entryID, cap);
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
			int roleID, Permission permission) throws SQLException, WikiException {
		if (isRoleExist(roleID)) {
			PreparedStatement pStmt = connection.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
					+ DatabaseConstants.RelRoleRead + " = ?, "
					+ DatabaseConstants.RelRoleInsert + " = ?, "
					+ DatabaseConstants.RelRoleUpdate + " = ?, "
					+ DatabaseConstants.RelRoleDelete + " = ? " + " WHERE "
					+ DatabaseConstants.RelRoleID + " = ?");

			pStmt.setInt(1, permission.getRead());
			pStmt.setInt(2, permission.getInsert());
			pStmt.setInt(3, permission.getUpdate());
			pStmt.setInt(4, permission.getDelete());
			pStmt.setInt(5, roleID);
			pStmt.execute();
			pStmt.close();
			/*
			if(!permission.isNeutralRead() || !permission.isNeutralInsert() || !permission.isNeutralUpdate() || !permission.isNeutralDelete()) {
				DatabaseContent entries = _wiki.database().content();
				for(int i = 0;i < entries.size(); i++) {
					int entryID = entries.getByIndex(i).id();
					Capability cap = this.getRole(roleID).getCapability(entryID);
					
					if(permission.isPositiveRead()) {
						cap.setRead(true);
					} else if(permission.isNegativeRead()) {
						cap.setRead(false);
					}
					
					if(permission.isPositiveInsert()) {
						cap.setInsert(true);
					} else if(permission.isNegativeInsert()) {
						cap.setInsert(false);
					}
					
					if(permission.isPositiveUpdate()) {
						cap.setUpdate(true);
					} else if(permission.isNegativeUpdate()) {
						cap.setUpdate(false);
					}
					
					if(permission.isPositiveDelete()) {
						cap.setDelete(true);
					} else if(permission.isNegativeDelete()) {
						cap.setDelete(false);
					}
					
					this.updateRoleEntryCapability(connection, roleID, entryID, cap);
				}
			}*/
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
		}
	}

	public synchronized void assignUser(Connection connection, int roleID,
			int userID) throws SQLException {
		// whether this role is mutex to the user's other roles
		if(!canBeAssigned(userID, roleID)) {
			return;
		}
		
		// the maximum number of owner
		if(roleID == Role.ownerID && this.isEnoughOwner()) {
			return;
		}
		
		if (isRoleExist(roleID) && _wiki.users().contains(userID)
				&& !isAssignmentExist(roleID, userID)) {
			PreparedStatement pStmt = connection
					.prepareStatement("INSERT INTO " + _wiki.name()
							+ DatabaseConstants.RelationRoleAssignment + " ( "
							+ DatabaseConstants.RelRoleAssignmentRoleID + " , "
							+ DatabaseConstants.RelRoleAssignmentUserID + " ) "
							+ " VALUES " + "( ? , ? )");

			pStmt.setInt(1, roleID);
			pStmt.setInt(2, userID);
			pStmt.execute();
			pStmt.close();
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
		}
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
		}
	}
	
	public synchronized void addSuperRoles(Connection connection, int subRoleID, int superRoleID) throws SQLException {
		if(subRoleID == superRoleID 
				|| !isRoleExist(subRoleID) || !isRoleExist(superRoleID)
				|| this.getRole(subRoleID).hasSuperRole(superRoleID)) {
			return;
		}
		
		if(this.hasCircle(subRoleID, superRoleID)) {
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
	}
	
	public synchronized void deleteSuperRoles(Connection connection, int subRoleID, int superRoleID) throws SQLException {
		if (!isRoleExist(subRoleID) || !isRoleExist(superRoleID) 
				|| !this.getRole(subRoleID).hasSuperRole(superRoleID)) {
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
							if (entries.getByIndex(i).id() == entry) {
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
		long startTime = System.nanoTime();
		try{
			
			
		if (user.is_admin()) {
			return true;
		} else if (_userRoleMap.containsKey(user.id())) {
	
			URI uri = exchange.getRequestURI();
			Integer entryID = isEntryLevelRequest(uri);
			Permission permission = getUserPermission(user.id());
			if (isProtectedRequest(exchange)) {
				if (isInsertRequest(exchange)) {
					if (entryID != null) {
						if (permission.getInsert() == Permission.positive) {
							return true;
						} else if (permission.getInsert() == Permission.negative) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.getInsert();
						}
					} else {
						if (permission.getInsert() == Permission.positive) {
							return true;
						} 
						return false;
					}
				} else if (isUpdateRequest(exchange)) {
					if (entryID != null) {
						if (permission.getUpdate() == Permission.positive) {
							return true;
						} else if (permission.getUpdate() == Permission.negative) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.getUpdate();
						}
					} else {
						return true;
					}
				} else if (isDeleteRequest(exchange)) {
					if (entryID != null) {
						if (permission.getDelete() == Permission.positive) {
							return true;
						} else if (permission.getDelete() == Permission.negative) {
							return false;
						} else {
							Capability capability = getUserCapability(
									user.id(), entryID);
							return capability.getDelete();
							}
					} else {
						return true;
					}
				} else {
					return false;
				}
			} else {
				if (entryID != null) {
					if (permission.getRead() == Permission.positive) {
						return true;
					} else if (permission.getRead() == Permission.negative) {
						return false;
					} else {
						Capability capability = getUserCapability(
								user.id(), entryID);
						return capability.getRead();
					}
				} else {
					return true;
				}
			}
		}
		return false;
		}
		finally {
    		System.out.println("Authority Checking takes ================" + (System.nanoTime() - startTime)/1000);
    	}
	}
	
	public boolean checkRequest(User user) {
		long startTime = System.nanoTime();
		try{
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
		finally {
			System.out.println("Authority Checking takes ================" + (System.nanoTime() - startTime)/1000);
		}
		
	}
	
	public static HashMap<Integer, HashMap<String, ArrayList<String>>> getWikiServerRoleAssignment1(Connection connection) throws SQLException {
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
	
	public static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getWikiServerRoleAssignment2(Connection connection) throws SQLException {
		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> roleAssignment = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
		Statement stmt1 = connection.createStatement();
		Statement stmt2 = connection.createStatement();
		Statement stmt3 = connection.createStatement();
		
		ResultSet rs_user = stmt1.executeQuery("SELECT * FROM " + WikiServerConstants.RelationUser);
		while(rs_user.next()) {
			roleAssignment.put(rs_user.getInt(WikiServerConstants.RelUserColID), new HashMap<Integer, ArrayList<Integer>>());
		}
		
		ResultSet rs_db = stmt2.executeQuery("SELECT * FROM " + WikiServerConstants.RelationDatabase);
		while(rs_db.next()) {
			int dbID = rs_db.getInt(WikiServerConstants.RelDatabaseColID);
			String dbName = rs_db.getString(WikiServerConstants.RelDatabaseColName);
			
			ResultSet rs_roleAssignment = stmt3.executeQuery("SELECT * FROM " + dbName + DatabaseConstants.RelationRoleAssignment);
			while(rs_roleAssignment.next()) {
				
				int userID = rs_roleAssignment.getInt(DatabaseConstants.RelRoleAssignmentUserID);
				int roleID = rs_roleAssignment.getInt(DatabaseConstants.RelRoleAssignmentRoleID);
				
				
				if(roleAssignment.containsKey(userID) && roleAssignment.get(userID).containsKey(dbID)) {
					roleAssignment.get(userID).get(dbID).add(roleID);
				}
				else if(!roleAssignment.containsKey(userID)) {
					HashMap<Integer, ArrayList<Integer>> temp = new HashMap<Integer, ArrayList<Integer>>();
					temp.put(dbID, new ArrayList<Integer>());
					temp.get(dbID).add(roleID);
					roleAssignment.put(userID, temp);
				}
				else {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(roleID);
					roleAssignment.get(userID).put(dbID, temp);
				}
			}
		}
		return roleAssignment;
	}
}
