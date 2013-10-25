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
import java.net.URL;
import java.util.zip.GZIPInputStream;





import org.dbwiki.data.schema.DatabaseSchema;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;


/** Deals with loading an XML document in from a URL and loading it into the database.
 * Uses ImportHandler to abstract over the details of how the data is loaded.
 * @author jcheney
 *
 */
public class XMLDocumentImportReader {
	/*
	 * Private Variables
	 */
	private ImportHandler _importHandler;
	private InputStream _inputStream;
	private String _rootPath;
	private DatabaseSchema _schema;
	private String _sourceURL;
	private User _user;
	private boolean _validating;
	private boolean _xmlSchema;
	
	/*
	 * Constructors
	 */
	// SAX parser. It opens stream over XML. More efficient than loading whole XML into memory.
	public XMLDocumentImportReader(URL sourceURL, DatabaseSchema schema, String rootPath, User user, boolean validating, boolean xmlSchema) throws org.dbwiki.exception.WikiException {
		_sourceURL = sourceURL.toString();
		_schema = schema;
		_rootPath = rootPath;
		_user = user;
		_validating = validating;
		_xmlSchema = xmlSchema;
		
		try {
			if(_sourceURL.endsWith(".gz")) {
				_inputStream = new GZIPInputStream(sourceURL.openStream());
			} else {
				_inputStream = sourceURL.openStream();		// here we go! loading xml
			}
		} catch (java.io.IOException ioe) {
			throw new WikiFatalException(ioe);
		}
	}

	
	
	/*
	 * Public Methods
	 */
	
	public void setImportHandler(ImportHandler importHandler) {
		_importHandler = importHandler;
	}

	public void start() throws org.dbwiki.exception.WikiException {
		_importHandler.startImport(_user, _sourceURL);
		
		// jcheney: documentHandler transforms InputHandler events ImportHandler, joining char blocks
		// and checking that elements are properly nested
		// it also uses rootPath to decide which subtrees to add
		DocumentInputHandler documentHandler = new DocumentInputHandler(_schema, _rootPath, _importHandler);
		// jcheney: callbackHandler transforms SAX input stream to InputHandler interface
		SAXCallbackInputHandler callbackHandler = new SAXCallbackInputHandler(documentHandler, true);
		
		try {
			callbackHandler.parse(_inputStream, _validating, _xmlSchema);	// parsing starts; look inside!
			_inputStream.close();
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		} catch (org.xml.sax.SAXException saxException) {
			throw new WikiFatalException(saxException);
		}
		
		if (documentHandler.hasException()) {
			throw new WikiFatalException(documentHandler.getException());
		}

		_importHandler.endImport();
	}
}
