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
package org.dbwiki.data.provenance;


import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.user.User;

/** A class representing provenance records.
 * TODO: Make method to handle saving to SQL.
 * TODO: Make method to handle printing out as HTML. 
 * @author jcheney
 *
 */
public abstract class Provenance {
	/*
	 * Public Constants
	 */
	
	public static final byte ProvenanceTypeUnknown  = -1;
	
	public static final byte ProvenanceTypeActivate = 0;
	public static final byte ProvenanceTypeCopy     = 1;
	public static final byte ProvenanceTypeCreate   = 6;
	public static final byte ProvenanceTypeDelete   = 2;
	public static final byte ProvenanceTypeImport   = 3;
	public static final byte ProvenanceTypeInsert   = 4;
	public static final byte ProvenanceTypeUpdate   = 5;

	
	/*
	 * Private Variables
	 */
	
	private ResourceIdentifier _identifier;
	private byte _type;
	private User _user;
	
	
	/*
	 * Constructors
	 */
	
	public Provenance(byte type, User user, ResourceIdentifier identifier) {
		_type = type;
		_user = user;
		_identifier = identifier;
	}
	
	
	/*
	 * Abstract Methods
	 */
	

	public abstract String name();
	
	
	/*
	 * Public Methods
	 */
	
	public ResourceIdentifier identifier() {
		return _identifier;
	}
	
	@Deprecated
	public boolean isActivate() {
		return (_type == ProvenanceTypeActivate);
	}
	public boolean isCopy() {
		return (_type == ProvenanceTypeCopy);
	}
	public boolean isDelete() {
		return (_type == ProvenanceTypeDelete);
	}
	public boolean isImport() {
		return (_type == ProvenanceTypeImport);
	}
	@Deprecated	
	public boolean isInsert() {
		return (_type == ProvenanceTypeInsert);
	}
	public boolean isUpdate() {
		return (_type == ProvenanceTypeUpdate);
	}
	
	public byte type() {
		return _type;
	}
	
	public User user() {
		return _user;
	}
}
