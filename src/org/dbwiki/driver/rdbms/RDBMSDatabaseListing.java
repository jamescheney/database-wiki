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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.data.index.DatabaseContent;

import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.WikiFatalException;

/** A listing of the entries in a database.
 * Entries can be looked up by node id or by integer index in the vector.
 * FIXME #query: Put the queries somewhere else.
 * @author jcheney
 *
 */

public class RDBMSDatabaseListing implements DatabaseContent {
	/*
	 * Private Variables
	 */
	
	private Hashtable<Integer, RDBMSDatabaseEntry> _entryIndex;
	private Vector<RDBMSDatabaseEntry> _entryList;
	private boolean _isSorted = false;
	
	
	/*
	 * Constructors
	 */
	
	public RDBMSDatabaseListing() {
		_entryIndex = new Hashtable<Integer, RDBMSDatabaseEntry>();
		_entryList = new Vector<RDBMSDatabaseEntry>();
	}
	
	/** 
	 * Creates a RDBMSDatabaseListing whose contents are loaded from the database
	 * FIXME #queries: Put queries into DatabaseConstants??
	 * @param con
	 * @param database
	 * @throws org.dbwiki.exception.WikiException
	 */
	public  RDBMSDatabaseListing(Connection con, RDBMSDatabase database) throws org.dbwiki.exception.WikiException {
		this();
		
		if (database.getDisplaySchemaNode() != null) {
			try {
				String relData = database.name() + DatabaseConstants.RelationData;
				String relTimestamp = database.name() + DatabaseConstants.RelationTimesequence;
				String relVersion = database.name() + DatabaseConstants.RelationVersion;
				
				PreparedStatement pStmtSelectContent = con.prepareStatement("SELECT " +
					relData + "." + DatabaseConstants.RelDataColID + ", " +
					relTimestamp + "." + DatabaseConstants.RelTimesequenceColStart + ", " +
					relTimestamp + "." + DatabaseConstants.RelTimesequenceColStop + " " +
					"FROM " + relData + ", " + relTimestamp + " " +
					"WHERE " + relData + "." + DatabaseConstants.RelDataColID + " = " + relData + "." + DatabaseConstants.RelDataColEntry + " AND " + 
					relData + "." + DatabaseConstants.RelDataColTimesequence + " = " + relTimestamp + "." + DatabaseConstants.RelTimesequenceColID + " " +
					"ORDER BY " +relData + "." + DatabaseConstants.RelDataColID + ", " + relTimestamp + "." + DatabaseConstants.RelTimesequenceColStart);
					
				PreparedStatement pStmtSelectLabel = con.prepareStatement("SELECT d." +
						DatabaseConstants.RelDataColEntry + ", d." + DatabaseConstants.RelDataColValue + ", " + 
						relTimestamp + "." + DatabaseConstants.RelTimesequenceColStart + ", " + relTimestamp + "." + DatabaseConstants.RelTimesequenceColStop + " FROM (" +
						"SELECT d1." + DatabaseConstants.RelDataColID + ", d1." + DatabaseConstants.RelDataColEntry + ", d1." + DatabaseConstants.RelDataColValue + ", " +
						"d1." + DatabaseConstants.RelDataColTimesequence + " " +
						"FROM " + relData + " d1, " + relData + " d2 " +
						"WHERE d1." + DatabaseConstants.RelDataColSchema + " = " + DatabaseConstants.RelDataColSchemaValUnknown + " AND " +
						"d1." + DatabaseConstants.RelDataColParent + " = d2." + DatabaseConstants.RelDataColID + " AND " +
						"d2." + DatabaseConstants.RelDataColSchema + " = ?) d " +
						"LEFT OUTER JOIN " + relTimestamp + " ON d." + DatabaseConstants.RelDataColTimesequence + " = " + relTimestamp + "." + DatabaseConstants.RelTimesequenceColID + " " +
						"ORDER BY d." + DatabaseConstants.RelDataColEntry + ", d." + DatabaseConstants.RelDataColID + " DESC, " + relTimestamp + "." + DatabaseConstants.RelTimesequenceColStop);
					
				PreparedStatement pStmtSelectLastChange = con.prepareStatement("SELECT " +
						relData + "." + DatabaseConstants.RelDataColEntry + ", MAX(" + relVersion + "." + DatabaseConstants.RelVersionColNumber + ") " +
						"FROM " + relData + ", " + relVersion + " " + 
						"WHERE " + relVersion + "." + DatabaseConstants.RelVersionColNode + " = " + relData + "." + DatabaseConstants.RelDataColID + " " +
						"GROUP BY " + relData + "." + DatabaseConstants.RelDataColEntry);
				
				ResultSet rs = pStmtSelectContent.executeQuery();
				RDBMSDatabaseEntry entry = null;
				while (rs.next()) {
					int id = rs.getInt(1);
					int start = rs.getInt(2);
					int end = rs.getInt(3);
					if (entry == null) {
						entry = new RDBMSDatabaseEntry(new NodeIdentifier(id), new TimeSequence(start, end));
						add(entry);
					} else {
						if (entry.identifier().nodeID() == id) {
							entry.timestamp().elongate(start, end);
						} else {
							entry = new RDBMSDatabaseEntry(new NodeIdentifier(id), new TimeSequence(start, end));
							add(entry);
						}
					}
				}
				rs.close();
				pStmtSelectLabel.setInt(1, database.getDisplaySchemaNode().id());
				rs = pStmtSelectLabel.executeQuery();
				entry = null;
				int maxTime = Integer.MIN_VALUE;
				while (rs.next()) {
					int id = rs.getInt(1);
					String value = rs.getString(2);
					int end = DatabaseConstants.RelTimestampColEndValOpen;
					rs.getInt(3);
					if (!rs.wasNull()) {
						end = rs.getInt(4);
					}
					if (entry == null) {
						entry = get(new NodeIdentifier(id));
						maxTime = Integer.MIN_VALUE;
					} else if (entry.identifier().nodeID() != id) {
						entry = get(new NodeIdentifier(id));
						maxTime = Integer.MIN_VALUE;
					}
					if (end == DatabaseConstants.RelTimestampColEndValOpen) {
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
					get(new NodeIdentifier(rs.getInt(1))).lastChange(rs.getInt(2));
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
		
		sort();
	}
	
	/*
	 * Public Methods
	 */
	
	public void add(RDBMSDatabaseEntry entry) {
		_entryIndex.put(new Integer(entry.identifier().nodeID()), entry);
		_entryList.add(entry);
		_isSorted = false;
	}
	
	public RDBMSDatabaseEntry getByIndex(int index) {
		return _entryList.get(index);
	}
	
	public RDBMSDatabaseEntry getByName(String key) {
		if (_isSorted) {
			int low = 0;
			int high = _entryList.size() - 1;
			while(high >= low) {
				int middle = (low + high) / 2;
				RDBMSDatabaseEntry entry = _entryList.get(middle);
				int comp = entry.label().compareTo(key);
				if(comp == 0) {
					return entry;
				}
				if(comp < 0) {
					low = middle + 1;
				}
				if(comp > 0) {
					high = middle - 1;
				}
			}
		} else {
			for (RDBMSDatabaseEntry entry : _entryList) {
				if (entry.label().equals(key)) {
					return entry;
				}
			}
		}
		return null;
	}
	
	public RDBMSDatabaseEntry getByID(int key) {
		
		for (RDBMSDatabaseEntry entry : _entryList) {
			if (entry.id() == key) {
				return entry;
			}
		}
		
		return null;
	}

	public RDBMSDatabaseEntry get(NodeIdentifier identifier) {
		return _entryIndex.get(new Integer(identifier.nodeID()));
	}
	
	public int size() {
		return _entryList.size();
	}
	
	public void sort() {
		Collections.sort(_entryList);
		_isSorted = true;
	}
}
