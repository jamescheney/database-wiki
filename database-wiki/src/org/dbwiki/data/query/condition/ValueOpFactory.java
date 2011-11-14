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

/** Generator for value operators during XAQL query parsing.
 * 
 * @author hmueller
 *
 */
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiQueryException;

public class ValueOpFactory {

	/*
	 * Public Constants
	 */
	
	public static final String EQ = "=";
	public static final String GEQ = ">=";
	public static final String GT = ">";
	public static final String IN = "IN";
	public static final String LEQ = "<=";
	public static final String LIKE = "LIKE";
	public static final String LT = "<";
	public static final String MATCHES = "MATCHES";
	public static final String NEQ1 = "!=";
	public static final String NEQ2 = "<>";
	
	
	/*
	 * Public Methods
	 */

	public ValueOp get(String operatorDef, String value) throws org.dbwiki.exception.WikiException {
		
		final byte typeInt = 1;
		final byte typeString = 2;

		int intValue = 0;
		String strValue = null;
		byte type;
		
		if (value.startsWith("'")) {
			if (value.endsWith("'")) {
				strValue = value.replaceAll("\\\\", "\\").replaceAll("\\\'", "\'");
				strValue = strValue.substring(1, strValue.length() - 1);
				type = typeString;
			} else {
				throw new WikiQueryException(WikiQueryException.InvalidQueryStatement, "Missing ' in " + value);
			}
		} else {
			try {
				intValue = Integer.parseInt(value);
				type = typeInt;
			} catch (java.lang.NumberFormatException nfe) {
				throw new WikiQueryException(WikiQueryException.InvalidQueryStatement, "Format of " + value + " not recognized");
			}
		}

		if (operatorDef.equals(EQ)) {
			if (type == typeInt) {
				return new EQInt(intValue);
			} else {
				return new EQString(strValue);
			}
		} else if ((operatorDef.equals(NEQ1)) || (operatorDef.equals(NEQ2))) {
			if (type == typeInt) {
				return new NEQInt(intValue);
			} else {
				return new NEQString(strValue);
			}
		} else if (operatorDef.equals(LT)) {
			if (type == typeInt) {
				return new LTInt(intValue);
			} else {
				return new LTString(strValue);
			}
		} else if (operatorDef.equals(LEQ)) {
			if (type == typeInt) {
				return new LEQInt(intValue);
			} else {
				return new LEQString(strValue);
			}
		} else if (operatorDef.equals(GT)) {
			if (type == typeInt) {
				return new GTInt(intValue);
			} else {
				return new GTString(strValue);
			}
		} else if (operatorDef.equals(GEQ)) {
			if (type == typeInt) {
				return new GEQInt(intValue);
			} else {
				return new GEQString(strValue);
			}
		} else if (operatorDef.equalsIgnoreCase(LIKE)) {
			return new LIKEString(strValue);
		} else if (operatorDef.equalsIgnoreCase(MATCHES)) {
			return new MATCHESString(strValue);
		} else if (operatorDef.equalsIgnoreCase(IN)) {
			INOp inOp = new INOp();
			if (type == typeInt) {
				inOp.add(new EQInt(intValue));
			} else {
				inOp.add(new EQString(strValue));
			}
			return inOp;
		} else {
			throw new WikiFatalException("Unexpected operator " + operatorDef);
		}
	}
}
