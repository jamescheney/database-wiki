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

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.time.sequence.TimeSequence;

import org.dbwiki.exception.WikiFatalException;

public class DatabaseContentReader implements DatabaseConstants {
	/*
	 * Public Methods
	 */
	
	public RDBMSDatabaseListing get(Connection con, RDBMSDatabase database) throws org.dbwiki.exception.WikiException {
		RDBMSDatabaseListing content = new RDBMSDatabaseListing();
		
		if (database.getDisplayEntity() != null) {
			try {
				String relData = database.name() + RelationData;
				String relTimestamp = database.name() + RelationTimestamp;
				String relVersion = database.name() + RelationVersion;
				
				PreparedStatement pStmtSelectContent = con.prepareStatement("SELECT " +
					relData + "." + RelDataColID + ", " +
					relTimestamp + "." + RelTimestampColStart + ", " +
					relTimestamp + "." + RelTimestampColEnd + " " +
					"FROM " + relData + ", " + relTimestamp + " " +
					"WHERE " + relData + "." + RelDataColID + " = " + relData + "." + RelDataColEntry + " AND " + 
					relData + "." + RelDataColTimestamp + " = " + relTimestamp + "." + RelTimestampColID + " " +
					"ORDER BY " +relData + "." + RelDataColID + ", " + relTimestamp + "." + RelTimestampColStart);
					
				PreparedStatement pStmtSelectLabel = con.prepareStatement("SELECT d." +
						RelDataColEntry + ", d." + RelDataColValue + ", " + 
						relTimestamp + "." + RelTimestampColStart + ", " + relTimestamp + "." + RelTimestampColEnd + " FROM (" +
						"SELECT d1." + RelDataColID + ", d1." + RelDataColEntry + ", d1." + RelDataColValue + ", " +
						"d1." + RelDataColTimestamp + " " +
						"FROM " + relData + " d1, " + relData + " d2 " +
						"WHERE d1." + RelDataColEntity + " = " + RelDataColEntityValUnknown + " AND " +
						"d1." + RelDataColParent + " = d2." + RelDataColID + " AND " +
						"d2." + RelDataColEntity + " = ?) d " +
						"LEFT OUTER JOIN " + relTimestamp + " ON d." + RelDataColTimestamp + " = " + relTimestamp + "." + RelTimestampColID + " " +
						"ORDER BY d." + RelDataColEntry + ", d." + RelDataColID + " DESC, " + relTimestamp + "." + RelTimestampColEnd);
					
				PreparedStatement pStmtSelectLastChange = con.prepareStatement("SELECT " +
						relData + "." + RelDataColEntry + ", MAX(" + relVersion + "." + RelVersionColNumber + ") " +
						"FROM " + relData + ", " + relVersion + " " + 
						"WHERE " + relVersion + "." + RelVersionColNode + " = " + relData + "." + RelDataColID + " " +
						"GROUP BY " + relData + "." + RelDataColEntry);
				
				ResultSet rs = pStmtSelectContent.executeQuery();
				RDBMSDatabaseEntry entry = null;
				while (rs.next()) {
					int id = rs.getInt(1);
					int start = rs.getInt(2);
					int end = rs.getInt(3);
					if (entry == null) {
						entry = new RDBMSDatabaseEntry(new NID(id), new TimeSequence(start, end, database.versionIndex()));
						content.add(entry);
					} else {
						if (entry.identifier().nodeID() == id) {
							entry.timestamp().elongate(start, end);
						} else {
							entry = new RDBMSDatabaseEntry(new NID(id), new TimeSequence(start, end, database.versionIndex()));
							content.add(entry);
						}
					}
				}
				rs.close();
				pStmtSelectLabel.setInt(1, database.getDisplayEntity().id());
				rs = pStmtSelectLabel.executeQuery();
				entry = null;
				int maxTime = Integer.MIN_VALUE;
				while (rs.next()) {
					int id = rs.getInt(1);
					String value = rs.getString(2);
					int end = RelTimestampColEndValOpen;
					rs.getInt(3);
					if (!rs.wasNull()) {
						end = rs.getInt(4);
					}
					if (entry == null) {
						entry = content.get(new NID(id));
						maxTime = Integer.MIN_VALUE;
					} else if (entry.identifier().nodeID() != id) {
						entry = content.get(new NID(id));
						maxTime = Integer.MIN_VALUE;
					}
					if (end == RelTimestampColEndValOpen) {
						entry.label(value);
						maxTime = Integer.MAX_VALUE;
					} else if (end > maxTime) {
						entry.label(value);
						maxTime = end;
					}
				}
				rs.close();
				rs = pStmtSelectLastChange.executeQuery();
				while (rs.next()) {
					content.get(new NID(rs.getInt(1))).lastChange(rs.getInt(2));
				}
				rs.close();
				pStmtSelectContent.close();
				pStmtSelectLabel.close();
				pStmtSelectLastChange.close();
				con.close();
			} catch (java.sql.SQLException sqlException) {
				throw new WikiFatalException(sqlException);
			}
		}
		
		content.sort();
		
		return content;
	}
}
