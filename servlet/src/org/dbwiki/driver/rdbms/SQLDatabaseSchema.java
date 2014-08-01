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
import java.sql.ResultSet;
import java.sql.Statement;

import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.time.TimeSequence;


import org.dbwiki.exception.data.WikiSchemaException;

/** Implementation of DatabaseSchema for RDBMS.
 * Main difference is that the constructor loads the schema from the database using queries.
 * FIXME #database This could be implemented as a factory class instead of as a subclass of DatabaseSchema
 * @author jcheney
 *
 */

public class SQLDatabaseSchema extends DatabaseSchema implements DatabaseConstants {
	/*
	 * Constructors
	 */
	public SQLDatabaseSchema(Connection con, SQLVersionIndex versionIndex, String dbName) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		Statement statement = con.createStatement();
		
		// The following query returns one row for each
		// version of each schema node along with the
		// start and end points of the associated time interval.
		// The left join ensures that every schema node will be returned
		// even if its time interval is derived from its parent.
		// (In the latter case, the start and end points will be null.)
		ResultSet results =
			statement.executeQuery(
				"SELECT " +
				"s." + RelSchemaColID + " as " + RelSchemaColID + ", " +
				"s." + RelSchemaColType + " as " + RelSchemaColType + ", " +
				"s." + RelSchemaColLabel + " as " + RelSchemaColLabel + ", " +
				"s." + RelSchemaColParent + " as " + RelSchemaColParent + ", " +
				"s." + RelSchemaColUser + " as " + RelSchemaColUser + ", " +
				"s." + RelSchemaColTimesequence + " as " + RelSchemaColTimesequence + ", " +
				"t." + RelTimesequenceColStart + " as " + RelTimesequenceColStart + ", " +
				"t." + RelTimesequenceColStop + " as " + RelTimesequenceColStop + " " +
				"FROM " + dbName + RelationSchema + " AS s LEFT JOIN " +
				          dbName + RelationTimesequence + " AS t " +
				"ON s." + RelSchemaColTimesequence + " = t." + RelTimesequenceColID + " " +
				"ORDER BY s." + RelSchemaColID);

		// As in DatabaseReader.get() we assume that parents
		// are encountered before children, so it is crucial that
		// ids are assigned in such a way as to enforce this
		// constraint, and that the above query has an
		// 'order by' clause.
		while (results.next()) {
			int id = results.getInt(RelSchemaColID);
			
			// if the schema node isn't already loaded, then load it
			SchemaNode schema;
			schema = get(id); 		
			if(schema == null) {
				String label = results.getString(RelSchemaColLabel);
				GroupSchemaNode parent = null;
				if (results.getInt(RelSchemaColParent) != -1) {
					parent = (GroupSchemaNode)get(results.getInt(RelSchemaColParent));
				}

				if ((results.getInt(RelSchemaColType) == RelSchemaColTypeValAttribute) && (parent != null)) {
					schema = new AttributeSchemaNode(id, label, parent);
				} else if (results.getInt(RelSchemaColType) == RelSchemaColTypeValGroup) {
					schema = new GroupSchemaNode(id, label, parent);
				} else {
					throw new WikiSchemaException(WikiSchemaException.InvalidSchemaType, "Database value " + results.getInt(RelSchemaColType));
				}
			}

			// load the time interval - if any
			int end = RelTimestampColEndValOpen;
			int start = results.getInt(RelTimesequenceColStart);
			if(!results.wasNull()) {
				// a time interval for this node
				end = results.getInt(RelTimesequenceColStop);
				if(schema.hasTimestamp()) {
					schema.getTimestamp().elongate(start, end);
				} else {
					schema.setTimestamp(new TimeSequence(start, end));
				}
			}
			
			add(schema);
		}
		
		results.close();
		statement.close();
	}
}

