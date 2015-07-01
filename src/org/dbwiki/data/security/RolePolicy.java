package org.dbwiki.data.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.dbwiki.driver.rdbms.DatabaseConstants;
import org.dbwiki.web.server.DatabaseWiki;

public class RolePolicy extends SimplePolicy {
	
	public static final int newRoleID = -1;
	
	private TreeMap<Integer, Role> _roleListing;
	private DatabaseWiki _wiki;

	public RolePolicy(int mode, DatabaseWiki wiki) {
		super(mode);
		_wiki = wiki;
	}

	public void initialize(Connection connection) throws SQLException {
		getRoleListing(connection);
    }
	
	public void getRoleListing(Connection connection) throws SQLException {
		_roleListing = new TreeMap<Integer, Role>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationRole);
		System.out.println("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationRole);
		while(rs.next()) {
			
			Permission permission = new Permission(
					rs.getInt(DatabaseConstants.RelRoleRead), 
					rs.getInt(DatabaseConstants.RelRoleInsert), 
					rs.getInt(DatabaseConstants.RelRoleUpdate), 
					rs.getInt(DatabaseConstants.RelRoleDelete));
			
			Role role = new Role(
					rs.getInt(DatabaseConstants.RelRoleID), 
					rs.getString(DatabaseConstants.RelRoleName), 
					_wiki, 
					permission,
					getCapabilities(connection, rs.getInt(DatabaseConstants.RelRoleID)),
					getUsers(connection, rs.getInt(DatabaseConstants.RelRoleID)),
					null);
			
			_roleListing.put(role.getID(), role);
		}
		rs.close();
		stmt.close();
	}
	
	protected HashMap<Integer, Capability> getCapabilities(Connection connection, int roleID) throws SQLException {
		HashMap<Integer, Capability> roleCapabilities = new HashMap<Integer, Capability>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationRoleCapability
				+ " WHERE " + DatabaseConstants.RelRoleCapabilityRoleID
				+ " = " + roleID);
		
		System.out.println("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationRoleCapability
				+ "WHERE " + DatabaseConstants.RelRoleID
				+ " = " + roleID);
		
