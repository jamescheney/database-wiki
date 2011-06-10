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
package org.dbwiki.web.ui.layout;

import java.util.Vector;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementList;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.web.request.parameter.RequestParameterVersion;

public class SubtreeLabelPrinter implements ElementLabelPrinter {
	/*
	 * Private Constants
	 */
	
	private static final String tokenCloseSchemaNodeLabel = "]]";
	private static final String tokenCloseOptionGroup = "}}";
	private static final String tokenOpenSchemaNodeLabel  = "[[";
	private static final String tokenOpenOptionGroup  = "{{";
	
	
	/*
	 * Private Interface
	 */
	
	private interface LabelDefinitionElement {
		/*
		 * Interface Methods
		 */
		
		public String getString(DatabaseElementNode node, RequestParameterVersion version);
	}
	
	
	/*
	 * Private Classes
	 */
	
	private class SchemaLabelElement implements LabelDefinitionElement {
		/*
		 * Private Variables
		 */
		
		private String _path;
		
		
		/*
		 * Constructors
		 */
		
		public SchemaLabelElement(String path) {
			_path = path;
		}
		
		
		/*
		 * Public Methods
		 */
		
		public String getString(DatabaseElementNode node, RequestParameterVersion version) {
			String label = null;
			
			try {
				DatabaseElementList nodes = ((DatabaseGroupNode)node).find(_path);
				for (int iNode = 0; iNode < nodes.size(); iNode++) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)nodes.get(iNode);
					for (int iText = 0; iText < attribute.value().size(); iText++) {
						DatabaseTextNode text = attribute.value().get(iText);
						if (version.matches(text)) {
							if (label != null) {
								label = label + ", " + text.value();
							} else {
								label = text.value();
							}
						}
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				label = "#error";
			}
			if (label != null) {
				return label;
			} else {
				return "";
			}
		}
	}
	
	private class OptionGroupLabelElement extends SubtreeLabelPrinter implements LabelDefinitionElement {
		/*
		 * Constructors
		 */
		
		public OptionGroupLabelElement(Vector<Token> tokens, int start, int end) throws org.dbwiki.exception.WikiException {
			super(tokens, start, end);
		}
		
		
		/*
		 * Public Methods
		 */
		
		public String getString(DatabaseElementNode node, RequestParameterVersion version) {
			String label = "";
			for (int iElement = 0; iElement < this.size(); iElement++) {
				String text = this.get(iElement).getString(node, version);
				if (!text.equals("")) {
					label = label + text;
				} else {
					return "";
				}
			}
			return label;
		 }
	}

	private class StringLabelElement implements LabelDefinitionElement {
		/*
		 * Private Variables
		 */
		
		private String _value;
		
		
		/*
		 * Constructors
		 */
		
		public StringLabelElement(String value) {
			_value = value;
		}
		
		
		/*
		 * Public Methods
		 */
		
		public String getString(DatabaseElementNode node, RequestParameterVersion version) {
			return _value;
		}
	}

	private class Token {
		/*
		 * Public Constants
		 */
		
		public static final byte TokenTypeCloseSchemaLabel = 0;
		public static final byte TokenTypeCloseOptionGroup = 1;
		public static final byte TokenTypeOpenSchemaLabel  = 2;
		public static final byte TokenTypeOpenOptionGroup  = 3;
		public static final byte TokenTypeStringValue      = 4;
		
		
		/*
		 * Private Variables
		 */
		
		private byte _type;
		private String _value;
		
		
		/*
		 * Constructors
		 */
		
		public Token(byte type, String value) {
			_type = type;
			_value = value;
		}
		
		public Token(byte type) {
			this(type, null);
		}
		
		
		/*
		 * Public Methods
		 */
		
		public boolean isCloseSchemaLabel() {
			return (_type == TokenTypeCloseSchemaLabel);
		}

		public boolean isCloseOptionGroup() {
			return (_type == TokenTypeCloseOptionGroup);
		}

		public boolean isOpenSchemaLabel() {
			return (_type == TokenTypeOpenSchemaLabel);
		}

		public boolean isOpenOptionGroup() {
			return (_type == TokenTypeOpenOptionGroup);
		}

		public boolean isStringValue() {
			return (_type == TokenTypeStringValue);
		}
		
		public byte type() {
			return _type;
		}
		
		public String value() {
			return _value;
		}
	}

	
	/*
	 * Private Variables
	 */
	
	private Vector<LabelDefinitionElement> _elements;
	private String _labelDefinition;
	
	/*
	 * Constructors
	 */
	
