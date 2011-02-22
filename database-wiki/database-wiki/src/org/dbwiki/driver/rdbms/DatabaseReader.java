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

import java.util.Hashtable;

import org.dbwiki.data.annotation.Annotation;

import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.data.resource.NID;

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.data.time.sequence.TimeSequence;

import org.dbwiki.exception.WikiFatalException;

public class DatabaseReader implements DatabaseConstants {
	/*
	 * Public Methods
	 */
	
	public DatabaseNode get(Connection con, RDBMSDatabase database, NID identifier) throws org.dbwiki.exception.WikiException {
		Hashtable<Integer, DatabaseNode> nodeIndex = new Hashtable<Integer, DatabaseNode>();
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM " + database.name() + ViewData + " " +
					"WHERE " + ViewDataColNodeEntry + " = (SELECT " + RelDataColEntry + " FROM " + database.name() + RelationData + " WHERE " + RelDataColID + " = " + identifier.nodeID() + ") " +
					"ORDER BY " + ViewDataColNodeID + ", " + ViewDataColTimestampStart + ", " + ViewDataColAnnotationID);
			DatabaseNode node = null;
			while (rs.next()) {
				int id = rs.getInt(ViewDataColNodeID);
				if ((node == null) || (((NID)node.identifier()).nodeID() != id))  {
					int entity = rs.getInt(ViewDataColNodeEntity);
					int parent = rs.getInt(ViewDataColNodeParent);
					if (entity != RelDataColEntityValUnknown) {
						RDBMSDatabaseGroupNode parentNode = null;
						if (parent != RelDataColParentValUnknown) {
							parentNode = (RDBMSDatabaseGroupNode)nodeIndex.get(new Integer(parent));
						}
						Entity schemaEntity = database.schema().get(entity);
						if (schemaEntity.isAttribute()) {
							node = new RDBMSDatabaseAttributeNode(id, (AttributeEntity)schemaEntity, parentNode);
						} else {
							node = new RDBMSDatabaseGroupNode(id, (GroupEntity)schemaEntity, parentNode);
						}
						if (parentNode != null) {
							parentNode.children().add((DatabaseElementNode)node);
						}
					} else {
						RDBMSDatabaseAttributeNode parentNode = (RDBMSDatabaseAttributeNode)nodeIndex.get(new Integer(parent));
						node =  new RDBMSDatabaseTextNode(id, parentNode, rs.getString(ViewDataColNodeValue));
						parentNode.value().add((DatabaseTextNode)node);
					}
					nodeIndex.put(new Integer(id), node);
				}
				int end = RelTimestampColEndValOpen;
				int start = rs.getInt(ViewDataColTimestampStart);
				if (!rs.wasNull()) {
					end = rs.getInt(ViewDataColTimestampEnd);
					if (!node.hasTimestamp()) {
						node.setTimestamp(new TimeSequence(start, end, database.versionIndex()));
					} else {
						((TimeSequence)node.getTimestamp()).elongate(start, end);
					}
				}
				int annotationID = rs.getInt(ViewDataColAnnotationID);
				if (!rs.wasNull()) {
					if (!node.annotation().contains(annotationID)) {
						node.annotation().add(new Annotation(annotationID, rs.getString(ViewDataColAnnotationText), rs.getString(ViewDataColAnnotationDate), database.users().get(rs.getInt(ViewDataColAnnotationUser))));
					}
				}
			}
			rs.close();
			stmt.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		return nodeIndex.get(new Integer(identifier.nodeID()));
	}
}
