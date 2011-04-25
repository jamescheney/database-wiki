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
import java.sql.Statement;


import org.dbwiki.data.provenance.Provenance;
import org.dbwiki.data.provenance.ProvenanceCopy;
import org.dbwiki.data.provenance.ProvenanceFactory;
import org.dbwiki.data.provenance.ProvenanceImport;

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.time.Version;

import org.dbwiki.data.time.version.VectorVersionIndex;
import org.dbwiki.data.time.version.VersionImpl;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

public class SQLVersionIndex extends VectorVersionIndex implements DatabaseConstants {
	/*
	 * Private Variables
	 */
	
	private String _insertSQL;
	
	
	/*
	 * Constructors
	 */
	
	public SQLVersionIndex(Connection con, String name, UserListing users) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + name + RelationVersion + " ORDER BY " + RelVersionColNumber);
		while (rs.next()) {
			int versionNumber = rs.getInt(RelVersionColNumber);
			String versionName = rs.getString(RelVersionColName);
			User user = users.get(rs.getInt(RelVersionColUser));
			Provenance provenance = ProvenanceFactory.getProvenance((byte)rs.getInt(RelVersionColProvenance), user, new NID(rs.getInt(RelVersionColNode)), rs.getString(RelVersionColSourceURL));
			long createTime = rs.getLong(RelVersionColTimeMilliSec);
			this.add(new VersionImpl(versionNumber, versionName, createTime, provenance, this));
		}
		rs.close();
		stmt.close();
		
		_insertSQL = "INSERT INTO " + name + RelationVersion + "(" +
				RelVersionColNumber + ", " +
				RelVersionColName + ", " +
				RelVersionColProvenance + ", " + 
				RelVersionColUser + ", " + 
				RelVersionColNode + ", " + 
				RelVersionColSourceURL + ", " +
				RelVersionColTimeMilliSec + ") VALUES(?, ?, ?, ?, ?, ?, ?)";
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void store(Connection con) throws org.dbwiki.exception.WikiException {
		try {
			Version version = this.getLastVersion();
			PreparedStatement pStmtInsertVersion = con.prepareStatement(_insertSQL);
			pStmtInsertVersion.setInt(1, version.number());
			pStmtInsertVersion.setString(2, version.name());
			pStmtInsertVersion.setShort(3, version.provenance().type());
			if (version.provenance().user() != null) {
				pStmtInsertVersion.setInt(4, version.provenance().user().id());
			} else {
				pStmtInsertVersion.setInt(4, User.UnknownUserID);
			}
			if (version.provenance().identifier() != null) {
				pStmtInsertVersion.setInt(5, ((NID)version.provenance().identifier()).nodeID());
			} else {
				pStmtInsertVersion.setInt(5, RelVersionColNodeValImport);
			}
			if (version.provenance().isCopy()) {
				pStmtInsertVersion.setString(6, ((ProvenanceCopy)version.provenance()).sourceURL());
			} else if (version.provenance().isImport()) {
				pStmtInsertVersion.setString(6, ((ProvenanceImport)version.provenance()).sourceURL());
			} else {
				pStmtInsertVersion.setString(6, null);
			}
			pStmtInsertVersion.setLong(7, version.time());
			pStmtInsertVersion.execute();
			pStmtInsertVersion.close();
		} catch (java.sql.SQLException exception) {
			throw new WikiFatalException(exception);
		}
	}
}
