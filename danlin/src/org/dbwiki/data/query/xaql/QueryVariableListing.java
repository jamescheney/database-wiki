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

/** Listing of variables in a XAQL query.
 * 
 * @author hmueller
 *
 */
import java.util.Hashtable;
import java.util.Vector;

import org.dbwiki.exception.data.WikiQueryException;

public class QueryVariableListing {

	/*
	 * Private Variables
	 */
	
	private Hashtable<String, QueryVariable> _variableIndex;
	private Vector<QueryVariable> _variableListing;
	
	
	/*
	 * Constructors
	 */
	
	public QueryVariableListing() {
		
		_variableIndex = new Hashtable<String, QueryVariable>();
		_variableListing = new Vector<QueryVariable>();
	}
	
	public QueryVariableListing(QueryVariable variable) throws org.dbwiki.exception.WikiException {
		
		this();
		
		this.add(variable);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(QueryVariable variable) throws org.dbwiki.exception.WikiException {
		
		if (_variableIndex.containsKey(variable.name().toUpperCase())) {
			throw new WikiQueryException(WikiQueryException.DuplicateVariableDefinition, variable.name() + " already exists");
		}
		_variableIndex.put(variable.name().toUpperCase(), variable);
		_variableListing.add(variable);
	}
	
	public QueryVariable get(int index) {
		
		return _variableListing.get(index);
	}
	
	public QueryVariable get(String name) throws org.dbwiki.exception.WikiException {
		
		if (!_variableIndex.containsKey(name.toUpperCase())) {
			throw new WikiQueryException(WikiQueryException.UnknownVariable, name);
		}
		return _variableIndex.get(name.toUpperCase());
	}
	
	public int size() {
		
		return _variableListing.size();
	}
}
