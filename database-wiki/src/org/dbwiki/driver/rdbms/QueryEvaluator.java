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

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.data.schema.SchemaNode;

/** Provides static methods to evaluate wiki path queries 
 * FIXME #static Find a better place for this code; reorganize with DatabaseReader
 * @author jcheney
 *
 */
public class QueryEvaluator extends DatabaseReader {
	/*
	 * Public Methods
	 */
	
	public static QueryResultSet evaluate(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		Vector<NodeIdentifier> entries = null;

		if (query.firstElement().hasCondition()) {
			if (query.firstElement().condition().isIndexCondition()) {
				entries = new Vector<NodeIdentifier>();
				NodeIdentifier nid = findEntryByIndex(con, database, ((WikiPathIndexCondition)query.firstElement().condition()).index());
				if (nid != null) {
					entries.add(nid);
				}
			}
		}

		if (entries == null) {
			entries = findEntryCandidates(con, database, query);
		}
		
		VectorQueryResultSet rs = new VectorQueryResultSet();
		
		for (int iEntry = 0; iEntry < entries.size(); iEntry++) {
			RDBMSDatabaseGroupNode entry = (RDBMSDatabaseGroupNode)get(con, database, entries.get(iEntry));
			eval(entry, query, 0, rs);
		}
		
		return rs;
	}
	
	
	/*
	 * Private Variables
	 */
	
	private static void eval(DatabaseElementNode node, WikiPathQueryStatement query, int pos, VectorQueryResultSet rs) throws org.dbwiki.exception.WikiException {
		WikiPathComponent component = query.get(pos);
		
		boolean isValid = true;
		
		if (component.hasCondition()) {
			if (component.condition().isValueCondition()) {
				isValid = false;
				WikiPathValueCondition valueCond = (WikiPathValueCondition)component.condition();
				DatabaseElementList nodes = ((DatabaseGroupNode)node).find(valueCond.schema());
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
				int schemaIndex = 0;
				for (int iChild = 0; iChild < group.children().size(); iChild++) {
					if (group.children().get(iChild).schema().equals(nextComp.schema())) {
						schemaIndex++;
						if (nextComp.hasCondition()) {
							if (nextComp.condition().isIndexCondition()) {
								if (((WikiPathIndexCondition)nextComp.condition()).index() == schemaIndex) {
									eval(group.children().get(iChild), query, pos + 1, rs);
									break;
								}
							} else {
								eval(group.children().get(iChild), query, pos + 1, rs);
							}
						} else {
							eval(group.children().get(iChild), query, pos + 1, rs);
						}
					}
				}
			}
		}
	}

	private static NodeIdentifier findEntryByIndex(Connection con, RDBMSDatabase database, int index) throws java.sql.SQLException {
		NodeIdentifier nid = null;
		
		Statement stmt = con.createStatement();
		
		String query = "SELECT DISTINCT " + RelDataColEntry + " " +
						"FROM " + database.name() + RelationData + " " +
						"ORDER BY " + RelDataColEntry;
		ResultSet rs = stmt.executeQuery(query);
		
		int count = 0;
		
		while (rs.next()) {
			count++;
			if (count == index) {
				nid = new NodeIdentifier(rs.getInt(1));
				break;
			}
		}
		
		rs.close();
		
		stmt.close();
		
		return nid;
	}
	
	private static Vector<NodeIdentifier> findEntryCandidates(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException {
		Vector<String> sqlStatements = new Vector<String>();
		Vector<String> parameters = new Vector<String>();
		
		if (query.firstElement().hasCondition()) {
			if (query.firstElement().condition().isValueCondition()) {
				WikiPathValueCondition valueCond = (WikiPathValueCondition)query.firstElement().condition();
				sqlStatements.add(getSchemaValueStatement(database, valueCond));
				parameters.add(valueCond.value());
			}
		}
		for (int iComponent = 1; iComponent < query.size(); iComponent++) {
			WikiPathComponent component = query.get(iComponent);
			if (component.hasCondition()) {
				if (component.condition().isIndexCondition()) {
					sqlStatements.add(getSchemaIndexStatement(database, component.schema(), ((WikiPathIndexCondition)component.condition()).index()));
				} else {
					WikiPathValueCondition valueCond = (WikiPathValueCondition)component.condition();
					sqlStatements.add(getSchemaValueStatement(database, valueCond));
					parameters.add(valueCond.value());
				}
			} else {
				sqlStatements.add(getSchemaIndexStatement(database, component.schema(), 1));
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
		
		Vector<NodeIdentifier> result = new Vector<NodeIdentifier>();
		
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			result.add(new NodeIdentifier(rs.getInt(1)));
		}
		
		rs.close();
		
		return result;
	}
	
	private static String getSchemaIndexStatement(RDBMSDatabase database, SchemaNode schema, int index) {
		return "SELECT DISTINCT " + RelDataColEntry + " " +
			"FROM " + database.name() + ViewSchemaIndex + " " +
			"WHERE " + ViewSchemaIndexColMaxCount + " >= " + index + " " +
			"AND " + RelDataColSchema + " = " + schema.id();
	}
	
	private static String getSchemaValueStatement(RDBMSDatabase database, WikiPathValueCondition condition) {
		return "SELECT DISTINCT d1." + RelDataColEntry + " " +
			"FROM " + database.name() + RelationData + " d1, " + database.name() + RelationData + " d2 " +
			"WHERE d1." + RelDataColSchema + " = " + RelDataColSchemaValUnknown + " " +
			"AND d1." + RelDataColValue + " = ? " +
			"AND d1." + RelDataColParent + " = d2." + RelDataColID + " " +
			"AND d2." + RelDataColSchema + " = " + condition.schema().id();
	}
}
