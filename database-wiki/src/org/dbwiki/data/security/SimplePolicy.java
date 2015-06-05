package org.dbwiki.data.security;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.dbwiki.web.server.WikiServerConstants;

public class SimplePolicy implements WikiServerConstants {

    public Vector<Authorization> _authorizationListing;
    public int _mode;

    
    public SimplePolicy(int mode, Vector<Authorization> authorizationListing) {
    	_mode = mode;
    	_authorizationListing = authorizationListing;
    }
    
    public SimplePolicy(int mode) {
    	_mode = mode;
    }
	/**
	 * Initialize authorization listing from database
	 * 
	 */
	public void getAuthorizationListing(Connection connection)
			throws SQLException {
		_authorizationListing = new Vector<Authorization>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+ RelationAuthorization);
		while (rs.next()) {
			_authorizationListing.add(new Authorization(rs
					.getString(RelAuthenticationColDatabaseName), rs
					.getInt(RelAuthenticationColUserID), rs
					.getBoolean(RelAuthenticationColRead), rs
					.getBoolean(RelAuthenticationColInsert), rs
					.getBoolean(RelAuthenticationColDelete), rs
					.getBoolean(RelAuthenticationColUpdate)));
		}
		
		rs.close();
		stmt.close();

	}
	


}
