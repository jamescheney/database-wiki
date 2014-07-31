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
package org.dbwiki.data.query.condition;

/** Additional provenance condition for HAS CHANGES and WAS MODIFIED conditions.
 * Is currently implemented as a list of versions that represnts the versions
 * for which a matching nodes has to satisfy the HAS CHANGES or WAS MODIFIED predicate.
 * 
 * @author hmueller
 *
 */
import java.util.Vector;

import org.dbwiki.data.provenance.Provenance;
import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.Version;

public class ProvenanceCondition {

	/*
	 * Private Variables
	 */
	
	private Vector<Version> _versions;
	
	
	/*
	 * Constructors
	 */
	
	public ProvenanceCondition() {
		
		_versions = new Vector<Version>();
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(Version version) {
		
		_versions.add(version);
	}
	
	public boolean matches(TimeSequence timestamp) {
		
		TimeInterval[] intervals = timestamp.intervals();
		
		int intervalIndex = 0;
		int versionIndex = 0;
		
		while ((intervalIndex < intervals.length) && (versionIndex < _versions.size())) {
			Version version = _versions.get(versionIndex);
			int versionNumber = version.number();
			if (version.provenance().type() == Provenance.ProvenanceTypeDelete) {
				versionNumber--;
				if (intervals[intervalIndex].end() == versionNumber) {
					return true;
				}
			} else {
				if (intervals[intervalIndex].start() == versionNumber) {
					return true;
				}
			}
			if (intervals[intervalIndex].start() >= versionNumber) {
				versionIndex++;
			} else if ((intervals[intervalIndex].end() > 0) && (intervals[intervalIndex].end() < versionNumber)) {
				intervalIndex++;
			} else {
				versionIndex++;
				intervalIndex++;
			}
		}
		return false;
	}
}
