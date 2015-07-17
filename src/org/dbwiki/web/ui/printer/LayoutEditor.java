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
package org.dbwiki.web.ui.printer;

import java.util.Vector;

import org.dbwiki.data.schema.AttributeSchemaNode;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.schema.GroupSchemaNode;

import org.dbwiki.exception.WikiException;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServerConstants;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.SchemaLayout;

/** Prints a form that generates layout requests
 * 
 * @author jcheney
 *
 */
public class LayoutEditor extends HtmlContentPrinter {
	/*
	 * Private Variables
	 */
	
	private WikiRequest _request;
	
	
	/*
	 * Constructors
	 */
	
	public LayoutEditor(WikiRequest request) {
		_request = request;
	}
	
	
	/*
	 * Public Methods
	 */

	public void print(HtmlLinePrinter printer) throws WikiException {
		Vector<SchemaNode> entities = new Vector<SchemaNode>();
		GroupSchemaNode root = _request.wiki().database().schema().root();
		entities.add(root);
		this.listSchemaNodes(root, entities);
		
		printer.paragraph("Database Layout", CSS.CSSHeadline);

		printer.openFORM("frmEditor", "POST", _request.parameters().get(RequestParameter.ParameterResource).value());
		printer.addHIDDEN(DatabaseWiki.ParameterDatabaseID, Integer.toString(_request.wiki().id()));
		printer.addHIDDEN(DatabaseWiki.ParameterFileType, Integer.toString(WikiServerConstants.RelConfigFileColFileTypeValLayout));

		printer.paragraph("Index Layout", CSS.CSSHeadlineSmall);

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);

		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Index Style");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);

		this.printIndexSelectBox(printer);
		
		printer.closeTD();
		printer.closeTR();
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Entry Label");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		
		this.printDisplayLabelSelectBox(entities, printer);
		
		printer.closeTD();
		printer.closeTR();
		
		printer.closeTABLE();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();

		for (int i = 0; i < entities.size(); i++) {
			this.printSchemaLayout(entities.get(i), printer);
		}
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addRealBUTTON("submit",
				"action", RequestParameterAction.ActionUpdate, "<img src=\"/pictures/button_save.gif\">");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addRealBUTTON("submit",
				"action", RequestParameterAction.ActionCancel, "<img src=\"/pictures/button_cancel.gif\">");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void listSchemaNodes(GroupSchemaNode schema, Vector<SchemaNode> entities) {
		for (int iChild = 0; iChild < schema.children().size(); iChild++) {
			SchemaNode child = schema.children().get(iChild);
			// skip deleted entities
			if (child.getTimestamp().isCurrent()) {
				entities.add(child);
				if (child.isGroup()) {
					this.listSchemaNodes((GroupSchemaNode)child, entities);
				}
			}
		}
	}
	
	private void printDisplayLabelSelectBox(Vector<SchemaNode> entities, HtmlLinePrinter printer) {
		int currentDisplaySchemaNodeID = -1;
		
		AttributeSchemaNode current = _request.wiki().layouter().displaySchemaNode(_request.wiki().database().schema());
		if (current != null) {
			currentDisplaySchemaNodeID = current.id();
		}
		
		int count = 0;
		
		if (entities.size() > 0) {
			for (int i = 0; i < entities.size(); i++) {
				SchemaNode schemaNode = entities.get(i);
				if (schemaNode.isAttribute()) {
					if (count > 0) {
						printer.addBR();
					}
					if (schemaNode.id() == currentDisplaySchemaNodeID) {
						printer.addRADIOBUTTON(schemaNode.path(), DatabaseLayouter.PropertyDisplaySchema, Integer.toString(schemaNode.id()), true);
					} else {
						printer.addRADIOBUTTON(schemaNode.path(), DatabaseLayouter.PropertyDisplaySchema, Integer.toString(schemaNode.id()), false);
					}
					count++;
				}
			}
		}
	}
	
	private void printDisplayOrderSelectBox(SchemaNode schema, int size, int displayOrder, HtmlLinePrinter printer) {
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display order");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);

		printer.openSELECT(DatabaseLayouter.PropertySchemaDisplayOrder + "_" + schema.id());
		for (int iOrder = 1; iOrder <=size; iOrder++) {
			printer.addOPTION(String.valueOf(iOrder), String.valueOf(iOrder), (iOrder == displayOrder));
		}
		printer.closeSELECT();
		
		printer.closeTD();
		printer.closeTR();
	}

	private void printSchemaLayout(SchemaNode schema, HtmlLinePrinter printer) {
		SchemaLayout layout = _request.wiki().layouter().get(schema);
	
		printer.paragraph("Schema node " + schema.path(), CSS.CSSHeadlineSmall);

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);

		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display name");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertySchemaName + "_" + schema.id(), "90", layout.getName());
		printer.closeTD();
		printer.closeTR();
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display label");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertySchemaLabel + "_" + schema.id(), "90", layout.getLabelDefinition());
		printer.closeTD();
		printer.closeTR();

		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display label (short form)");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertySchemaLabelShort + "_" + schema.id(), "90", layout.getLabelShortDefinition());
		printer.closeTD();
		printer.closeTR();

