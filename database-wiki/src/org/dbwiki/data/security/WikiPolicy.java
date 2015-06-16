package org.dbwiki.data.security;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.driver.rdbms.DatabaseConstants;

import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.server.DatabaseWiki;

public class WikiPolicy extends SimplePolicy {

	// map from user ids to capabilities
    private Map<Integer,Capability> _authorizationListing;
    // map from user ids and entries to capabilites
    private Map<Integer,Map<Integer,Capability>> _policyListing;
    private DatabaseWiki _wiki;
    
    public WikiPolicy(int mode, DatabaseWiki wiki) {
    	super(mode);
    	_wiki = wiki;
    }	
    
    
	/**
	 * Initialize authorization listing from database
	 * 
	 */
    
    public void initialize(Connection connection) throws SQLException {
    	getAuthorizationListing(connection);
    	getDBPolicyListing(connection);
    
    }
    
	public void getAuthorizationListing(Connection connection)
			throws SQLException {
		_authorizationListing = new HashMap<Integer,Capability>();
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM "
				+ RelationAuthorization 
				+ " WHERE " + RelAuthenticationColDatabaseName + " = ? ");
		stmt.setString(1, _wiki.name());
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			_authorizationListing.put(rs.getInt(RelAuthenticationColUserID), 
					new Capability(
					rs.getBoolean(RelAuthenticationColRead), 
					rs.getBoolean(RelAuthenticationColInsert), 
					rs.getBoolean(RelAuthenticationColDelete), 
					rs.getBoolean(RelAuthenticationColUpdate)));
		}
		
		rs.close();
		stmt.close();

	}

	
	/**
	 * Get entry permissions of all users for database DB from DB_policy table
	 * @param user_id the id of a user
	 * FIXME: Combine this with getAuthorizationListing
	 */
	public void getDBPolicyListing(Connection con) throws SQLException {

		_policyListing = new HashMap<Integer,Map<Integer,Capability>>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationPolicy);
		System.out.println("SELECT * FROM "
				+ _wiki.name() + DatabaseConstants.RelationPolicy);
		while (rs.next()) {
			Capability cap = new Capability(rs.getBoolean(DatabaseConstants.RelPolicyRead), 
					rs.getBoolean(DatabaseConstants.RelPolicyInsert), 
					rs.getBoolean(DatabaseConstants.RelPolicyDelete), 
					rs.getBoolean(DatabaseConstants.RelPolicyUpdate));
			updateEntryCapability(rs.getInt(DatabaseConstants.RelPolicyUserID),
								  rs.getInt(DatabaseConstants.RelPolicyEntry),
								  cap);
		}
		rs.close();
		stmt.close();
	}
	
    

   
    
   
   
    private Integer isEntryLevelRequest(URI uri) {
    	String path = uri.getPath();
    	if (uri.getRawQuery() != null) {
    		path = path + "?" + uri.getRawQuery();
    	}
    	String[] items = path.split("/");
    	if(items.length >= 3) {
    		if(items[2].contains("?")) {
    			if(items[2].split("\\?")[0].length() == 0) {
    				return null;
    			} else {
    				try {
    					return new Integer(Integer.parseInt(items[2].split("\\?")[0],16));
    				} catch (NumberFormatException e) {
    					return null;
    				}
    			}
    		} else {
    			try {
    				// FIXME: DatabaseContent doesn't allow lookup by id
    				DatabaseContent entries = _wiki.database().content();
    				try {
    					int entry = Integer.parseInt(items[2],16);
    					for(int i = 0; i < entries.size(); i++) {
    						if(entries.get(i).id() == entry) {
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
   
    /** Check a request by user uname to do request specified by exchange */
    public boolean checkRequest(User user,  Exchange<?> exchange) {
    	boolean isProtectedRequest = isProtectedRequest(exchange);
    	if(user.is_admin()) { 
    		return true; 
    	} else if (find(user.id())) { 
    		//get the access permissions in the database
			Capability cap =  findCapability(user.id());
			boolean isRead = cap.isRead();
			boolean isInsert = cap.isInsert();
			boolean isDelete = cap.isDelete();
			boolean isUpdate = cap.isUpdate();

			URI uri = exchange.getRequestURI();
			Integer entryId = isEntryLevelRequest(uri);
			
			// check what kind of request it is
			if (isProtectedRequest) {
				//insert request
				if (isInsertRequest(exchange) && isInsert == true){
					// if this is an entry-level request
					if(entryId != null) {
						// and we have an entry-level policy
						if(findEntry(user.id(),entryId)) {
							return findEntryCapability(user.id(),entryId).isInsert();
						}
					} else { // otherwise use database-level capability
					return true; 
					}
					// if delete request
				} else if (isDeleteRequest(exchange) && isDelete == true){
					if(entryId != null) {
						if(findEntry(user.id(),entryId)) {
							return findEntryCapability(user.id(),entryId).isDelete();
						}
					}
					return true; 
					// update request
				} else if (isUpdateRequest(exchange) && isUpdate == true){
					if(entryId != null) {
						if(findEntry(user.id(),entryId)) {
							return findEntryCapability(user.id(),entryId).isUpdate();
						}
					}
					return true;
				} else { // deny all other protected requests, for now
					return false;
				}
			} else { // !isProtectedRequest
				//read request
				if (isRead == true) {
					if(entryId != null) {
						if(findEntry(user.id(),entryId)) {
							return findEntryCapability(user.id(),entryId).isRead();
						}
					}
					return true;
				} else { // !isRead
					return false;
				}
			}
		}

    	return false; 
    }
   
    
    /** Determine whether there exists any authorization for user user_index in database wiki_name. 
     * 
     * @param user_index
     * @param wiki_name
     * @return
     */
    public boolean find (int user_index) {
    	return _authorizationListing.containsKey(user_index);

    }
    
    public boolean findEntry (int user_index, int entry) {
    	return _policyListing.containsKey(user_index) && _policyListing.get(user_index).containsKey(entry);

    }

    /** Return the authorization/capability for user user_index in database wiki_name
     * FIXME: It would be better to build a map from user ids and db names to capabilities.
     * @param user_index
     * @param wiki_name
     * @return
     */
    public Capability findCapability(int user_index ) {
    	return _authorizationListing.get(user_index);

    }
    
    public Capability findEntryCapability (int user_index, int entry) {

    	return _policyListing.get(user_index).get(entry);
    }
    
    /* FIXME: #security It would be better if this also updated the authorization listing. */
    public synchronized void updateCapability(Connection con, int user_index,  Capability cap) 
    	throws SQLException {
    	PreparedStatement pStmt = null;
		
		if (find(user_index)) {
			pStmt = con.prepareStatement("UPDATE "
					+ RelationAuthorization + " " + "SET "
					+ RelAuthenticationColRead + " = ?, "
					+ RelAuthenticationColInsert + " = ?, "
					+ RelAuthenticationColDelete + " = ?, "
					+ RelAuthenticationColUpdate + " = ? "
					+ "WHERE " + RelAuthenticationColUserID
					+ " = ? " + "AND "
					+ RelAuthenticationColDatabaseName + " = ?");

			pStmt.setBoolean(1, cap.isRead());
			pStmt.setBoolean(2, cap.isInsert());
			pStmt.setBoolean(3, cap.isDelete());
			pStmt.setBoolean(4, cap.isUpdate());
			pStmt.setInt(5, user_index);
			pStmt.setString(6, _wiki.name());
			pStmt.execute();
			pStmt.close();
		} else {
			pStmt = con.prepareStatement("INSERT INTO "
					+ RelationAuthorization + "("
					+ RelAuthenticationColDatabaseName + ", "
					+ RelAuthenticationColUserID + ", "
					+ RelAuthenticationColRead + ", "
					+ RelAuthenticationColInsert + ", "
					+ RelAuthenticationColDelete + ", "
					+ RelAuthenticationColUpdate
					+ ") VALUES(?, ?, ?, ?, ?, ?)");

			pStmt.setString(1, _wiki.name());
			pStmt.setInt(2, user_index);
			pStmt.setBoolean(3, cap.isRead());
			pStmt.setBoolean(4, cap.isInsert());
			pStmt.setBoolean(5, cap.isDelete());
			pStmt.setBoolean(6, cap.isUpdate());
			pStmt.execute();
			pStmt.close();
		}
		con.commit();

		_authorizationListing.put(user_index, new Capability(cap.isRead(), cap.isInsert(), cap.isDelete(),cap.isUpdate()));
    }
    
    public synchronized void updateEntryCapability(Connection con, int user_index, int entry, Capability cap) 
    		throws SQLException {
    	
    	PreparedStatement pStmt = null;
		
		if (findEntry(user_index, entry)) {
			pStmt = con.prepareStatement("UPDATE "
					+ _wiki.name() + DatabaseConstants.RelationPolicy + " " + "SET "
					+ DatabaseConstants.RelPolicyRead + " = ?, "
					+ DatabaseConstants.RelPolicyInsert + " = ?, "
					+ DatabaseConstants.RelPolicyDelete + " = ?, "
					+ DatabaseConstants.RelPolicyUpdate + " = ? "
					+ "WHERE " + DatabaseConstants.RelPolicyEntry + " = ? "
					+ "AND " + DatabaseConstants.RelPolicyUserID + " = ?");

			pStmt.setBoolean(1, cap.isRead());
			pStmt.setBoolean(2, cap.isInsert());
			pStmt.setBoolean(3, cap.isDelete());
			pStmt.setBoolean(4, cap.isUpdate());
			pStmt.setInt(5, entry);
			pStmt.setInt(6, user_index);
			pStmt.execute();
			pStmt.close();
		} else {
			pStmt = con.prepareStatement("INSERT INTO "
					+ _wiki.name() + DatabaseConstants.RelationPolicy + "("
					+ DatabaseConstants.RelPolicyEntry + ", "
					+ DatabaseConstants.RelPolicyUserID + ", "
					+ DatabaseConstants.RelPolicyRead + ", "
					+ DatabaseConstants.RelPolicyInsert + ", "
					+ DatabaseConstants.RelPolicyDelete + ", "
					+ DatabaseConstants.RelPolicyUpdate
					+ ") VALUES(?, ?, ?, ?, ?, ?)");

			pStmt.setInt(1, entry);
			pStmt.setInt(2, user_index);
			pStmt.setBoolean(3, cap.isRead());
			pStmt.setBoolean(4, cap.isInsert());
			pStmt.setBoolean(5, cap.isDelete());
			pStmt.setBoolean(6, cap.isUpdate());
			pStmt.execute();
			pStmt.close();
		}
		con.commit();

		updateEntryCapability(user_index, entry, cap);
	}
    
    
    protected synchronized void updateEntryCapability(int user_index, int entry, Capability capability) {
    	if(!_policyListing.containsKey(user_index)) {
	    	Map<Integer,Capability> map = new HashMap<Integer,Capability>();
			map.put(entry, capability);
			_policyListing.put(user_index, map);
		} else {
			_policyListing.get(user_index).put(entry,capability);
		}
    }

}
