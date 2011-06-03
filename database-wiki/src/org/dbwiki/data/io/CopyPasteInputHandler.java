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

import org.dbwiki.data.document.PasteAttributeNode;
import org.dbwiki.data.document.PasteDatabaseInfo;
import org.dbwiki.data.document.PasteGroupNode;
import org.dbwiki.data.document.PasteNode;
import org.dbwiki.data.document.PasteTextNode;

import org.dbwiki.exception.WikiFatalException;

/** An InputHandler that provides callbacks to parse in 
 * an XML document that was generated by CopyPasteNodeWriter, 
 * and construct a PasteNode containing the data.
 * FIXME #copypaste Avoid PasteNode.
 * @author jcheney
 *
 */
public class CopyPasteInputHandler implements InputHandler {
	/*
	 * Private Variables
	 */
	
	private PasteDatabaseInfo _databaseInfo;
	private Exception _exception;
	private Stack<PasteNode> _readStack;
	private PasteNode _root = null;
	
	
	/*
	 * Public Methods
	 */

	public void exception(Exception excpt) {
		_exception = excpt;
	}

	public Exception getException() {
		return _exception;
	}
	
	public PasteNode getPasteNode() {
		return _root;
	}

	public boolean hasException() {
		return (_exception != null);
	}

	public void startDocument() throws org.dbwiki.exception.WikiException {
		_databaseInfo = null;
		_readStack = new Stack<PasteNode>();
		_root = null;
	}
	
	public void endDocument() throws org.dbwiki.exception.WikiException {
		if (!_readStack.isEmpty()) {
			throw new WikiFatalException("Invalid document format");
		}
	}

	public void startElement(String label) throws org.dbwiki.exception.WikiException {
		throw new WikiFatalException("Invalid method call: " + this.getClass().getName() + ".startElement(" + label + ")");
	}

	public void startElement(String label, Attribute[] attrs) throws org.dbwiki.exception.WikiException {
		if ((label.equals(CopyPasteConstants.ElementLabelDatabase)) && (attrs.length == 2) && (_databaseInfo == null)) {
			String name = this.getAttribute(attrs, CopyPasteConstants.AttributeLabelDatabaseName).value();
			int version = Integer.parseInt(this.getAttribute(attrs, CopyPasteConstants.AttributeLabelVersion).value());
			String key = this.getAttribute(attrs, CopyPasteConstants.AttributeLabelID).value();
			_databaseInfo = new PasteDatabaseInfo(name, version, key);
		} else if ((label.equals(CopyPasteConstants.ElementLabelNode)) && (attrs.length == 2) && (_databaseInfo != null)) {
			if (Integer.parseInt(this.getAttribute(attrs, CopyPasteConstants.AttributeLabelType).value()) == CopyPasteConstants.NodeTypeText) {
				PasteTextNode node = new PasteTextNode(_databaseInfo);
				if (_root == null) {
					_root = node;
				} else {
					((PasteAttributeNode)_readStack.peek()).setValue(node);
				}
				_readStack.push(node);
			} else {
				throw new WikiFatalException("Invalid node type in copy & paste data stream");
			}
		} else if ((label.equals(CopyPasteConstants.ElementLabelNode)) && (attrs.length == 3) && (_databaseInfo != null)) {
			int type = Integer.parseInt(this.getAttribute(attrs, CopyPasteConstants.AttributeLabelType).value());
			String entityName = this.getAttribute(attrs, CopyPasteConstants.AttributeLabelEntityName).value();
			PasteNode node = null;
			if (type == CopyPasteConstants.NodeTypeAttribute) {
				node = new PasteAttributeNode(_databaseInfo, entityName);
			} else if (type == CopyPasteConstants.NodeTypeGroup) {
				node = new PasteGroupNode(_databaseInfo, entityName);
			} else {
				throw new WikiFatalException("Invalid node type in copy & paste data stream");
			}
			if (_root == null) {
				_root = node;
			} else {
				((PasteGroupNode)_readStack.peek()).children().add(node);
			}
			_readStack.push(node);
		} else {
			throw new WikiFatalException("Invalid element in copy & paste data stream");
		}
	}

	public void endElement(String label) throws org.dbwiki.exception.WikiException {
		if (label.equals(CopyPasteConstants.ElementLabelNode)) {
			_readStack.pop();
		}
	}

	
	public void text(char[] value) throws org.dbwiki.exception.WikiException {
		String text = new String(value);
		if (!text.trim().equals("")) {
			((PasteTextNode)_readStack.peek()).setValue(text);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private Attribute getAttribute(Attribute[] attrs, String name) throws org.dbwiki.exception.WikiException {
		for (int iAttr = 0; iAttr < attrs.length; iAttr++) {
			if (attrs[iAttr].name().equals(name)) {
				return attrs[iAttr];
			}
		}
		throw new WikiFatalException("Missing attribute " + name);
	}
}
