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

import org.dbwiki.data.database.DatabaseElementNode;

import org.dbwiki.data.schema.SchemaNode;

import org.dbwiki.web.request.parameter.RequestParameterVersion;

/** Collects the information used to lay out each schema node
 * EditWithParent - if the schema node is edited, also include its parent
 * LabelAlign - left, top or none
 * ShowContent - if the schema node is shown, show its content also (for groups)
 * TextHeight - font size of the schema node
 * DisplayStyle - group, list or table
 * @author jcheney
 *
 */

public class SchemaLayout {
	/*
	 * Public Constants
	 */
	
	public static final String DefaultDisplayStyle = DisplayStyle.DefaultStyle;
	public static final boolean DefaultEditWithParent = false; 
	public static final String DefaultLabelAlign = LabelAlignment.DefaultAlignment;
	public static final boolean DefaultShowContent = true; 
	public static final int DefaultTextHeight = 16;

	
	/*
	 * Private Variables
	 */
	
	private LabelAlignment _alignment;
	private int _displayOrder;
	private DisplayStyle _displayStyle;
	private boolean _editWithParent;
	private ElementLabelPrinter _labelPrinter;
	private ElementLabelPrinter _labelShortPrinter;
	private String _name;
	private boolean _showContent;
	private String _styleSheetPrefix;
	private int _textHeight;
	
	
	/*
	 * Constructors
	 */
	
	public SchemaLayout(SchemaNode schema) {
		_name = schema.label();
		_labelPrinter = new StringLabelPrinter(_name);
		_labelShortPrinter = new StringLabelPrinter(_name);
		_alignment = new LabelAlignment(schema);
		_displayOrder = schema.id();
		_displayStyle = new DisplayStyle();
		_editWithParent = DefaultEditWithParent;
		_showContent = DefaultShowContent;
		_styleSheetPrefix = "";
		_textHeight = DefaultTextHeight;
	}
	
	public SchemaLayout(String name, String label, String labelAlign, String labelShort, int displayOrder, String displayStyle, boolean editWithParent, boolean showContent, String styleSheetPrefix, int textHeight) {
		_name = name;
		_labelPrinter = new SubtreeLabelPrinter(label);
		_labelShortPrinter = new SubtreeLabelPrinter(labelShort);
		_alignment = new LabelAlignment(labelAlign);
		_displayOrder = displayOrder;
		_displayStyle = new DisplayStyle(displayStyle);
		_editWithParent = editWithParent;
		_showContent = showContent;
		_styleSheetPrefix = styleSheetPrefix;
		_textHeight = textHeight;
		
		if (_styleSheetPrefix == null) {
			_styleSheetPrefix = "";
		}
	}

	/*
	 * Public Methods
	 */
	
	public String getCSS(String key) {
		return _styleSheetPrefix + key;
	}
	
	public int getDisplayOrder() {
		return _displayOrder;
	}
	
	public DisplayStyle getDisplayStyle() {
		return _displayStyle;
	}
	
	public boolean getEditWithParent() {
		return _editWithParent;
	}
	
	public String getLabel(DatabaseElementNode node, RequestParameterVersion version) {
		return _labelPrinter.getLabel(node, version);
	}
	
	public String getLabelDefinition() {
		return _labelPrinter.getDefinition();
	}
	
	public String getLabelShortDefinition() {
		return _labelShortPrinter.getDefinition();
	}

	public LabelAlignment getLabelAlignment() {
		return _alignment;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getShortLabel(DatabaseElementNode node, RequestParameterVersion version) {
		return _labelShortPrinter.getLabel(node, version);
	}

	public String getStyleSheetPrefix() {
		return _styleSheetPrefix;
	}
	
	public boolean getShowContent() {
		return _showContent;
	}
	
	public int getTextHeight() {
		return _textHeight;
	}
}
