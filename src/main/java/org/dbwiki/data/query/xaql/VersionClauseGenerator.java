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

/** Generates a VERSION clause from a given set of XAQLTokens.
 * 
 * @author hmueller
 *
 */
import java.util.Iterator;

import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiQueryException;

public class VersionClauseGenerator {

	/*
	 * Public Methods
	 */
	
	public VersionClause getVersionClause(VersionIndex versionIndex, XAQLToken token) throws org.dbwiki.exception.WikiException {
		
		if (token.type() != XAQLToken.VERSION_CLAUSE) {
			throw new WikiFatalException("Invalid token type " + token.type());
		}
		
		Iterator<XAQLToken> timestampTokens = token.children().iterator();
		
		String timestamp = "";
		while (timestampTokens.hasNext()) {
			XAQLToken timestampToken = timestampTokens.next();
			if (timestampToken.type() == XAQLToken.TIMESTAMP_VALUE) {
				if (timestampToken.value().equalsIgnoreCase(("NOW"))) {
					timestamp = timestamp + TimeSequence.OpenIntervalChar;
				} else if (timestampToken.value().toUpperCase().startsWith("AT")) {
					String dateString = timestampToken.value().substring(timestampToken.value().indexOf('\'') + 1, timestampToken.value().lastIndexOf('\''));
					Version version = null;
					try {
						version = versionIndex.getVersionAt(org.dbwiki.lib.DateTime.getDate(dateString));
					} catch (java.text.ParseException perseException) {
						throw new WikiQueryException(WikiQueryException.InvalidQueryStatement, "Invalid date expression: " + dateString);
					}
					if (version != null) {
						timestamp = timestamp + version.number();
					} else {
						timestamp = timestamp + "0";
					}
				} else {
					timestamp = timestamp + timestampToken.value();
				}
			} else {
				timestamp = timestamp + timestampToken.value();
			}
		}
		
		boolean clipped = false;
		while (!clipped) {
			if (timestamp.startsWith("0")) {
				timestamp = timestamp.substring(1);
				if (timestamp.startsWith("-0")) {
					timestamp = timestamp.substring(2);
				} else if (timestamp.startsWith("-")) {
					timestamp = "1" + timestamp;
					clipped = true;
				}
				if (timestamp.startsWith(",")) {
					timestamp = timestamp.substring(1);
				}
			} else {
				clipped = true;
			}
		}
		
		if (!timestamp.equals("") && !timestamp.equals(TimeSequence.OpenIntervalChar)) {
			return new VersionClause(new TimeSequence(timestamp));
		} else {
			return new VersionClause(new TimeSequence(Integer.MAX_VALUE));
		}
	}
}