		if (schema.id() != 0) {
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Label alignment");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getLabelAlignment().isLeftAlign()) {
				printer.addRADIOBUTTON("Left", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "left", true);
			} else {
				printer.addRADIOBUTTON("Left", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "left", false);
			}
			printer.addBR();
			if (layout.getLabelAlignment().isTopAlign()) {
				printer.addRADIOBUTTON("Top", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "top", true);
			} else {
				printer.addRADIOBUTTON("Top", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "top", false);
			}
			printer.addBR();
			if (layout.getLabelAlignment().isNoneAlign()) {
				printer.addRADIOBUTTON("None", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "none", true);
			} else {
				printer.addRADIOBUTTON("None", DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "none", false);
			}
			printer.closeTD();
			printer.closeTR();
	
			this.printDisplayOrderSelectBox(schema, _request.wiki().database().schema().size(), layout.getDisplayOrder(), printer);
			
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Display style");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getDisplayStyle().isGroupStyle()) {
				printer.addRADIOBUTTON("Group", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "group", true);
			} else {
				printer.addRADIOBUTTON("Group", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "group", false);
			}
			printer.addBR();
			if (layout.getDisplayStyle().isListStyle()) {
				printer.addRADIOBUTTON("List", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "list", true);
			} else {
				printer.addRADIOBUTTON("List", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "list", false);
			}
			printer.addBR();
			if (layout.getDisplayStyle().isTableStyle()) {
				printer.addRADIOBUTTON("Table", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "table", true);
			} else {
				printer.addRADIOBUTTON("Table", DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "table", false);
			}
			printer.closeTD();
			printer.closeTR();
	
			if (schema.isGroup()) {
				printer.openTR();
				printer.openTD(CSS.CSSFormLabel);
				printer.text("Show content");
				printer.closeTD();
				printer.openTD(CSS.CSSFormControl);
				if (layout.getShowContent()) {
					printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "true", true);
					printer.addBR();
					printer.addRADIOBUTTON("No", DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "false", false);
				} else {
					printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "true", false);
					printer.addBR();
					printer.addRADIOBUTTON("No", DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "false", true);
				}
				printer.closeTD();
				printer.closeTR();
			} else {
				printer.addHIDDEN(DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "false");
			}
			
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Edit with parent");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getEditWithParent()) {
				printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertySchemaEditWithParent + "_" + schema.id(), "true", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", DatabaseLayouter.PropertySchemaEditWithParent + "_" + schema.id(), "false", false);
			} else {
				printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertySchemaEditWithParent + "_" + schema.id(), "true", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", DatabaseLayouter.PropertySchemaEditWithParent + "_" + schema.id(), "false", true);
			}
			printer.closeTD();
			printer.closeTR();
	
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Style sheet prefix");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			printer.addTEXTAREA(DatabaseLayouter.PropertySchemaStyleSheetPrefix + "_" + schema.id(), "90", layout.getStyleSheetPrefix());
			printer.closeTD();
			printer.closeTR();

			if (schema.isAttribute()) {
				printer.openTR();
				printer.openTD(CSS.CSSFormLabel);
				printer.text("Text height");
				printer.closeTD();
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(DatabaseLayouter.PropertySchemaTextHeight + "_" + schema.id(), "90", Integer.toString(layout.getTextHeight()));
				printer.closeTD();
				printer.closeTR();
			} else {
				printer.addHIDDEN(DatabaseLayouter.PropertySchemaTextHeight + "_" + schema.id(), "0");
			}
		} else {
			printer.addHIDDEN(DatabaseLayouter.PropertySchemaLabelAlign + "_" + schema.id(), "left");
			printer.addHIDDEN(DatabaseLayouter.PropertySchemaDisplayStyle + "_" + schema.id(), "group");
			printer.addHIDDEN(DatabaseLayouter.PropertySchemaShowContent + "_" + schema.id(), "false");
			printer.addHIDDEN(DatabaseLayouter.PropertySchemaEditWithParent + "_" + schema.id(), "false");
			printer.addHIDDEN(DatabaseLayouter.PropertySchemaTextHeight + "_" + schema.id(), "0");
		}
		
		printer.closeTABLE();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();

	}
	
	private void printIndexSelectBox(HtmlLinePrinter printer) {
		String selected = _request.wiki().layouter().indexType();
		
		if (selected.equals(DatabaseLayouter.IndexAZMultiPage)) {
			printer.addRADIOBUTTON("A-Z Multi-Page", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexAZMultiPage, true);
		} else {
			printer.addRADIOBUTTON("A-Z Multi-Page", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexAZMultiPage, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseLayouter.IndexAZSinglePage)) {
			printer.addRADIOBUTTON("A-Z Single-Page", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexAZSinglePage, true);
		} else {
			printer.addRADIOBUTTON("A-Z Single-Page", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexAZSinglePage, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseLayouter.IndexFullList)) {
			printer.addRADIOBUTTON("Full Listing", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexFullList, true);
		} else {
			printer.addRADIOBUTTON("Full Listing", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexFullList, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseLayouter.IndexMultiColumn)) {
			printer.addRADIOBUTTON("Multi Column", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexMultiColumn, true);
		} else {
			printer.addRADIOBUTTON("Multi Column", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexMultiColumn, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseLayouter.IndexPartialList)) {
			printer.addRADIOBUTTON("Partial Listing", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexPartialList, true);
		} else {
			printer.addRADIOBUTTON("Partial Listing", DatabaseLayouter.PropertyIndexType, DatabaseLayouter.IndexPartialList, false);
		}
	}
}
