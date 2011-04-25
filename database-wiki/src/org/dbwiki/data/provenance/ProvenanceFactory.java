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


import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;

import org.dbwiki.data.resource.ResourceIdentifier;

public class ProvenanceFactory {
	/*
	 * Public Methods
	 */
	
	public static Provenance getProvenance(byte type, User user, ResourceIdentifier identifier, String sourceURL) throws org.dbwiki.exception.WikiException {
		switch (type) {
		case Provenance.ProvenanceTypeActivate:
			return new ProvenanceActivate(user, identifier);
		case Provenance.ProvenanceTypeCopy:
			return new ProvenanceCopy(user, identifier, sourceURL);
		case Provenance.ProvenanceTypeDelete:
			return new ProvenanceDelete(user, identifier);
		case Provenance.ProvenanceTypeImport:
			return new ProvenanceImport(user, sourceURL);
		case Provenance.ProvenanceTypeInsert:
			return new ProvenanceInsert(user, identifier);
		case Provenance.ProvenanceTypeUpdate:
			return new ProvenanceUpdate(user, identifier);
		default:
			throw new WikiFatalException("Unknown provenance type " + type);
		}
	}
}
