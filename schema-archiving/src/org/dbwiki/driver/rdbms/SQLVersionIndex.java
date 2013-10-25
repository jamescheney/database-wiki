/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.driver.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.dbwiki.data.provenance.Provenance;
import org.dbwiki.data.provenance.ProvenanceCopy;
import org.dbwiki.data.provenance.ProvenanceFactory;
import org.dbwiki.data.provenance.ProvenanceImport;

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.data.time.Version;


import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

/** Specialization of VectorVersionIndex to handle loading and storing version index via SQL.
 * 
 * @author jcheney
 *
 */
public class SQLVersionIndex extends VersionIndex implements DatabaseConstants {
	/*
	 * Private Variables
	 */
	
	private String _name;
	
	
	/*
	 * Constructors
	 */
	
	/**
	 * The empty flag indicates that no versions are yet present in the database.
	 * It is necessary for the case where we haven't yet created the version table.
	 */
	public SQLVersionIndex(Connection con, String name, UserListing users, boolean empty)
		throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		_name = name;
		if(!empty) {
			load(con,users);
		}
		

	}
	
	
	/*
	 * Public Methods
	 */
	
	public void load(Connection con,UserListing users) throws org.dbwiki.exception.WikiException, java.sql.SQLException {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + _name + RelationVersion + " ORDER BY " + RelVersionColNumber);
		while (rs.next()) {
			int versionNumber = rs.getInt(RelVersionColNumber);
			String versionName = rs.getString(RelVersionColName);
			User user = users.get(rs.getInt(RelVersionColUser));
			int nodeId = rs.getInt(RelVersionColNode);
			NodeIdentifier nid = null;
			if(nodeId != -1)
				nid = new NodeIdentifier(nodeId);
			Provenance provenance =
				ProvenanceFactory.getProvenance((byte)rs.getInt(RelVersionColProvenance),
						user, nid, rs.getString(RelVersionColSourceURL));
			long createTime = rs.getLong(RelVersionColTimeMilliSec);
			this.add(new Version(versionNumber, versionName, createTime, provenance, this));
		}	
		rs.close();
		stmt.close();	
	
	}
	public void store(Connection con) throws org.dbwiki.exception.WikiException {
		try {
			Version version = this.getLastVersion();
			PreparedStatement statement = storeUpdate(con,version);
			/*
			PreparedStatement statement = con.prepareStatement(_insertSQL);
			
			statement.setInt(1, version.number());
			statement.setString(2, version.name());
			
			Provenance provenance = version.provenance();
			
			statement.setShort(3, provenance.type());
			if (provenance.user() != null) {
				statement.setInt(4, provenance.user().id());
			} else {
				statement.setInt(4, User.UnknownUserID);
			}
			if (provenance.identifier() != null) {
				statement.setInt(5, ((NodeIdentifier)provenance.identifier()).nodeID());
			} else {
				statement.setInt(5, RelVersionColNodeValImport);
			}
			if (provenance.isCopy()) {
				statement.setString(6, ((ProvenanceCopy)provenance).sourceURL());
			} else if (provenance.isImport()) {
				statement.setString(6, ((ProvenanceImport)provenance).sourceURL());
			} else {
				statement.setString(6, null);
			}
			*/
			statement.setLong(7, version.time());
			statement.execute();
			statement.close();
		} catch (java.sql.SQLException exception) {
			throw new WikiFatalException(exception);
		}
	}
	
	private PreparedStatement storeUpdate(Connection con, Version version) throws SQLException {
		String _insertSQL = "INSERT INTO " + _name + RelationVersion + "(" +
			RelVersionColNumber + ", " +
			RelVersionColName + ", " +
			RelVersionColProvenance + ", " + 
			RelVersionColUser + ", " + 
			RelVersionColNode + ", " + 
			RelVersionColSourceURL + ", " +
			RelVersionColTimeMilliSec + ") VALUES(?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement statement = con.prepareStatement(_insertSQL);
		statement.setInt(1, version.number());
		statement.setString(2, version.name());
		
		// FIXME: #provenance Should really put this code into Provenance or Version classes 
		Provenance provenance = version.provenance();
		
		statement.setShort(3, provenance.type());
		if (provenance.user() != null) {
			statement.setInt(4, provenance.user().id());
		} else {
			statement.setInt(4, User.UnknownUserID);
		}
		if (provenance.identifier() != null) {
			statement.setInt(5, ((NodeIdentifier)provenance.identifier()).nodeID());
		} else {
			statement.setInt(5, RelVersionColNodeValImport);
		}
		if (provenance.isCopy()) {
			statement.setString(6, ((ProvenanceCopy)provenance).sourceURL());
		} else if (provenance.isImport()) {
			statement.setString(6, ((ProvenanceImport)provenance).sourceURL());
		} else {
			statement.setString(6, null);
		}
		statement.setLong(7, version.time());
		return statement;
	}
	
}

