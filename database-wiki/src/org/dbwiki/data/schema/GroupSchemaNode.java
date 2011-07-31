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


/** A SchemaNode is a schema type that has a name and a list of 
 * children forming the content of the group.
 *
 * @author jcheney
 *
 */
public class GroupSchemaNode extends SchemaNode {
	/*
	 * Private Variables
	 */
	
	private SchemaNodeList _children;
	
	
	/*
	 * Constructors
	 */
	public GroupSchemaNode(int id, String label, GroupSchemaNode parent, TimeSequence timestamp)
	throws org.dbwiki.exception.WikiException {
		super(id, label, parent, timestamp);
		_children = new SchemaNodeList();
	}
	
	public GroupSchemaNode(int id, String label, GroupSchemaNode parent)
	throws org.dbwiki.exception.WikiException {
		this(id, label, parent, null);
	}
	
	
	/*
	 * Public Methods
	 */

	public SchemaNodeList children() {
		return _children;
	}
	
	public SchemaNode find(String path) throws org.dbwiki.exception.WikiException {
		StringTokenizer tokens = new StringTokenizer(path, "/");
		
		SchemaNodeList children = this.children();
		while (tokens.hasMoreTokens()) {
			SchemaNode schema = children.get(tokens.nextToken());
			if (schema == null) {
				throw new WikiSchemaException(WikiSchemaException.UnknownSchemaNode, this.path() + "/" + path);
			}
			if (tokens.hasMoreTokens()) {
				if (schema.isGroup()) {
					children = ((GroupSchemaNode)schema).children();
				} else {
					throw new WikiSchemaException(WikiSchemaException.UnknownSchemaNode, this.path() + "/" + path);
				}
			} else {
				return schema;
			}
		}
		throw new WikiSchemaException(WikiSchemaException.UnknownSchemaNode, this.path() + "/" + path);
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
	public void printToBuf(StringBuffer buf,String indentation, String extend, String cr) {

		if (children().size() >= 1) {
			buf.append(indentation);
			buf.append("$" + label() + " {" + cr);
			
			for (int iChild = 0; iChild < children().size(); iChild++) {
				SchemaNode node = children().get(iChild);
				node.printToBuf(buf, indentation + extend, extend, cr);
				if(iChild < children().size()-1 && node instanceof AttributeSchemaNode) {
					buf.append(",");
				
				}
				buf.append(cr);
			}
			buf.append(indentation);
			buf.append("}");
		} else {
			buf.append(indentation);
			buf.append("$" + label());
		}
		
	}

	public void printToBuf(StringBuffer buf,String indentation) {
		this.printToBuf(buf, indentation, "\t", "\n");
	}
}