		while(rs.next()) {
			Capability capability = new Capability(rs.getBoolean(DatabaseConstants.RelRoleCapabilityRead),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityInsert),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityDelete),
					rs.getBoolean(DatabaseConstants.RelRoleCapabilityUpdate));
			roleCapabilities.put(rs.getInt(DatabaseConstants.RelRoleCapabilityEntryID), capability);	
		}
		
		return roleCapabilities;
	}
	
	protected ArrayList<Integer> getUsers(Connection connection, int roleID) throws SQLException {
		ArrayList<Integer> users = new ArrayList<Integer>();
		
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+  _wiki.name() + DatabaseConstants.RelationRoleAssignment + " WHERE " 
				+ DatabaseConstants.RelRoleAssignmentRoleID + " = " 
				+ roleID);
		while(rs.next()) {
			users.add(rs.getInt(DatabaseConstants.RelRoleAssignmentUserID));
		}
		
		return users;
	}
	
	public Set<Integer> getRoleIDListing() {
		return _roleListing.keySet();
	}
	
	public Role getRole(int role_id) {
		return _roleListing.get(role_id);
	}
	
	// whether a role exists according to role id
	public boolean isRoleExist(int role_id) {
		if(_roleListing.containsKey(role_id)) {
			return true;
		}
		return false;
	}
	
	// whether a role name has been used
	public boolean isRoleNameExist(String role_name) {
		for(Integer id : _roleListing.keySet()) {
			if(_roleListing.get(id).getName().equals(role_name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public synchronized void addRole(Connection connection, String name) throws SQLException {
		if(!isRoleNameExist(name)) {
			
			PreparedStatement pStmt = connection.prepareStatement("INSERT INTO "
					+ _wiki.name() + DatabaseConstants.RelationRole + "("
					+ DatabaseConstants.RelRoleName + " , "
					+ DatabaseConstants.RelRoleRead + " , "
					+ DatabaseConstants.RelRoleInsert + " , "
					+ DatabaseConstants.RelRoleUpdate + " , "
					+ DatabaseConstants.RelRoleDelete 
					+") VALUES ( ?, -1, -1, -1, -1 )");
			
			System.out.println("INSERT INTO "
					+ _wiki.name() + DatabaseConstants.RelationRole + "("
					+ DatabaseConstants.RelRoleName  + " , "
					+ DatabaseConstants.RelRoleRead + " , "
					+ DatabaseConstants.RelRoleInsert + " , "
					+ DatabaseConstants.RelRoleUpdate + " , "
					+ DatabaseConstants.RelRoleDelete
					+") VALUES ( "+ name +", -1, -1, -1, -1 )");
			
			pStmt.setString(1, name);
			pStmt.execute();
			pStmt.close();
			
			getRoleListing(connection);
		}
	}

	public synchronized void deleteRole(Connection connection, int role_id) throws SQLException {
		if(isRoleExist(role_id)) {
			
			PreparedStatement pStmt1 = connection.prepareStatement("DELETE FROM "
					+ _wiki.name() + DatabaseConstants.RelationRole + " WHERE "
					+ DatabaseConstants.RelRoleID + " = ?");
			
			PreparedStatement pStmt2 = connection.prepareStatement("DELETE FROM "
					+ _wiki.name() + DatabaseConstants.RelationRoleCapability + " WHERE "
					+ DatabaseConstants.RelRoleCapabilityRoleID + " = ?");
			
			System.out.println("DELETE FROM "
					+ _wiki.name() + DatabaseConstants.RelationRole + "WHERE"
					+ DatabaseConstants.RelRoleID + " = " + role_id);
			
			System.out.println("DELETE FROM "
					+ _wiki.name() + DatabaseConstants.RelationRoleCapability + " WHERE "
					+ DatabaseConstants.RelRoleCapabilityRoleID + " = " + role_id);
			
			pStmt1.setInt(1, role_id);
			pStmt2.setInt(1, role_id);
			
			pStmt1.execute();
			pStmt2.execute();
			
			pStmt1.close();
			pStmt2.close();
			
			getRoleListing(connection);
		}
	}
	
	public synchronized void updateRoleEntryCapability(Connection con, int role_id, int entry, Capability cap) 
    		throws SQLException {
    	
    	PreparedStatement pStmt = null;
		Role role = getRole(role_id);
		if (role.isCapabilityExist(entry)) {
			pStmt = con.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRoleCapability + " " + "SET "
					+ DatabaseConstants.RelRoleCapabilityRead + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityInsert + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityUpdate + " = ?, "
					+ DatabaseConstants.RelRoleCapabilityDelete + " = ? "
					+ "WHERE " + DatabaseConstants.RelRoleCapabilityEntryID + " = ? "
					+ "AND " + DatabaseConstants.RelRoleCapabilityRoleID + " = ?");

			pStmt.setBoolean(1, cap.isRead());
			pStmt.setBoolean(2, cap.isInsert());
			pStmt.setBoolean(3, cap.isDelete());
			pStmt.setBoolean(4, cap.isUpdate());
			pStmt.setInt(5, entry);
			pStmt.setInt(6, role_id);
			pStmt.execute();
			pStmt.close();
		} else {
			pStmt = con.prepareStatement("INSERT INTO "
					+ _wiki.name() + DatabaseConstants.RelationRoleCapability + "("
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
    
    
    protected synchronized void updateRoleEntryCapability(int role_id, int entry, Capability capability) {
    	Role role = getRole(role_id);
    	if(role.isCapabilityExist(entry)) {
    		role.setCapability(entry, capability);
    	}
    	else {
    		role.addCapability(entry, capability);
    	}
    }
    
    public synchronized void updateRolePermission(Connection connection, int role_id, Permission permission) throws SQLException {
    	if(isRoleExist(role_id)) {
	    	PreparedStatement pStmt = connection.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
	    			+ DatabaseConstants.RelRoleRead + " = ?, " 
	    			+ DatabaseConstants.RelRoleInsert + " = ?, " 
	    			+ DatabaseConstants.RelRoleUpdate + " = ?, " 
	    			+ DatabaseConstants.RelRoleDelete + " = ? " 
					+" WHERE "
					+ DatabaseConstants.RelRoleID + " = ?");
			
			System.out.println("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
	    			+ DatabaseConstants.RelRoleRead + " = " + permission.getReadPermission() + " , " 
	    			+ DatabaseConstants.RelRoleInsert + " = " + permission.getInsertPermission() + " , " 
	    			+ DatabaseConstants.RelRoleUpdate + " = " + permission.getUpdatePermission() + " , " 
	    			+ DatabaseConstants.RelRoleDelete + " = " + permission.getDeletePermission() + " " 
					+" WHERE "
					+ DatabaseConstants.RelRoleID + " = " + role_id);
			
			pStmt.setInt(1, permission.getReadPermission());
			pStmt.setInt(2, permission.getInsertPermission());
			pStmt.setInt(3, permission.getUpdatePermission());
			pStmt.setInt(4, permission.getDeletePermission());
			pStmt.setInt(5, role_id);
			pStmt.execute();
			pStmt.close();
    	}
    }
    
    public synchronized void updateRoleName(Connection connection, int role_id, String role_name) throws SQLException {
    	if(isRoleExist(role_id)) {
    		PreparedStatement pStmt = connection.prepareStatement("UPDATE "
    				+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
    				+ DatabaseConstants.RelRoleName + " = ? "
    				+ " WHERE "
    				+ DatabaseConstants.RelRoleID + " = ? ");
    		
    		System.out.println("UPDATE "
    				+ _wiki.name() + DatabaseConstants.RelationRole + " SET "
    				+ DatabaseConstants.RelRoleName + " = " + role_name
    				+ " WHERE "
    				+ DatabaseConstants.RelRoleID + " = " + role_id);
    		
    		pStmt.setString(1, role_name);
    		pStmt.setInt(2, role_id);
    		pStmt.execute();
    		pStmt.close();
    		
    		getRoleListing(connection);
    	}
    }
    
    protected boolean isAssignmentExist(int role_id, int user_id) throws SQLException {
    	if(getRole(role_id).getUsers().contains(user_id)) {
    		return true;
    	}
    	
    	return false;
    }
    
    public synchronized void assignUser(Connection connection, int role_id, int user_id) throws SQLException {
    	if(isRoleExist(role_id) && _wiki.users().contains(user_id) && !isAssignmentExist(role_id, user_id)) {
    		PreparedStatement pStmt = connection.prepareStatement("INSERT INTO "
    				+ _wiki.name() + DatabaseConstants.RelationRoleAssignment + " ( "
    				+ DatabaseConstants.RelRoleAssignmentRoleID + " , "
    				+ DatabaseConstants.RelRoleAssignmentUserID + " ) "
    				+ " VALUES " + "( ? , ? )");
    		
    		System.out.println("INSERT INTO "
    				+ _wiki.name() + DatabaseConstants.RelationRoleAssignment + " ( "
    				+ DatabaseConstants.RelRoleAssignmentRoleID + " , "
    				+ DatabaseConstants.RelRoleAssignmentUserID + " ) "
    				+ " VALUES " + "( " + role_id + " , " + user_id + ")");
    		
    		pStmt.setInt(1, role_id);
    		pStmt.setInt(2, user_id);
    		pStmt.execute();
    		pStmt.close();
    		
    		getRoleListing(connection);
    	}
    }
    
    public synchronized void unassignUser(Connection connection, int role_id, int user_id) throws SQLException {
    	if(isRoleExist(role_id)) {
    		PreparedStatement pStmt = connection.prepareStatement("DELETE FROM "
    				+ _wiki.name() + DatabaseConstants.RelationRoleAssignment + " WHERE "
    				+ DatabaseConstants.RelRoleAssignmentRoleID + " = ? AND "
    				+ DatabaseConstants.RelRoleAssignmentUserID + " = ? ");
    		
    		System.out.println("DELETE FROM "
    				+ _wiki.name() + DatabaseConstants.RelationRoleAssignment + " WHERE "
    				+ DatabaseConstants.RelRoleAssignmentRoleID + " = ? AND "
    				+ DatabaseConstants.RelRoleAssignmentUserID + " = ? ");
    		
    		pStmt.setInt(1, role_id);
    		pStmt.setInt(2, user_id);
    		pStmt.execute();
    		pStmt.close();
    		
    		getRoleListing(connection);
    	}
    }
}
