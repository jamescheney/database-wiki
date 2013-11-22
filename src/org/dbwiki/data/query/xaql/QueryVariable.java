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
package org.dbwiki.data.query.xaql;

/** Query variable in a XAQL query. COntains the variable name,
 * the relative target path that defines the matching nodes and
 * the list variables that have this variable as a root.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.query.xpath.XPath;
import org.dbwiki.data.schema.SchemaNode;

public class QueryVariable {

	/*
	 * Private Variables
	 */
	
	private QueryVariableListing _children;
	private String _name;
	private XPath _targetPath;
	
	
	/*
	 * Constructors
	 */
	
	public QueryVariable(String name, XPath targetPath) {
		
		_name = name;
		_targetPath = targetPath;
		
		_children = new QueryVariableListing();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public QueryVariableListing children() {
		
		return _children;
	}
	
	public String name() {
		
		return _name;
	}
	
	public SchemaNode targetEntity() {
		
		return _targetPath.lastElement().entity();
	}
	
	public XPath targetPath() {
		
		return _targetPath;
	}
	
	@Override
	public String toString() {
		
		return "$" + _name + _targetPath.toString();
	}
}
