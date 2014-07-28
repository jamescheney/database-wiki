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


import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;


import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.exception.data.WikiDataException;

/** An InputHandler that parses xml expecting it to be in the special form that we handle.
 * Any deviations can lead to silent problems or exceptions.
 * Uses a schema to tell InputCallbackHandler how to parse things
 * Uses a root path to decide which subtrees will correspond to entries
 * A new InputCallbackHandler is called once for each such subtree.
 * @author jcheney
 *
 */
public class DocumentInputHandler implements InputHandler {
	/*
	 * Private Variables
	 */
	
	private String _schemaNodeRootPath;
	private Exception _exception;
	private ImportHandler _importHandler;
	private InputCallbackHandler _handler;
	private String _readPath;
	private DatabaseSchema _schema;
	

	/*
	 * Constructors
	 */
	public DocumentInputHandler(DatabaseSchema schema, String schemaNodeRootPath, ImportHandler importHandler) {
		_schema = schema;
		_schemaNodeRootPath = schemaNodeRootPath;
		_importHandler = importHandler;
		
		_readPath = null;		
	}
	
	
	/*
	 * Public Methods
	 */
	@Override
	public void endDocument() throws org.dbwiki.exception.WikiException {
		if (_exception != null) {
			throw new WikiFatalException(_exception);
		}
	}

	@Override
	public void endElement(String label) throws org.dbwiki.exception.WikiException {
		if (_exception != null) {
			throw new WikiFatalException(_exception);
		}
		
		if (_handler != null) {
			_handler.endElement();
			if (_readPath.equals(_schemaNodeRootPath)) {
				_handler.endDocument();
				DocumentNode inputRoot = _handler.getRootNode();
				if (!inputRoot.isGroup()) {
					throw new WikiDataException(WikiDataException.InvaldInputData, "Group node expected as input root");
				} else {
					_importHandler.importDocument((DocumentGroupNode)inputRoot);
				}
				_handler = null;
			}
		}
		_readPath = _readPath.substring(0, _readPath.lastIndexOf("/"));
	}

	@Override
	public void exception(Exception excpt) {
		_exception = excpt;
	}

	@Override
	public Exception getException() {
		return _exception;
	}
	
	@Override
	public boolean hasException() {
		return (_exception != null);
	}
	
	@Override
	public void startDocument() throws org.dbwiki.exception.WikiException {
		_readPath = "";
	}

	@Override
	public void startElement(String label) throws org.dbwiki.exception.WikiException {
		if (_exception != null) {
			throw new WikiFatalException(_exception);
		}

		_readPath = _readPath + "/" + label;
		
		if (_handler != null) {
			_handler.startElement(label);
		} else if (_readPath.equals(_schemaNodeRootPath)) {
			_handler = new InputCallbackHandler(_schema);
			_handler.startDocument();
			_handler.startElement(label);
		}
	}

	@Override
	public void startElement(String label, Attribute[] attrs) throws org.dbwiki.exception.WikiException {
		this.startElement(label);
		for (int iAttribute = 0; iAttribute < attrs.length; iAttribute++) {
			Attribute attribute = attrs[iAttribute];
			this.startElement(attribute.name());
			this.text(attribute.value().toCharArray());
			this.endElement(attribute.name());
		}
	}

	@Override
	public void text(char[] value) throws org.dbwiki.exception.WikiException {
		if (_exception != null) {
			throw new WikiFatalException(_exception);
		}

		if (_handler != null) {
			_handler.text(new String(value));
		}
	}
	
	

}
