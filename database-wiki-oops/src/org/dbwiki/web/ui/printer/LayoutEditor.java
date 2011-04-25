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

import org.dbwiki.data.schema.AttributeEntity;
import org.dbwiki.data.schema.Entity;
import org.dbwiki.data.schema.GroupEntity;

import org.dbwiki.exception.WikiException;

import org.dbwiki.web.html.HtmlLinePrinter;

import org.dbwiki.web.request.WikiRequest;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterAction;

import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.WikiServerConstants;

import org.dbwiki.web.ui.CSS;

import org.dbwiki.web.ui.layout.DatabaseLayouter;
import org.dbwiki.web.ui.layout.EntityLayout;

public class LayoutEditor implements HtmlContentPrinter {
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
		Vector<Entity> entities = new Vector<Entity>();
		GroupEntity root = _request.wiki().database().schema().root();
		entities.add(root);
		this.listEntities(root, entities);
		
		printer.paragraph("Database Layout", CSS.CSSHeadline);

		printer.openFORM("frmEditor", "POST", _request.parameters().get(RequestParameter.ParameterResource).value());
		printer.addHIDDEN(DatabaseWiki.ParameterWikiID, Integer.toString(_request.wiki().id()));
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

		for (int iEntity = 0; iEntity < entities.size(); iEntity++) {
			this.printEntityLayout(entities.get(iEntity), printer);
		}
		
