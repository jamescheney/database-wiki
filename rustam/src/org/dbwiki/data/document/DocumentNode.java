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
package org.dbwiki.data.document;

import org.dbwiki.data.schema.SchemaNode;

/** Document nodes represent data that's being inserted??
 * Document nodes can be compared, and contain a schema node reference.
 * FIXME #node: Use generic Node<SchemaNode>
 * @author jcheney
 *
 */
public abstract class DocumentNode implements Comparable<DocumentNode> {
	/*
	 * Private Variables
	 */
	
	private SchemaNode _schema;
	/*protected int _pre = -1;
	protected int _post = -1;*/
	protected String Dewey = "";
	
	/*
	 * Constructors
	 */
	
	public DocumentNode(SchemaNode schema) {
		_schema = schema;
	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isAttribute();
	
	
	/*
	 * Public Methods
	 */
	//public abstract int doNumbering(int startingFrom);
	public abstract void doDeweyNumbering(String startingFrom);

	   
	public int compareTo(DocumentNode element) {
		if (this.schema().id() < element.schema().id()) {
			return -1;
		} else if (this.schema().id() > element.schema().id()) {
			return 1;
		} else {
			return 0;
		}
	}

	public SchemaNode schema() {
		return _schema;
	}
	
	public boolean isGroup() {
		return !this.isAttribute();
	}
	
	public String label() {
		return _schema.label();
	}
	
	/*public int getpre(){
		return _pre;
	}
	public int getpost(){
		return _post;
	}
	*/
	public String getDewey()
	{
		return Dewey;
	}

}
