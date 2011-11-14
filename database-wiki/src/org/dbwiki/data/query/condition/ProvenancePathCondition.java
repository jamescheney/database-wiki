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

/** A PathCondition with an additional ProvenanceCondition. The implementing
 * sub-classes are currently HasChangesCondition and WasModifiedCondition.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.data.query.xpath.XPath;

import org.dbwiki.data.time.TimeSequence;

public abstract class ProvenancePathCondition extends PathCondition {

	/*
	 * Private Variables
	 */
	
	private ProvenanceCondition _provenanceCondition;
	
	
	/*
	 * Constructors
	 */
	
	public ProvenancePathCondition(XPath targetPath, int quantifier, boolean negated, ProvenanceCondition provenanceCondition) {
		
		super(targetPath, quantifier, negated);
		
		_provenanceCondition = provenanceCondition;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean isRootTimestamp(TimeSequence timestamp) {
		
		return ((timestamp.size() == 1) && (timestamp.intervals()[0].start() == 1) && (timestamp.isCurrent()));
	}
	
	public boolean matchesProvenanceCondition(TimeSequence timestamp) {
		
		if (_provenanceCondition != null) {
			return _provenanceCondition.matches(timestamp);
		} else {
			return true;
		}
	}
}