	public SubtreeLabelPrinter(String label) {
		_labelDefinition = label;
				
		String def = label;
		
		try {
			Vector<Token> tokens = new Vector<Token>();
			while (!def.equals("")) {
				int posCloseSchema = def.indexOf(tokenCloseSchemaNodeLabel);
				int posCloseGroup = def.indexOf(tokenCloseOptionGroup);
				int posOpenSchema = def.indexOf(tokenOpenSchemaNodeLabel);
				int posOpenGroup = def.indexOf(tokenOpenOptionGroup);
				switch (this.getMinPositive(posCloseSchema, posCloseGroup, posOpenSchema, posOpenGroup)) {
				case 1:
					if (posCloseSchema > 0) {
						tokens.add(new Token(Token.TokenTypeStringValue, def.substring(0, posCloseSchema)));
					}
					tokens.add(new Token(Token.TokenTypeCloseSchemaLabel));
					def = def.substring(posCloseSchema + tokenCloseSchemaNodeLabel.length());
					break;
				case 2:
					if (posCloseGroup > 0) {
						tokens.add(new Token(Token.TokenTypeStringValue, def.substring(0, posCloseGroup)));
					}
					tokens.add(new Token(Token.TokenTypeCloseOptionGroup));
					def = def.substring(posCloseGroup + tokenCloseOptionGroup.length());
					break;
				case 3:
					if (posOpenSchema > 0) {
						tokens.add(new Token(Token.TokenTypeStringValue, def.substring(0, posOpenSchema)));
					}
					tokens.add(new Token(Token.TokenTypeOpenSchemaLabel));
					def = def.substring(posOpenSchema + tokenOpenSchemaNodeLabel.length());
					break;
				case 4:
					if (posOpenGroup > 0) {
						tokens.add(new Token(Token.TokenTypeStringValue, def.substring(0, posOpenGroup)));
					}
					tokens.add(new Token(Token.TokenTypeOpenOptionGroup));
					def = def.substring(posOpenGroup + tokenOpenOptionGroup.length());
					break;
				default:
					tokens.add(new Token(Token.TokenTypeStringValue, def));
					def = "";
					break;
				}
			}
			this.init(tokens);
		} catch (Exception exception) {
			exception.printStackTrace();
			_elements = new Vector<LabelDefinitionElement>();
			_elements.add(new StringLabelElement("#layout_def"));
		}
	}
	
	public SubtreeLabelPrinter(Vector<Token> tokens, int start, int end) throws org.dbwiki.exception.WikiException {
		_labelDefinition = "";

		Vector<Token> myToken = new Vector<Token>();
		for (int iToken = start; iToken <= end; iToken++) {
			myToken.add(tokens.get(iToken));
		}
		this.init(myToken);
	}

		
	/*
	 * Public Methods
	 */
	
	public LabelDefinitionElement get(int index) {
		return _elements.get(index);
	}
	
	public String getDefinition() {
		return _labelDefinition;
	}
	
	public String getLabel(DatabaseElementNode node, RequestParameterVersion version) {
		String label = "";
		for (int iElement = 0; iElement < _elements.size(); iElement++) {
			label = label + _elements.get(iElement).getString(node, version);
		}
		return label;
	}
	
	public int size() {
		return _elements.size();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private int findClosingOptGroupToken(int start, Vector<Token> tokens) throws org.dbwiki.exception.WikiException {
		int index = start;
		int openCount = 0;
		while (index < tokens.size()) {
			Token token = tokens.get(index);
			if (token.isOpenOptionGroup()) {
				openCount++;
			} else if (token.isCloseOptionGroup()) {
				openCount--;
				if (openCount == 0) {
					return index;
				}
			}
			index++;
		}
		throw new WikiFatalException("Invalid subtree label definition");
	}
	
	private int getMinPositive(int v1, int v2, int v3, int v4) {
		int[] val = new int[4];
		if (v1 == -1) {
			val[0] = Integer.MAX_VALUE;
		} else {
			val[0] = v1;
		}
		if (v2 == -1) {
			val[1] = Integer.MAX_VALUE;
		} else {
			val[1] = v2;
		}
		if (v3 == -1) {
			val[2] = Integer.MAX_VALUE;
		} else {
			val[2] = v3;
		}
		if (v4 == -1) {
			val[3] = Integer.MAX_VALUE;
		} else {
			val[3] = v4;
		}
		
		if ((val[0] < val[1]) && (val[0] < val[2]) && (val[0] < val[3])) {
			return 1;
		} else if ((val[1] < val[0]) && (val[1] < val[2]) && (val[1] < val[3])) {
			return 2;
		} else if ((val[2] < val[0]) && (val[2] < val[1]) && (val[2] < val[3])) {
			return 3;
		} else if ((val[3] < val[0]) && (val[3] < val[1]) && (val[3] < val[2])) {
			return 4;
		} else {
			return 0;
		}
	}
	
	private void init(Vector<Token> tokens) throws org.dbwiki.exception.WikiException {
		_elements = new Vector<LabelDefinitionElement>();

		int index = 0;
		while (index < tokens.size()) {
			Token token = tokens.get(index);
			if (token.isOpenSchemaLabel()) {
				if (tokens.get(index + 2).isCloseSchemaLabel()) {
					_elements.add(new SchemaLabelElement(tokens.get(index + 1).value()));
					index += 3;
				} else {
					throw new WikiFatalException("Invalid subtree label definition");
				}
			} else if (token.isOpenOptionGroup()) {
				int closeGroup = this.findClosingOptGroupToken(index, tokens);
				_elements.add(new OptionGroupLabelElement(tokens, index + 1, closeGroup - 1));
				index = closeGroup + 1;
			} else if (token.isStringValue()) {
				_elements.add(new StringLabelElement(token.value()));
				index++;
			} else {
				throw new WikiFatalException("Unexpected token in subtree label definition: " + token.type());
			}
		}
	}
}
