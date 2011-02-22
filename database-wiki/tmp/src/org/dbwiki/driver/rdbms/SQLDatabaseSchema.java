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

import org.dbwiki.data.schema.AttributeEntityImpl;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;
import org.dbwiki.data.schema.GroupEntityImpl;


import org.dbwiki.exception.data.WikiSchemaException;

public class SQLDatabaseSchema extends DatabaseSchema implements DatabaseConstants {
	/*
	 * Constructors
	 */
	
	public SQLDatabaseSchema(Connection con, String name) throws java.sql.SQLException, org.dbwiki.exception.WikiException {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + name  + RelationSchema + " ORDER BY " + RelSchemaColID);
		while (rs.next()) {
			int id = rs.getInt(RelSchemaColID);
			String label =rs.getString(RelSchemaColLabel);
			GroupEntity parent = null;
			if (rs.getInt(RelSchemaColParent) != -1) {
				parent = (GroupEntity)this.get(rs.getInt(RelSchemaColParent));
			}
			Entity entity = null;
			if ((rs.getInt(RelSchemaColType) == RelSchemaColTypeValAttribute) && (parent != null)) {
				entity = new AttributeEntityImpl(id, label, parent);
			} else if (rs.getInt(RelSchemaColType) == RelSchemaColTypeValGroup) {
				entity = new GroupEntityImpl(id, label, parent);
			} else {
				throw new WikiSchemaException(WikiSchemaException.InvalidEntityType, "Database value " + rs.getInt(RelSchemaColType));
			}
			this.add(entity);
		}
		rs.close();
		stmt.close();
	}
}
