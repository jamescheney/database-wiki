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
import java.util.Vector;

import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.query.VectorQueryResultSet;
import org.dbwiki.data.query.WikiPathComponent;
import org.dbwiki.data.query.WikiPathQueryStatement;
import org.dbwiki.data.query.WikiPathValueCondition;

import org.dbwiki.data.resource.NodeIdentifier;

import org.dbwiki.exception.WikiFatalException;

/** Provides static methods to evaluate wiki path queries 
 * FIXME #static Find a better place for this code; reorganize with DatabaseReader
 * @author jcheney
 *
 */
public class SingleQueryEvaluator extends DatabaseReader {
	/*
	 * Public Methods
	 */
	 
	
	public static QueryResultSet evaluate(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
	
		long startevalute = System.nanoTime();
		
		// step 1: traverse path query  and generate vector of queries and parameters
		// step 2: evaluate query to get actual nodes
		Vector<NodeIdentifier> entries = findNodes(con,database,query);
				
		// step 3: for each node, get the result from DB
		VectorQueryResultSet rs = new VectorQueryResultSet();	
		for (int iEntry = 0; iEntry < entries.size(); iEntry++) {
			DatabaseNode entry = get(con, database, entries.get(iEntry));
			rs.add(entry);
		}
		
	 	long finishevalute = System.nanoTime() - startevalute;
	 	System.out.println("new finishevalute" + finishevalute);
	
		return rs;
		
		
	}
	
	private static Vector<NodeIdentifier> findNodes(Connection con, RDBMSDatabase database, WikiPathQueryStatement query) throws java.sql.SQLException, WikiFatalException {
		Vector<String> parameters = new Vector<String>();
		String fromclauses = "";
		String conditions= "";
		
		System.out.println(query.size());
		for (int iComponent = 1; iComponent < query.size(); iComponent++) {
			String start = "s"+(iComponent-1);
			String end = "s"+(iComponent);
			WikiPathComponent component = query.get(iComponent);
			fromclauses = fromclauses + ", "+ database.name() + RelationData + " "+end;
			conditions = conditions + " AND (" + sqlStep(component,start,end)+ ") ";
			if (component.hasCondition()) {
				if(component.condition().isIndexCondition()) {
					throw new WikiFatalException("Index conditions not handled");
				} else {
					WikiPathValueCondition valueCond = (WikiPathValueCondition)component.condition();
					conditions = " AND (" + sqlCondition(database, valueCond,end) + ") " +  conditions; 
				}
			} 
		}
		String last = "s"+(query.size()-1);
		String sql = null;
		
		WikiPathComponent firstComponent = query.get(0);
		if (firstComponent.hasCondition()) {
			if(firstComponent.condition().isIndexCondition()) {
				throw new WikiFatalException("Index conditions not handled");
			} else {
				WikiPathValueCondition valueCond = (WikiPathValueCondition)firstComponent.condition();
				conditions = " AND (" + sqlCondition(database, valueCond,"s0") + ") " +  conditions; 
			}
		}
		
		sql = "SELECT DISTINCT " + last+ "." + RelDataColID 
			+ " FROM " + database.name() + RelationData +" s0 " + fromclauses
			+ " WHERE ( s0.parent = -1 AND s0.schema = " + firstComponent.schema().id() + conditions + ")";
	 	System.out.println(sql);
		
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

	/** Tests whether a filter is satisfied.  Is there a descendant of s that has value t?
	 * #FIXME Not safe w.r.t timestamps!
	 * 
	 * @param database
	 * @param valueCond
	 * @param s
	 * @return
	 */
	private static String sqlCondition(RDBMSDatabase database, WikiPathValueCondition valueCond, String start) {
		
		String v = start + "_v";
		String t = start + "_t";
		return "EXISTS (SELECT * FROM " + database.name() + RelationData + " "+t+", " + database.name() + RelationData + " " + v +
			" WHERE "+v+"." + RelDataColValue + " = '" + valueCond.value() + "'" + // FIXME #security NOT secure!
			" AND "+start+"." + RelDataColPre   + " <= "+v+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " >= "+v+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+v+"." + RelDataColEntry + " " +
			" AND "+t+"." + RelDataColID + " =  "+v+"."+ RelDataColParent + " " +
			" AND "+t+"." + RelDataColSchema + " = " + valueCond.schema().id() + ")";
	}

	private static String sqlStep(WikiPathComponent component, String start, String end) throws WikiFatalException {
		if(!component.hasAxis() || component.axis().equals("child")) {
			return end+ ".parent = " + start +".id" +
			" AND "+start+"." + RelDataColPre   + " <= "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " >= "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
			
		} else if(component.axis().equals("descendant")) {
			return start+"." + RelDataColPre   + " < "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " > "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		} else if(component.axis().equals("descendant-or-self")) {
			return start+"." + RelDataColPre   + " <= "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " >= "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("parent")) {
			return start+ ".parent = " + end +".id" +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id()+
			" AND " + end +".pre < " + start +".pre "+ 
			" AND " + start+" .post < " +end+ ".post";


		}
		else if(component.axis().equals("ancestor")) {
			return start+"." + RelDataColPre   + " > "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " < "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("ancestor-or-self")) {
			return start+"." + RelDataColPre   + " >= "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " <= "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("following")) {
			return start+"." + RelDataColPre   + " < "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " < "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("proceeding")) {
			return start+"." + RelDataColPre   + " > "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " > "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("following-sibling")) {
			return end+ ".parent = " + start +".id" +
			" AND "+start+"." + RelDataColPre   + " < "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " < "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}
		else if(component.axis().equals("proceeding-sibling")) {
			return end+ ".parent = " + start +".id" +
			" AND "+start+"." + RelDataColPre   + " > "+end+"." + RelDataColPre   + " " +
			" AND "+start+"." + RelDataColPost  + " > "+end+"." + RelDataColPost  + " " +
			" AND "+start+"." + RelDataColEntry + " = "+end+"." + RelDataColEntry + " " +
			" AND " + end +".schema = " + component.schema().id();
		
		}else {
			throw new WikiFatalException("Unhandled axis " + component.axis());
		}
	}
	
	
	

		 

	
}
