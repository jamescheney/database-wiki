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

import java.io.StringReader;

import java.util.Hashtable;
import java.util.Properties;

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.Entity;


import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.web.server.DatabaseWiki;

/** Collects the information used to lay out the data in a DatabaseWiki 
 * 
 * @author jcheney
 *
 */
public class DatabaseLayouter {
	/*
	 * Public Constants
	 */
	
	public static final String PropertyDisplayEntity = "DISPLAY_ENTIY";
	public static final String PropertyEntityDisplayOrder = "DISPLAY_ORDER";
	public static final String PropertyEntityDisplayStyle = "DISPLAY_STYLE";
	public static final String PropertyEntityEditWithParent = "EDIT_WITH_PARENT";
	public static final String PropertyEntityLabel = "LABEL";
	public static final String PropertyEntityLabelShort = "LABEL_SHORT";
	public static final String PropertyEntityLabelAlign = "LABEL_ALIGN";
	public static final String PropertyEntityName  = "NAME";
	public static final String PropertyEntityShowContent = "SHOW_CONTENT";
	public static final String PropertyEntityStyleSheetPrefix = "CSS_PREFIX";
	public static final String PropertyEntityTextHeight = "TEXT_HEIGHT";
	
	public static final String PropertyIndexType = "INDEX_TYPE";
	
	
	/*
	 * Private Variables
	 */
	
	private int _displayEntityID = -1;
	private String _indexType = DatabaseWiki.IndexFullList;
	private Hashtable<Integer, EntityLayout> _layouter;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseLayouter(String config) throws org.dbwiki.exception.WikiException {
		_layouter = new Hashtable<Integer, EntityLayout>();
		
		if (config != null) {
			try {
				Properties properties = new Properties();
				properties.load(new StringReader(config));
				if (properties.containsKey(PropertyDisplayEntity)) {
					_displayEntityID = Integer.parseInt(properties.getProperty(PropertyDisplayEntity));
				} else {
					_displayEntityID = -1;
				}
				if (properties.containsKey(PropertyIndexType)) {
					_indexType = properties.getProperty(PropertyIndexType);
				} else {
					_indexType = DatabaseWiki.IndexFullList;
				}
				// The following assumes that for each entity the layout definition
				// (in property) contains a key for at least the entity name and the
				// entity label. This assumption should be assured by the way we store
				// layout definitions.
				int entityID = 0;
				while (properties.containsKey(PropertyEntityName + "_" + entityID)) {
					String name = properties.getProperty(PropertyEntityName + "_" + entityID);
					String label = properties.getProperty(PropertyEntityLabel + "_" + entityID);
					// Label alignment
					String labelAlign = EntityLayout.DefaultLabelAlign;
					if (properties.getProperty(PropertyEntityLabelAlign + "_" + entityID) != null) {
						labelAlign = properties.getProperty(PropertyEntityLabelAlign + "_" + entityID);
					}
					// Label short form
					String labelShort = label;
					if (properties.getProperty(PropertyEntityLabelShort + "_" + entityID) != null) {
						labelShort = properties.getProperty(PropertyEntityLabelShort + "_" + entityID);
					}
					// Display order
					int displayOrder = entityID;
					if (properties.getProperty(PropertyEntityDisplayOrder + "_" + entityID) != null) {
						try {
							displayOrder = Integer.parseInt(properties.getProperty(PropertyEntityDisplayOrder + "_" + entityID));
						} catch (NumberFormatException exception) {
						}
					}
					// Display style
					String displayStyle = EntityLayout.DefaultDisplayStyle;
					if (properties.getProperty(PropertyEntityDisplayStyle + "_" + entityID) != null) {
						displayStyle = properties.getProperty(PropertyEntityDisplayStyle + "_" + entityID);
					}
					// Edit with parent
					boolean editWithParent = EntityLayout.DefaultEditWithParent;
					if (properties.getProperty(PropertyEntityEditWithParent + "_" + entityID) != null) {
						editWithParent = Boolean.parseBoolean(properties.getProperty(PropertyEntityEditWithParent + "_" + entityID));
					}
					// Show content
					boolean showContent = EntityLayout.DefaultShowContent;
					if (properties.getProperty(PropertyEntityShowContent + "_" + entityID) != null) {
						showContent = Boolean.parseBoolean(properties.getProperty(PropertyEntityShowContent + "_" + entityID));
					}
					// Style sheet prefix
					String styleSheetPrefix = properties.getProperty(PropertyEntityStyleSheetPrefix + "_" + entityID);
					// Text height
					int textHeight = EntityLayout.DefaultTextHeight;
					try {
						textHeight = Integer.parseInt(properties.getProperty(PropertyEntityTextHeight + "_" + entityID));
					} catch (NumberFormatException exception) {
					}
					EntityLayout layout = new EntityLayout(name, label, labelAlign, labelShort, displayOrder, displayStyle, editWithParent, showContent, styleSheetPrefix, textHeight);
	        		_layouter.put(new Integer(entityID++), layout);
				}
			} catch (java.io.IOException ioException) {
				throw new WikiFatalException(ioException);
			}
		}
	}
	
	public DatabaseLayouter() {
		_layouter = new Hashtable<Integer, EntityLayout>();
	}
	
	
	/*
	 * Static Methods
	 */
	
	public static boolean isLayoutParameter(String name) {
		if (name.equals(PropertyDisplayEntity)) {
			return true;
		} else if (name.equals(PropertyIndexType)) {
			return true;
		} else if (name.startsWith(PropertyEntityDisplayOrder)) {
			return true;
		} else if (name.startsWith(PropertyEntityDisplayStyle)) {
			return true;
		} else if (name.startsWith(PropertyEntityEditWithParent)) {
			return true;
		} else if (name.startsWith(PropertyEntityLabel)) {
			return true;
		} else if (name.startsWith(PropertyEntityLabelAlign)) {
			return true;
		} else if (name.startsWith(PropertyEntityLabelShort)) {
			return true;
		} else if (name.startsWith(PropertyEntityName)) {
			return true;
		} else if (name.startsWith(PropertyEntityShowContent)) {
			return true;
		} else if (name.startsWith(PropertyEntityStyleSheetPrefix)) {
			return true;
		} else if (name.startsWith(PropertyEntityTextHeight)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public AttributeEntity displayEntity(DatabaseSchema schema) {
		if (_displayEntityID >= 0) {
			if (schema != null) {
				if (schema.size() > _displayEntityID) {
					return (AttributeEntity)schema.get(_displayEntityID);
				}
			}
		} else {
			if (schema != null) {
				for (int iEntity = 1; iEntity < schema.size(); iEntity++) {
					if (schema.get(iEntity).isAttribute()) {
						return (AttributeEntity)schema.get(iEntity);
					}
				}
			}
		}
		return null;
	}
	
	public EntityLayout get(Entity entity) {
		Integer key = new Integer(entity.id());
		if (_layouter.containsKey(key)) {
			return _layouter.get(key);
		} else {
			return new EntityLayout(entity);
		}
	}
	
	public String indexType() {
		return _indexType;
	}
	
	public int size() {
		return _layouter.size();
	}
}
