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
package org.dbwiki.data.schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.dbwiki.exception.data.WikiSchemaException;


/** Parser that maps a string to a DatabaseSchema
 * FIXME #schemaparsing Replace this with a parboiled parser.
 * 
 * @author jcheney
 *
 */

public class SchemaParser {
	//
	// SYNTAX:
	//
	// $entry_path {
	//   $element {  
	//     @attribute,
	//     @attribute
	//   }
	// }
	//

	
	/*
	 * Private Constants
	 */
	
	private static final String attributeIndicator = "@";
	private static final String attributeDelimiter = ",";
	private static final String closeElement = "}";
	private static final String elementIndicator = "$";
	private static final String entryIndicator = "$";
	private static final String openElement = "{";
	
	
	
	/*
	 * Public Methods
	 */
	
	public DatabaseSchema parse(File file) throws org.dbwiki.exception.WikiException {
		String text = "";
		String line = null;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while ((line = in.readLine()) != null) {
				text = text + line.trim();
			}
			in.close();
		} catch (java.io.IOException ioException) {
			throw new WikiSchemaException(ioException);
		}
		return this.parse(text);
	}
	
	public DatabaseSchema parse(String text) throws org.dbwiki.exception.WikiException {
		DatabaseSchema schema = new DatabaseSchema();
		
		this.parseEntry(text.trim(), schema);
		
		return schema;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private int findEndOfElement(String text) {
		int openCount = 0;
		
		int iIndex = 0;
		while (iIndex < text.length()) {
			int posOpen = text.indexOf(openElement, iIndex);
			int posClose = text.indexOf(closeElement, iIndex);
			if (posClose == -1) {
				return -1;
			} else if (posOpen > 0) {
				if (posOpen < posClose) {
					openCount++;
					iIndex = posOpen + 1;
				} else {
					openCount--;
					if (openCount < 0) {
						return -1;
					} else if (openCount == 0) {
						return posClose;
					} else {
						iIndex = posClose + 1;
					}
				}
			} else {
				openCount--;
				if (openCount == 0) {
					return posClose;
				} else {
					iIndex = posClose + 1;
				}
			}
		}
		return -1;
	}
	
	private GroupSchemaNode parseElement(String text, GroupSchemaNode parent, DatabaseSchema schema) throws org.dbwiki.exception.WikiException {
		// $name {
		if (!((text.startsWith(elementIndicator)) && (text.endsWith(closeElement)))) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Element expected in " + text);
		}
		
		String elementDefinition = text.substring(elementIndicator.length(), text.length() - closeElement.length());
		
		int pos = elementDefinition.indexOf(openElement);
		if (pos == -1) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Missing open bracket in element definition " + text);
		}

		String elementName = elementDefinition.substring(0, pos).trim();
		String elementBody = elementDefinition.substring(pos + openElement.length()).trim();
		
		GroupSchemaNode schemaNode = new GroupSchemaNode(schema.size(), elementName, parent);
		if (!DatabaseSchema.isValidName(schemaNode.label())) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid element name " + elementName);
		}
		schema.add(schemaNode);

		this.parseElementBody(elementBody, schemaNode, schema);

		return schemaNode;
	}
	
	private void parseElementBody(String text, GroupSchemaNode parent, DatabaseSchema schema) throws org.dbwiki.exception.WikiException {
		String elementBody = text;
		
		if (elementBody.length() == 0) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Empty element definition for " + parent.label());
		}
		while (elementBody.length() > 0) {
			if (elementBody.startsWith(attributeIndicator)) {
				int posDelim = elementBody.indexOf(attributeDelimiter);
				String attributeName = null;
				if (posDelim != -1) {
					attributeName = elementBody.substring(attributeIndicator.length(), posDelim).trim();
					elementBody = elementBody.substring(posDelim + attributeDelimiter.length()).trim();
					if (elementBody.length() == 0) {
						throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Empty element definition in " + text);
					}
				} else {
					attributeName = elementBody.substring(attributeIndicator.length()).trim();
					elementBody = "";
				}
				AttributeSchemaNode attribute = new AttributeSchemaNode(schema.size(), attributeName, parent);
				if (!DatabaseSchema.isValidName(attribute.label())) {
					throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid attribute name " + attributeName + " in " + text);
				}
				schema.add(attribute);
			} else if (elementBody.startsWith(elementIndicator)) {
				int posDelim = this.findEndOfElement(elementBody);
				if (posDelim != -1) {
					this.parseElement(elementBody.substring(0, posDelim + closeElement.length()), parent, schema);
					elementBody = elementBody.substring(posDelim + closeElement.length()).trim();
				} else {
					throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid element body " + text);
				}
			} else {
				throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid element body " + text);
			}
		}
	}

	/** Parse something of the form $name {...}
	 * @param text - The text being parsed
	 * @param schema - Schema to add entry and its recursive contents to
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void parseEntry(String text, DatabaseSchema schema) throws org.dbwiki.exception.WikiException {
		// $name {...}
		
		if (!((text.startsWith(entryIndicator)) && (text.endsWith(closeElement)))) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Entry expected in " + text);
		}
		
		String entryDefinition = text.substring(entryIndicator.length(), text.length() - closeElement.length());
		
		int pos = entryDefinition.indexOf(openElement);
		if (pos == -1) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Missing open bracket in entry definition " + text);
		}

		String entryName = entryDefinition.substring(0, pos).trim();
		String entryBody = entryDefinition.substring(pos + openElement.length()).trim();
		
		GroupSchemaNode schemaNode = new GroupSchemaNode(schema.size(), entryName, null);
		if (!DatabaseSchema.isValidName(DatabaseSchema.getSchemaNodeName(schemaNode.label()))) {
			throw new WikiSchemaException(WikiSchemaException.SyntaxError, "Invalid entry name " + entryName);
		}
		schema.add(schemaNode);
		
		this.parseElementBody(entryBody, schemaNode, schema);

	}
}