		printer.openPARAGRAPH(CSS.CSSButtonLine);
		printer.openCENTER();
		printer.addBUTTON("image", "action", RequestParameterAction.ActionUpdate, "/pictures/button_save.gif");
		printer.text("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		printer.addBUTTON("image", "action", RequestParameterAction.ActionCancel, "/pictures/button_cancel.gif");
		printer.closeCENTER();
		printer.closePARAGRAPH();

		printer.closeFORM();
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void listEntities(GroupEntity entity, Vector<Entity> entities) {
		for (int iChild = 0; iChild < entity.children().size(); iChild++) {
			Entity child = entity.children().get(iChild);
			entities.add(child);
			if (child.isGroup()) {
				this.listEntities((GroupEntity)child, entities);
			}
		}
	}
	
	private void printDisplayLabelSelectBox(Vector<Entity> entities, HtmlLinePrinter printer) {
		int currentDisplayEntityID = -1;
		
		AttributeEntity current = _request.wiki().displayEntity(_request.wiki().database().schema());
		if (current != null) {
			currentDisplayEntityID = current.id();
		}
		
		int count = 0;
		
		if (entities.size() > 0) {
			for (int iEntity = 0; iEntity < entities.size(); iEntity++) {
				Entity entity = entities.get(iEntity);
				if (entity.isAttribute()) {
					if (count > 0) {
						printer.addBR();
					}
					if (entity.id() == currentDisplayEntityID) {
						printer.addRADIOBUTTON(entity.path(), DatabaseLayouter.PropertyDisplayEntity, Integer.toString(entity.id()), true);
					} else {
						printer.addRADIOBUTTON(entity.path(), DatabaseLayouter.PropertyDisplayEntity, Integer.toString(entity.id()), false);
					}
					count++;
				}
			}
		}
	}
	
	private void printDisplayOrderSelectBox(Entity entity, int size, int displayOrder, HtmlLinePrinter printer) {
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display order");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);

		printer.openSELECT(DatabaseLayouter.PropertyEntityDisplayOrder + "_" + entity.id());
		for (int iOrder = 1; iOrder <=size; iOrder++) {
			printer.addOPTION(String.valueOf(iOrder), String.valueOf(iOrder), (iOrder == displayOrder));
		}
		printer.closeSELECT();
		
		printer.closeTD();
		printer.closeTR();
	}

	private void printEntityLayout(Entity entity, HtmlLinePrinter printer) {
		EntityLayout layout = _request.wiki().layouter().get(entity);
	
		printer.paragraph("Entity " + entity.path(), CSS.CSSHeadlineSmall);

		printer.openTABLE(CSS.CSSFormContainer);
		printer.openTR();
		printer.openTD(CSS.CSSFormContainer);

		printer.openTABLE(CSS.CSSFormFrame);
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display name");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertyEntityName + "_" + entity.id(), "90", layout.getName());
		printer.closeTD();
		printer.closeTR();
		
		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display label");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertyEntityLabel + "_" + entity.id(), "90", layout.getLabelDefinition());
		printer.closeTD();
		printer.closeTR();

		printer.openTR();
		printer.openTD(CSS.CSSFormLabel);
		printer.text("Display label (short form)");
		printer.closeTD();
		printer.openTD(CSS.CSSFormControl);
		printer.addTEXTAREA(DatabaseLayouter.PropertyEntityLabelShort + "_" + entity.id(), "90", layout.getLabelShortDefinition());
		printer.closeTD();
		printer.closeTR();

		if (entity.id() != 0) {
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Label alignment");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getLabelAlignment().isLeftAlign()) {
				printer.addRADIOBUTTON("Left", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "left", true);
			} else {
				printer.addRADIOBUTTON("Left", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "left", false);
			}
			printer.addBR();
			if (layout.getLabelAlignment().isTopAlign()) {
				printer.addRADIOBUTTON("Top", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "top", true);
			} else {
				printer.addRADIOBUTTON("Top", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "top", false);
			}
			printer.addBR();
			if (layout.getLabelAlignment().isNoneAlign()) {
				printer.addRADIOBUTTON("None", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "none", true);
			} else {
				printer.addRADIOBUTTON("None", DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "none", false);
			}
			printer.closeTD();
			printer.closeTR();
	
			this.printDisplayOrderSelectBox(entity, _request.wiki().database().schema().size(), layout.getDisplayOrder(), printer);
			
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Display style");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getDisplayStyle().isGroupStyle()) {
				printer.addRADIOBUTTON("Group", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "group", true);
			} else {
				printer.addRADIOBUTTON("Group", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "group", false);
			}
			printer.addBR();
			if (layout.getDisplayStyle().isListStyle()) {
				printer.addRADIOBUTTON("List", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "list", true);
			} else {
				printer.addRADIOBUTTON("List", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "list", false);
			}
			printer.addBR();
			if (layout.getDisplayStyle().isTableStyle()) {
				printer.addRADIOBUTTON("Table", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "table", true);
			} else {
				printer.addRADIOBUTTON("Table", DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "table", false);
			}
			printer.closeTD();
			printer.closeTR();
	
			if (entity.isGroup()) {
				printer.openTR();
				printer.openTD(CSS.CSSFormLabel);
				printer.text("Show content");
				printer.closeTD();
				printer.openTD(CSS.CSSFormControl);
				if (layout.getShowContent()) {
					printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "true", true);
					printer.addBR();
					printer.addRADIOBUTTON("No", DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "false", false);
				} else {
					printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "true", false);
					printer.addBR();
					printer.addRADIOBUTTON("No", DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "false", true);
				}
				printer.closeTD();
				printer.closeTR();
			} else {
				printer.addHIDDEN(DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "false");
			}
			
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Edit with parent");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			if (layout.getEditWithParent()) {
				printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertyEntityEditWithParent + "_" + entity.id(), "true", true);
				printer.addBR();
				printer.addRADIOBUTTON("No", DatabaseLayouter.PropertyEntityEditWithParent + "_" + entity.id(), "false", false);
			} else {
				printer.addRADIOBUTTON("Yes", DatabaseLayouter.PropertyEntityEditWithParent + "_" + entity.id(), "true", false);
				printer.addBR();
				printer.addRADIOBUTTON("No", DatabaseLayouter.PropertyEntityEditWithParent + "_" + entity.id(), "false", true);
			}
			printer.closeTD();
			printer.closeTR();
	
			printer.openTR();
			printer.openTD(CSS.CSSFormLabel);
			printer.text("Style sheet prefix");
			printer.closeTD();
			printer.openTD(CSS.CSSFormControl);
			printer.addTEXTAREA(DatabaseLayouter.PropertyEntityStyleSheetPrefix + "_" + entity.id(), "90", layout.getStyleSheetPrefix());
			printer.closeTD();
			printer.closeTR();

			if (entity.isAttribute()) {
				printer.openTR();
				printer.openTD(CSS.CSSFormLabel);
				printer.text("Text height");
				printer.closeTD();
				printer.openTD(CSS.CSSFormControl);
				printer.addTEXTAREA(DatabaseLayouter.PropertyEntityTextHeight + "_" + entity.id(), "90", Integer.toString(layout.getTextHeight()));
				printer.closeTD();
				printer.closeTR();
			} else {
				printer.addHIDDEN(DatabaseLayouter.PropertyEntityTextHeight + "_" + entity.id(), "0");
			}
		} else {
			printer.addHIDDEN(DatabaseLayouter.PropertyEntityLabelAlign + "_" + entity.id(), "left");
			printer.addHIDDEN(DatabaseLayouter.PropertyEntityDisplayStyle + "_" + entity.id(), "group");
			printer.addHIDDEN(DatabaseLayouter.PropertyEntityShowContent + "_" + entity.id(), "false");
			printer.addHIDDEN(DatabaseLayouter.PropertyEntityEditWithParent + "_" + entity.id(), "false");
			printer.addHIDDEN(DatabaseLayouter.PropertyEntityTextHeight + "_" + entity.id(), "0");
		}
		
		printer.closeTABLE();
		
		printer.closeTD();
		printer.closeTR();
		printer.closeTABLE();

	}
	
	private void printIndexSelectBox(HtmlLinePrinter printer) {
		String selected = _request.wiki().layouter().indexType();
		
		if (selected.equals(DatabaseWiki.IndexAZMultiPage)) {
			printer.addRADIOBUTTON("A-Z Multi-Page", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexAZMultiPage, true);
		} else {
			printer.addRADIOBUTTON("A-Z Multi-Page", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexAZMultiPage, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseWiki.IndexAZSinglePage)) {
			printer.addRADIOBUTTON("A-Z Single-Page", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexAZSinglePage, true);
		} else {
			printer.addRADIOBUTTON("A-Z Single-Page", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexAZSinglePage, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseWiki.IndexFullList)) {
			printer.addRADIOBUTTON("Full Listing", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexFullList, true);
		} else {
			printer.addRADIOBUTTON("Full Listing", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexFullList, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseWiki.IndexMultiColumn)) {
			printer.addRADIOBUTTON("Multi Column", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexMultiColumn, true);
		} else {
			printer.addRADIOBUTTON("Multi Column", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexMultiColumn, false);
		}
		printer.addBR();
		if (selected.equals(DatabaseWiki.IndexPartialList)) {
			printer.addRADIOBUTTON("Partial Listing", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexPartialList, true);
		} else {
			printer.addRADIOBUTTON("Partial Listing", DatabaseLayouter.PropertyIndexType, DatabaseWiki.IndexPartialList, false);
		}
	}
}
