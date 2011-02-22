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
package org.dbwiki.data.time;

import org.dbwiki.data.provenance.Provenance;
import java.util.Vector;
import org.dbwiki.data.database.DatabaseNode;

public interface VersionIndex extends TimestampPrinter {
	/*
	 * Interface Methods
	 */
	
	public void add(Version version) throws org.dbwiki.exception.WikiException;
	public Version get(int index);
	public Version getByNumber(int number) throws org.dbwiki.exception.WikiException;
	public Version getLastVersion();
	public Version getNextVersion(Provenance provenance);
	public Vector<Version> getNodeChanges(DatabaseNode node)  throws org.dbwiki.exception.WikiException;
	public int size();
}
