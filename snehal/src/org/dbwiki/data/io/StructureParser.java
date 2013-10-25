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
/*
 * Copyright (c) 2007-2010, University of Edinburgh
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the University of Edinburgh nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF EDINBURGH OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.dbwiki.data.io;

import java.util.Stack;
import java.util.Vector;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.GroupSchemaNode;
import org.dbwiki.exception.WikiException;

/** An XML parser that scans the document and generates a schema
 * Currently, the schema is produced as a string and then eventually re-parsed by SchemaParser.
 * @author jcheney
 *
 */
public class StructureParser implements InputHandler {
	/*
	 * Private Classes
	 */
	
	private class ElementNode {
		/*
		 * Private Variables
		 */
		
		private Vector<ElementNode> _children;
		private String _label;
		
		
		/*
		 * Constructors
		 */
		
		public ElementNode(String label) {
			_label = label;
			
			_children = new Vector<ElementNode>();
		}
		
		
		/*
		 * Public Methods
		 */
		
		public Vector<ElementNode> children() {
			return _children;
		}
		
		public String label() {
			return _label;
		}

		/** Slightly hacky function to get the node associated with a given path, to use as the root. 
		 *  
		 * 	@param path
		 * @return
		 */
		public ElementNode get(String path) {
			if(path.startsWith("/"+_label)) {
				int idx = path.indexOf('/', 1); 
				if (idx == -1) {
					return this;
				} else {
					for (int iChild = 0; iChild < children().size(); iChild++) {
						ElementNode child = children().get(iChild).get(path.substring(idx));
						if (child != null) {
							return child;
						}
					}
					return null;
				}
			} else {
				return null;
			}
		}
	}
	
	
	/*
	 * Private Variables
	 */
	
	private Stack<ElementNode> _elementStack = null;
	private ElementNode _root;
	private Exception _exception = null;
	
	
	/*
	 * Public Methods
	 */
	
	public void startDocument() {
		_exception = null;
		
		_root = null;
		_elementStack = new Stack<ElementNode>();
	}
	
	public void endDocument() {
	}

	public void startElement(String label) {
		if (_root == null) {
			_root = new ElementNode(label);
			_elementStack.push(_root);
		} else {
			ElementNode currentElement = _elementStack.peek();
			ElementNode child = null;
			for (int iChild = 0; iChild < currentElement.children().size(); iChild++) {
				if (currentElement.children().get(iChild).label().equals(label)) {
					child = currentElement.children().get(iChild);
					break;
				}
			}
			if (child == null) {
				child = new ElementNode(label);
				currentElement.children().add(child);
			}
			_elementStack.push(child);
		}
	}
	
	public void startElement(String label, Attribute[] attrs) throws org.dbwiki.exception.WikiException {
		this.startElement(label);
		for (int iAttribute = 0; iAttribute < attrs.length; iAttribute++) {
			Attribute attribute = attrs[iAttribute];
			this.startElement(attribute.name());
			this.text(attribute.value().toCharArray());
			this.endElement(attribute.name());
		}
	}


	public void endElement(String label) {
		_elementStack.pop();
	}
	
	public void text(char[] value) {
	}
	
	public void exception(Exception excpt) {
		_exception = excpt;
	}
	
	public boolean hasException() {
		return (_exception != null);
	}
	
	public Exception getException() {
		return _exception;
	}

	
	
	public DatabaseSchema getDatabaseSchema(String path) throws WikiException {
		ElementNode node = _root.get(path);
		
		return buildRoot(node);
	}
	

	public DatabaseSchema getDatabaseSchema() throws WikiException {
		
		return buildRoot(_root);
	}
	
	/*
	 * Private Methods
	 */
	private void buildSchema(DatabaseSchema schema, ElementNode node, GroupSchemaNode parent) throws WikiException {
		if (node.children().size() > 0) {
			// has children, so must be an element ('GroupSchemaNode')
			GroupSchemaNode schemaNode = new GroupSchemaNode(schema.size(), node.label(), parent);
			schema.add(schemaNode);
			for (int i = 0; i < node.children().size(); i++) {
				this.buildSchema(schema, node.children().get(i), schemaNode);
			}
		} else {
			// if we've seen no element children here, then assume
			// this is an attribute
			AttributeSchemaNode schemaNode = new AttributeSchemaNode(schema.size(), node.label(), parent);
			schema.add(schemaNode);
		}
	}
	
	private DatabaseSchema buildRoot(ElementNode node) throws WikiException {
		DatabaseSchema schema = new DatabaseSchema();
		GroupSchemaNode rootSchemaNode = new GroupSchemaNode(schema.size(), node.label(), null);
		schema.add(rootSchemaNode);
		
		for (int i = 0; i < node.children().size(); i++) {
			buildSchema(schema, node.children().get(i), rootSchemaNode);
		}
		
		return schema;
	}


	
}
