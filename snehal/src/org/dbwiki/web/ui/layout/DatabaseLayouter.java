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

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.DatabaseSchema;
import org.dbwiki.data.schema.SchemaNode;


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
	
	public static final String PropertyDisplaySchema = "DISPLAY_SCHEMA";
	public static final String PropertySchemaDisplayOrder = "DISPLAY_ORDER";
	public static final String PropertySchemaDisplayStyle = "DISPLAY_STYLE";
	public static final String PropertySchemaEditWithParent = "EDIT_WITH_PARENT";
	public static final String PropertySchemaLabel = "LABEL";
	public static final String PropertySchemaLabelShort = "LABEL_SHORT";
	public static final String PropertySchemaLabelAlign = "LABEL_ALIGN";
	public static final String PropertySchemaName  = "NAME";
	public static final String PropertySchemaShowContent = "SHOW_CONTENT";
	public static final String PropertySchemaStyleSheetPrefix = "CSS_PREFIX";
	public static final String PropertySchemaTextHeight = "TEXT_HEIGHT";
	
	public static final String PropertyIndexType = "INDEX_TYPE";
	
	
	/*
	 * Private Variables
	 */
	
	private int _displaySchemaID = -1;
	private String _indexType = DatabaseWiki.IndexFullList;
	private Hashtable<Integer, SchemaLayout> _layouter;
	
	
	/*
	 * Constructors
	 */
	/** Parses in a layout from a properties file.
	 * 
	 */
	public DatabaseLayouter(String config) throws org.dbwiki.exception.WikiException {
		_layouter = new Hashtable<Integer, SchemaLayout>();
		
		if (config != null) {
			try {
				Properties properties = new Properties();
				properties.load(new StringReader(config));
				if (properties.containsKey(PropertyDisplaySchema)) {
					_displaySchemaID = Integer.parseInt(properties.getProperty(PropertyDisplaySchema));
				} else {
					_displaySchemaID = -1;
				}
				if (properties.containsKey(PropertyIndexType)) {
					_indexType = properties.getProperty(PropertyIndexType);
				} else {
					_indexType = DatabaseWiki.IndexFullList;
				}
				// The following assumes that for each schema node the layout definition
				// (in property) contains a key for at least the schema node name and the
				// schema node label. This assumption should be assured by the way we store
				// layout definitions.
				int schemaID = 0;
				while (properties.containsKey(PropertySchemaName + "_" + schemaID)) {
					String name = properties.getProperty(PropertySchemaName + "_" + schemaID);
					String label = properties.getProperty(PropertySchemaLabel + "_" + schemaID);
					// Label alignment
					String labelAlign = SchemaLayout.DefaultLabelAlign;
					if (properties.getProperty(PropertySchemaLabelAlign + "_" + schemaID) != null) {
						labelAlign = properties.getProperty(PropertySchemaLabelAlign + "_" + schemaID);
					}
					// Label short form
					String labelShort = label;
					if (properties.getProperty(PropertySchemaLabelShort + "_" + schemaID) != null) {
						labelShort = properties.getProperty(PropertySchemaLabelShort + "_" + schemaID);
					}
					// Display order
					int displayOrder = schemaID;
					if (properties.getProperty(PropertySchemaDisplayOrder + "_" + schemaID) != null) {
						try {
							displayOrder = Integer.parseInt(properties.getProperty(PropertySchemaDisplayOrder + "_" + schemaID));
						} catch (NumberFormatException exception) {
						}
					}
					// Display style
					String displayStyle = SchemaLayout.DefaultDisplayStyle;
					if (properties.getProperty(PropertySchemaDisplayStyle + "_" + schemaID) != null) {
						displayStyle = properties.getProperty(PropertySchemaDisplayStyle + "_" + schemaID);
					}
					// Edit with parent
					boolean editWithParent = SchemaLayout.DefaultEditWithParent;
					if (properties.getProperty(PropertySchemaEditWithParent + "_" + schemaID) != null) {
						editWithParent = Boolean.parseBoolean(properties.getProperty(PropertySchemaEditWithParent + "_" + schemaID));
					}
					// Show content
					boolean showContent = SchemaLayout.DefaultShowContent;
					if (properties.getProperty(PropertySchemaShowContent + "_" + schemaID) != null) {
						showContent = Boolean.parseBoolean(properties.getProperty(PropertySchemaShowContent + "_" + schemaID));
					}
					// Style sheet prefix
					String styleSheetPrefix = properties.getProperty(PropertySchemaStyleSheetPrefix + "_" + schemaID);
					// Text height
					int textHeight = SchemaLayout.DefaultTextHeight;
					try {
						textHeight = Integer.parseInt(properties.getProperty(PropertySchemaTextHeight + "_" + schemaID));
					} catch (NumberFormatException exception) {
					}
					SchemaLayout layout = new SchemaLayout(name, label, labelAlign, labelShort, displayOrder, displayStyle, editWithParent, showContent, styleSheetPrefix, textHeight);
	        		_layouter.put(new Integer(schemaID++), layout);
				}
			} catch (java.io.IOException ioException) {
				throw new WikiFatalException(ioException);
			}
		}
	}
	
	public DatabaseLayouter() {
		_layouter = new Hashtable<Integer, SchemaLayout>();
	}
	
	
	/*
	 * Static Methods
	 */
	
	public static boolean isLayoutParameter(String name) {
		if (name.equals(PropertyDisplaySchema)) {
			return true;
		} else if (name.equals(PropertyIndexType)) {
			return true;
		} else if (name.startsWith(PropertySchemaDisplayOrder)) {
			return true;
		} else if (name.startsWith(PropertySchemaDisplayStyle)) {
			return true;
		} else if (name.startsWith(PropertySchemaEditWithParent)) {
			return true;
		} else if (name.startsWith(PropertySchemaLabel)) {
			return true;
		} else if (name.startsWith(PropertySchemaLabelAlign)) {
			return true;
		} else if (name.startsWith(PropertySchemaLabelShort)) {
			return true;
		} else if (name.startsWith(PropertySchemaName)) {
			return true;
		} else if (name.startsWith(PropertySchemaShowContent)) {
			return true;
		} else if (name.startsWith(PropertySchemaStyleSheetPrefix)) {
			return true;
		} else if (name.startsWith(PropertySchemaTextHeight)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/*
	 * Public Methods
	 */
	/** Get the display schema node associated with this database.
	 * This is an AttributeSchemaNode whose value is used to display entries at the listing top-level.
	 * Unless otherwise specified, this will be the first attribute we can find in the schema.
	 * @param schema
	 * @return
	 */
	public AttributeSchemaNode displaySchemaNode(DatabaseSchema schema) {
		if (_displaySchemaID >= 0) {
			if (schema != null) {
				if (schema.size() > _displaySchemaID) {
					return (AttributeSchemaNode)schema.get(_displaySchemaID);
				}
			}
		} else {
			if (schema != null) {
				for (int i = 1; i < schema.size(); i++) {
					if (schema.get(i).isAttribute()) {
						return (AttributeSchemaNode)schema.get(i);
					}
				}
			}
		}
		return null;
	}
	
	public SchemaLayout get(SchemaNode schema) {
		Integer key = new Integer(schema.id());
		if (_layouter.containsKey(key)) {
			return _layouter.get(key);
		} else {
			return new SchemaLayout(schema);
		}
	}
	
	public String indexType() {
		return _indexType;
	}
	
	public int size() {
		return _layouter.size();
	}
}
