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
package org.dbwiki.data.io;

import java.io.InputStream;

import org.xml.sax.InputSource;

import org.xml.sax.XMLReader;

import org.xml.sax.helpers.XMLReaderFactory;

public class SAXDocumentParser {
	/*
	 * Public Methods
	 */
	
	public void parse(InputStream inputStream, boolean validating, boolean xmlSchema, SAXCallbackInputHandler xmlHandler) throws java.io.IOException, org.xml.sax.SAXException {
		if (inputStream != null) {
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	        parser.setFeature("http://xml.org/sax/features/validation", validating);
	        parser.setFeature("http://apache.org/xml/features/validation/schema", xmlSchema);
	        parser.setContentHandler(xmlHandler);
	        parser.setErrorHandler(xmlHandler);
	        parser.parse(new InputSource(inputStream));
			inputStream.close();
		} else {
			xmlHandler.startDocument();
			xmlHandler.endDocument();
		}
	}
}
