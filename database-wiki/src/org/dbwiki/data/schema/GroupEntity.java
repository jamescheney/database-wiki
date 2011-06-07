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
package org.dbwiki.data.schema;

import java.util.StringTokenizer;

import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.exception.data.WikiSchemaException;


/** A GroupEntity is a schema type that has a name and a list of 
 * children forming the content of the group.
 *
 * @author jcheney
 *
 */
public class GroupEntity extends Entity {
	/*
	 * Private Variables
	 */
	
	private EntityList _children;
	
	
	/*
	 * Constructors
	 */
	public GroupEntity(int id, String label, GroupEntity parent, TimeSequence timestamp)
	throws org.dbwiki.exception.WikiException {
		super(id, label, parent, timestamp);
		_children = new EntityList();
	}
	
	public GroupEntity(int id, String label, GroupEntity parent)
	throws org.dbwiki.exception.WikiException {
		this(id, label, parent, null);
	}
	
	
	/*
	 * Public Methods
	 */

	public EntityList children() {
		return _children;
	}
	
	public Entity find(String path) throws org.dbwiki.exception.WikiException {
		StringTokenizer tokens = new StringTokenizer(path, "/");
		
		EntityList children = this.children();
		while (tokens.hasMoreTokens()) {
			Entity entity = children.get(tokens.nextToken());
			if (entity == null) {
				throw new WikiSchemaException(WikiSchemaException.UnknownEntity, this.path() + "/" + path);
			}
			if (tokens.hasMoreTokens()) {
				if (entity.isGroup()) {
					children = ((GroupEntity)entity).children();
				} else {
					throw new WikiSchemaException(WikiSchemaException.UnknownEntity, this.path() + "/" + path);
				}
			} else {
				return entity;
			}
		}
		throw new WikiSchemaException(WikiSchemaException.UnknownEntity, this.path() + "/" + path);
	}
	
	public boolean isAttribute() {
		return false;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append(label());
		
		int n = _children.size();
		if(n == 0) {
		} else if(n == 1){
			buf.append('/');
			buf.append(_children.get(0).toString());
		} else {
			buf.append("/{");
			for(int i = 0; i < n-1; i++) {
				buf.append(_children.get(i).toString());
				buf.append(',');
			}
			buf.append(_children.get(n-1).toString());
			buf.append("}");
		}

		return buf.toString();
	}
	
	// FIXME #schemaparsing: Evil hack to deal with irregularity of schema parser
	public void printToBuf(StringBuffer buf,String indentation) {

		if (children().size() >= 1) {
			buf.append(indentation);
			buf.append("$" + label() + " {\n");
			
			for (int iChild = 0; iChild < children().size(); iChild++) {
				Entity node = children().get(iChild);
				node.printToBuf(buf,indentation + "\t");
				if(iChild < children().size()-1 && node instanceof AttributeEntity) {
					buf.append(",");
				
				}
				buf.append("\n");
			}
			buf.append(indentation);
			buf.append("}");
		} else {
			buf.append(indentation);
			buf.append("$" + label());
		}
		
	}

	
	
}
