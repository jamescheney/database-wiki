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
package org.dbwiki.data.query;

import java.util.Vector;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.exception.data.WikiQueryException;

public class WikiPathQueryStatement implements QueryStatement {
	/*
	 * Private Variables
	 */
	
	private Vector<WikiPathComponent> _elements;
	
	
	/*
	 * Constructors
	 */
	
	public WikiPathQueryStatement(Database database, String pathExpression) throws org.dbwiki.exception.WikiException {
		//
		// Expects a XPath-like expression, i.e., /entity{[child='...'] | :<<node-index>>}/entity{[child='...'] | :<<node-index>>}/...
		// The [child='...'] | :<<node-index>>-part is optional.
		// Only one of the options [child='...'] OR :<<node-index>> are allowed.
		//
		PathTokenizer tokens = new PathTokenizer(pathExpression);
		
		if (tokens.size() > 0) {
			_elements = new Vector<WikiPathComponent>();
			for (int iToken = 0; iToken < tokens.size(); iToken++) {
				this.add(database, tokens.get(iToken));
			}
		} else {
			throw new WikiQueryException(WikiQueryException.InvalidWikiQuery, pathExpression);
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public WikiPathComponent firstElement() {
		return _elements.firstElement();
	}
	
	public WikiPathComponent get(int index) {
		return _elements.get(index);
	}
	
	public boolean isNIDStatement() {
		return false;
	}

	public boolean isWikiPathStatement() {
		return true;
	}
	
	public int size() {
		return _elements.size();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void add(Database database, String token) throws org.dbwiki.exception.WikiException {
		String label = null;
		String condition = null;
		
		boolean isIndexCondition = false;
		
		int pos = this.indexOfConditionStart(token);
		if (pos != -1) {
			label = token.substring(0, pos);
			if (token.charAt(pos) == '[') {
				condition = token.substring(pos + 1);
				if (!condition.endsWith("]")) {
					throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
				}
				condition = condition.substring(0, condition.length() - 1);
			} else {
				isIndexCondition = true;
				condition = token.substring(pos + 1);
			}
		} else {
			label = token;
		}
		
		Entity entity = null;
		
		if (_elements.size() > 0) {
			Entity parent = _elements.lastElement().entity();
			if (parent.isGroup()) {
				entity = ((GroupEntity)parent).find(label);
			} else {
				throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
			}
		} else {
			entity = database.schema().root();
			if (!entity.label().equals(label)) {
				throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
			}
		}
		
		if (condition != null) {
			if (isIndexCondition) {
				_elements.add(new WikiPathComponent(entity, new WikiPathIndexCondition(condition)));
			} else {
				if (entity.isGroup()) {
					pos = condition.indexOf('=');
					if (pos != -1) {
						Entity child = ((GroupEntity)entity).find(condition.substring(0, pos));
						if (child.isAttribute()) {
							String value = condition.substring(pos + 1);
							if ((value.startsWith("'")) && (value.endsWith("'"))) {
								_elements.add(new WikiPathComponent(entity, new WikiPathValueCondition((AttributeEntity)child, value)));
							} else {
								throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);							
							}
						} else {
							throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
						}
					} else {
						throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
					}
				} else {
					throw new WikiQueryException(WikiQueryException.InvalidWikiPathComponent, token);
				}
			}
		} else {
			_elements.add(new WikiPathComponent(entity));
		}
	}
	
	

	private int indexOfConditionStart(String token) {
		int posIndex = token.indexOf(':');
		int posValue = token.indexOf('[');
		
		if ((posIndex != -1) && (posValue == -1)) {
			return posIndex;
		} else if ((posIndex == -1) && (posValue != -1)) {
			return posValue;
		} else if ((posIndex != -1) && (posValue != -1)) {
			return Math.min(posIndex, posValue);
		} else {
			return -1;
		}
	}
	
}
