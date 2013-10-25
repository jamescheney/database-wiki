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
package org.dbwiki.data.resource;

/** Interface for identifiers of resources.
 * Resources include schema types (entities), pages and node ids
 * A ResourceIdentifier knows how to compare to other Resource Identifiers,
 * how to tell whether it is a root identifier,
 * how to convert itself to a parameter string (e.g. for use in a form),
 * and how to convert itself to a URL.
 * @author jcheney
 *
 */
public interface ResourceIdentifier {
	/*
	 * Interface Methods
	 */
	
	/** Compares two ResourceIdentifiers
	 * 
	 */
	public boolean equals(ResourceIdentifier identifier);
	
	/** Tests whether ResourceIdentifier is a root
	 * 
	 * @return boolean
	 */
	public boolean isRootIdentifier();
	
	/** Converts ResourceIdentifier to a string
	 * 
	 * @return String
	 */
	public String toParameterString();
	
	/** Converts ResourceIdentifier to a URL string 
	 * 
	 * @return
	 */
	public String toURLString();
}
