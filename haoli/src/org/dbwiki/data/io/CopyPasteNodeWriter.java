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

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;

import org.dbwiki.exception.WikiFatalException;

/** A NodeWriter that provides callbacks for exporting data as XML
 * with extra annotations for the copy-paste protocol
 * FIXME #xmlwriter Maybe could be derived from ExportNodeWriter and we could get rid of abstract superclass
 * @author jcheney
 *
 */
public class CopyPasteNodeWriter extends NodeWriter {
	/*
	 * Public Methods
	 */


	public void startDatabase(Database database, int version) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("<" + CopyPasteConstants.ElementLabelDatabase 
					+ " " + CopyPasteConstants.AttributeLabelDatabaseName 
					+ "=\"" + database.name() 
					+ "\" " + CopyPasteConstants.AttributeLabelVersion 
					+ "=\"" + version + "\">");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	
	
	public void endDatabase(Database database) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("</" + CopyPasteConstants.ElementLabelDatabase + ">");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	public void startGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.openGroupNode(node));
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	

	public void endGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.closeNode());
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public void writeAttributeNode(DatabaseAttributeNode node, DatabaseTextNode value) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.openAttributeNode(node));
			this.writeTextNode(value);
			this.writeln(this.closeNode());
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public void writeTextNode(DatabaseTextNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.write(this.openTextNode(node));
			this.write(org.dbwiki.lib.XML.maskText(node.getValue()));
			this.writeln(this.closeNode());
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	
	private String closeNode() {
		return "</" + CopyPasteConstants.ElementLabelNode + ">";
	}
	
	private String openAttributeNode(DatabaseAttributeNode node) {
		return this.openElementNode(node, CopyPasteConstants.NodeTypeAttribute);
	}
	
	private String openElementNode(DatabaseElementNode node, int type) {
		return "<" + CopyPasteConstants.ElementLabelNode 
			+ " " + CopyPasteConstants.AttributeLabelType 
			+ "=\"" + Integer.toString(type) 
			+ "\" " + CopyPasteConstants.AttributeLabelSchemaNodeName 
			+ "=\"" + node.label() 
			+ "\" " + CopyPasteConstants.AttributeLabelID 
			+ "=\"" + node.identifier().toURLString() + "\">"; 
	}
	
	private String openGroupNode(DatabaseGroupNode node) {
		return this.openElementNode(node, CopyPasteConstants.NodeTypeGroup);
	}
	
	private String openTextNode(DatabaseTextNode node) {
		return "<" + CopyPasteConstants.ElementLabelNode 
				+ " " + CopyPasteConstants.AttributeLabelType 
				+ "=\"" + Integer.toString(CopyPasteConstants.NodeTypeText) 
				+ "\" " + CopyPasteConstants.AttributeLabelID 
				+ "=\"" + node.identifier().toURLString() + "\">"; 
	}
}
