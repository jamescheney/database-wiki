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

import java.util.Stack;

import org.dbwiki.data.document.DocumentAttributeNode;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiDataException;

/** Callbacks for parsing an XML file that meets our special case of XML
 * FIXME #xmlparsing: This is only used in DocumentInputHandler and so can probably be merged with it (at least as a private inner class)
 * TODO #xmlparsing: Add error handling so that we don't just fail if the XML document uses features we don't handle.
 * @author jcheney
 *
 */
public class InputCallbackHandler {
	/*
	 * Private Methods
	 */
	
	
	private Stack<DocumentNode> _elementStack;
	private int _ignoreSubtreeDepth = -1;
	private DocumentNode _root;
	private Entity _rootEntity;
	private NodeValueReader _valueReader;
	
	
	/*
	 * Constructors
	 */
	
	public InputCallbackHandler(Entity rootEntity) {
		_rootEntity = rootEntity;
		_valueReader = null;
	}
	
	public InputCallbackHandler(DatabaseSchema schema) {
		_rootEntity = schema.root();
		_valueReader = null;
	}
	/*
	 * Public Methods
	 */
	
	public void endDocument() throws org.dbwiki.exception.WikiException {
		if (!_elementStack.isEmpty()) {
			throw new WikiDataException(WikiDataException.UnexcpectedEOD, "Open elements on input stack");
		}
		if (_valueReader != null) {
			throw new WikiDataException(WikiDataException.UnexcpectedEOD, "Open elements on input stack");
		}
	}
	
	public void endElement() throws org.dbwiki.exception.WikiException {
		if (_ignoreSubtreeDepth > 0) {
			_ignoreSubtreeDepth--;
		} else if (_valueReader != null) {
			if (_valueReader.hasOpenElements()) {
				_valueReader.endElement();
			} else if (!_elementStack.isEmpty()) {
				DocumentNode currentElement = _elementStack.peek();
				if (currentElement.isAttribute()) {
					DocumentAttributeNode attribute = (DocumentAttributeNode)currentElement;
					if (!attribute.hasValue()) {
						attribute.setValue(_valueReader.value());
						_valueReader = null;
						_elementStack.pop();
					} else {
						throw new WikiDataException(WikiDataException.InvaldInputData, "Duplicate text value for attribute " + attribute.label());
					}
				} else {
					throw new WikiDataException(WikiDataException.InvaldInputData, "Unexpected text value under element " + currentElement.label());
				}
			} else {
				throw new WikiDataException(WikiDataException.InvaldInputData, "Trying to close non-existing element past end of document");
			}
		} else if (!_elementStack.isEmpty()) {
			_elementStack.pop();
		} else {
			throw new WikiDataException(WikiDataException.InvaldInputData, "Trying to close non-existing element past end of document");
		}
	}
	
	public DocumentNode getRootNode() {
		return _root;
	}
	
	public void startDocument() {
		_elementStack = new Stack<DocumentNode>();
		_ignoreSubtreeDepth = 0;
		_root = null;
	}
	
	public void startElement(String label) throws org.dbwiki.exception.WikiException {
		if (_ignoreSubtreeDepth > 0) {
			_ignoreSubtreeDepth++;
		} else if (_valueReader != null) {
			_valueReader.startElement(label);
		} else if (_root == null) {
			if (_rootEntity.label().equals(label)) {
				if (_rootEntity.isAttribute()) {
					_root = new DocumentAttributeNode((AttributeEntity)_rootEntity);
					_valueReader = new NodeValueReader();
				} else {
					_root = new DocumentGroupNode((GroupEntity)_rootEntity);
				}
				_elementStack.push(_root);
			} else {
				throw new WikiDataException(WikiDataException.InvaldInputData, "Expected root label " + _rootEntity.label() + " instead of " + label);
			}
		} else if (!_elementStack.isEmpty()) {
			DocumentNode element = _elementStack.peek();
			if (element.isGroup()) {
				DocumentGroupNode group = (DocumentGroupNode)element;
				Entity childEntity = ((GroupEntity)group.entity()).children().get(label);
				if (childEntity != null && childEntity.getTimestamp().isCurrent()) {
					DocumentNode child = null;
					if (childEntity.isAttribute()) {
						child = new DocumentAttributeNode((AttributeEntity)childEntity);
						_valueReader = new NodeValueReader();
					} else {
						child = new DocumentGroupNode((GroupEntity)childEntity);
					}
					group.children().add(child);
					_elementStack.push(child);
				} else {
					_ignoreSubtreeDepth = 1;
				}
			} else {
				throw new WikiFatalException("Missing node value reader for attribute node " + element.label());
			}
		} else {
			throw new WikiDataException(WikiDataException.InvaldInputData, "Multiple root nodes detected");
		}
	}
	
	public void text(String value) throws org.dbwiki.exception.WikiException {
		if (_ignoreSubtreeDepth <= 0) {
			if (_valueReader != null) {
				_valueReader.text(value);
			} else if (!value.trim().equals("")) {
				throw new WikiDataException(WikiDataException.InvaldInputData, "Unexpected text node " + value);
			}
		}
	}
}
