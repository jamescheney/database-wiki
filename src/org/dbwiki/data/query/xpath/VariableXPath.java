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
package org.dbwiki.data.query.xpath;

/** XPath expression that starts at a variable in a XAQL query.
*
* @author hmueller
*
*/
import org.dbwiki.data.schema.SchemaNode;

public class VariableXPath extends XPath {

	/*
	 * Private Variables
	 */
	
	private SchemaNode _rootEntity;
	private String _variableName;
	
	
	/*
	 * Constructors
	 */
	
	public VariableXPath(String variableName, SchemaNode rootEntity) {
		
		super();
		
		_rootEntity = rootEntity;
		_variableName = variableName;
	}
	
	public VariableXPath(String variableName, SchemaNode rootEntity, XPath path) {
		
		this(variableName, rootEntity);
		
		for (int iElement = 0; iElement < path.size(); iElement++) {
			this.add(path.get(iElement));
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public SchemaNode rootEntity() {
		
		return _rootEntity;
	}
	
	public String variableName() {
		
		return _variableName;
	}
}
