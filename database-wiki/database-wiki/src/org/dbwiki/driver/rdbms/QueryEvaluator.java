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

import java.util.Vector;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.query.VectorQueryResultSet;
import org.dbwiki.data.query.WikiPathComponent;
import org.dbwiki.data.query.WikiPathIndexCondition;
import org.dbwiki.data.query.WikiPathQueryStatement;
import org.dbwiki.data.query.WikiPathValueCondition;

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.schema.Entity;

public class QueryEvaluator extends DatabaseReader {
	/*
	 * Public Methods
	 */
	
	public QueryResultSet evaluate(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		Vector<NID> entries = null;
		if (query.firstElement().hasCondition()) {
			if (query.firstElement().condition().isIndexCondition()) {
				entries = new Vector<NID>();
				NID nid = this.findEntryByIndex(con, database, ((WikiPathIndexCondition)query.firstElement().condition()).index());
				if (nid != null) {
					entries.add(nid);
				}
			}
		}
		if (entries == null) {
			entries = this.findEntryCandidates(con, database, query);
		}
		
		VectorQueryResultSet rs = new VectorQueryResultSet();
		
		for (int iEntry = 0; iEntry < entries.size(); iEntry++) {
			RDBMSDatabaseGroupNode entry = (RDBMSDatabaseGroupNode)this.get(con, database, entries.get(iEntry));
			this.eval(entry, query, 0, rs);
		}
		return rs;
	}
	
	
	/*
	 * Private Variables
	 */
	
	private void eval(DatabaseElementNode node, WikiPathQueryStatement query, int pos, VectorQueryResultSet rs) throws org.dbwiki.exception.WikiException {
		WikiPathComponent component = query.get(pos);
		
		boolean isValid = true;
		
		if (component.hasCondition()) {
			if (component.condition().isValueCondition()) {
				isValid = false;
				WikiPathValueCondition valueCond = (WikiPathValueCondition)component.condition();
				DatabaseElementList nodes = ((DatabaseGroupNode)node).find(valueCond.entity());
				for (int iNode = 0; iNode < nodes.size(); iNode++) {
					DatabaseTextNode text = (((DatabaseAttributeNode)nodes.get(iNode))).value().getCurrent();
					if (text != null) {
						if (text.value().equals(valueCond.value())) {
							isValid = true;
							break;
						}
					}
				}
			}
		}
		
		if (isValid) {
			if (pos == (query.size() - 1)) {
				rs.add(node);
			} else {
				WikiPathComponent nextComp = query.get(pos + 1);
				DatabaseGroupNode group = (DatabaseGroupNode)node;
				int entityIndex = 0;
				for (int iChild = 0; iChild < group.children().size(); iChild++) {
					if (group.children().get(iChild).entity().equals(nextComp.entity())) {
						entityIndex++;
						if (nextComp.hasCondition()) {
							if (nextComp.condition().isIndexCondition()) {
								if (((WikiPathIndexCondition)nextComp.condition()).index() == entityIndex) {
									this.eval(group.children().get(iChild), query, pos + 1, rs);
									break;
								}
							} else {
								this.eval(group.children().get(iChild), query, pos + 1, rs);
							}
						} else {
							this.eval(group.children().get(iChild), query, pos + 1, rs);
						}
					}
				}
			}
		}
	}

	private NID findEntryByIndex(Connection con, RDBMSDatabase database, int index) throws java.sql.SQLException {
		NID nid = null;
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT DISTINCT " + RelDataColEntry + " " +
				"FROM " + database.name() + RelationData + " " +
				"ORDER BY " + RelDataColEntry);
		
		int count = 0;
		
		while (rs.next()) {
			count++;
			if (count == index) {
				nid = new NID(rs.getInt(1));
				break;
			}
		}
		
		rs.close();
		
		stmt.close();
		
		return nid;
	}
	
	private Vector<NID> findEntryCandidates(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException {
		Vector<String> sqlStatements = new Vector<String>();
		Vector<String> parameters = new Vector<String>();
		
		if (query.firstElement().hasCondition()) {
			if (query.firstElement().condition().isValueCondition()) {
				WikiPathValueCondition valueCond = (WikiPathValueCondition)query.firstElement().condition();
				sqlStatements.add(this.getEntityValueStatement(database, valueCond));
				parameters.add(valueCond.value());
			}
		}
		for (int iComponent = 1; iComponent < query.size(); iComponent++) {
			WikiPathComponent component = query.get(iComponent);
			if (component.hasCondition()) {
				if (component.condition().isIndexCondition()) {
					sqlStatements.add(this.getEntityIndexStatement(database, component.entity(), ((WikiPathIndexCondition)component.condition()).index()));
				} else {
					WikiPathValueCondition valueCond = (WikiPathValueCondition)component.condition();
					sqlStatements.add(this.getEntityValueStatement(database, valueCond));
					parameters.add(valueCond.value());
				}
			} else {
				sqlStatements.add(this.getEntityIndexStatement(database, component.entity(), 1));
			}
		}
		
		String sql = null;
		
		if (sqlStatements.size() > 0) {
			sql = "SELECT DISTINCT " + RelDataColEntry + " FROM (" + sqlStatements.firstElement();
			for (int iStatement = 1; iStatement < sqlStatements.size(); iStatement++) {
				sql = sql + " INTERSECT " + sqlStatements.get(iStatement);
			}
			sql = sql + ") q ORDER BY " + RelDataColEntry;
		} else {
			sql = "SELECT DISTINCT " + RelDataColEntry + " FROM " + database.name() + RelationData + " ORDER BY " + RelDataColEntry;
		}
		
		PreparedStatement pStmt = con.prepareStatement(sql);
		for (int iParameter = 0; iParameter < parameters.size(); iParameter++) {
			pStmt.setString(iParameter + 1, parameters.get(iParameter));
		}
		
		Vector<NID> result = new Vector<NID>();
		
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			result.add(new NID(rs.getInt(1)));
		}
		
		rs.close();
		
		return result;
	}
	
	private String getEntityIndexStatement(RDBMSDatabase database, Entity entity, int index) {
		return "SELECT DISTINCT " + RelDataColEntry + " " +
			"FROM " + database.name() + ViewEntityIndex + " " +
			"WHERE " + ViewEntityIndexColMaxCount + " >= " + index + " " +
			"AND " + RelDataColEntity + " = " + entity.id();
	}
	
	private String getEntityValueStatement(RDBMSDatabase database, WikiPathValueCondition condition) {
		return "SELECT DISTINCT d1." + RelDataColEntry + " " +
			"FROM " + database.name() + RelationData + " d1, " + database.name() + RelationData + " d2 " +
			"WHERE d1." + RelDataColEntity + " = " + RelDataColEntityValUnknown + " " +
			"AND d1." + RelDataColValue + " = ? " +
			"AND d1." + RelDataColParent + " = d2." + RelDataColID + " " +
			"AND d2." + RelDataColEntity + " = " + condition.entity().id();
	}
}
