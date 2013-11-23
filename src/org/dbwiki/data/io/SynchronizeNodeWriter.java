package org.dbwiki.data.io;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.driver.rdbms.RDBMSDatabaseTextNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;

public class SynchronizeNodeWriter extends NodeWriter {
	
	/*
	 * Public Methods
	 */
	@Override
	public void writeInit() throws java.io.IOException {
		this.writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}

	@Override
	public void startDatabase(Database database, int version) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("<" + SynchronizeConstants.ElementLabelDatabase 
					+ " " + SynchronizeConstants.AttributeLabelDatabaseName 
					+ "=\"" + database.name() 
					+ "\" " + SynchronizeConstants.AttributeLabelVersion 
					+ "=\"" + version + "\">");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	
	
	@Override
	public void endDatabase(Database database) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("</" + SynchronizeConstants.ElementLabelDatabase + ">");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	@Override
	public void startGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.openGroupNode(node));
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	

	@Override
	public void endGroupNode(DatabaseGroupNode node, boolean isLast) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.closeNode());
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	@Override
	public void writeAttributeNode(DatabaseAttributeNode node, DatabaseTextNode value, boolean isLast) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln(this.openAttributeNode(node));
			this.writeTextNode(value);
			this.writeln(this.closeNode());
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	@Override
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
		return "</" + SynchronizeConstants.ElementLabelNode + ">";
	}
	
	private String openAttributeNode(DatabaseAttributeNode node) {
		return this.openElementNode(node, SynchronizeConstants.NodeTypeAttribute);
	}
	
	private String openElementNode(DatabaseElementNode node, int type) {
		return "<" + SynchronizeConstants.ElementLabelNode 
			+ " " + SynchronizeConstants.AttributeLabelType 
			+ "=\"" + Integer.toString(type) 
			+ "\" " + SynchronizeConstants.AttributeLabelSchemaNodeName 
			+ "=\"" + node.label() 
			+ "\" " + SynchronizeConstants.AttributeLabelSchemaNodeId
			+ "=\"" + node.schema().id()
			+ "\" " + SynchronizeConstants.AttributeLabelID 
			+ "=\"" + node.identifier().nodeID()
			+ "\" " + SynchronizeConstants.AttributeLabelPre 
			+ "=\"" + node.getpre()
			+ "\" " + SynchronizeConstants.AttributeLabelPost
			+ "=\"" + node.getpost()
			+ "\" " + SynchronizeConstants.AttributeLabelParent
			+ "=\"" + node.getparent()
			+ "\" " + SynchronizeConstants.AttributeLabelTimeSequence
			+ "=\"" + node.getTimestamp().toIntString()+ "\">"; 
	}
	
	private String openGroupNode(DatabaseGroupNode node) {
		return this.openElementNode(node, SynchronizeConstants.NodeTypeGroup);
	}
	
	private String openTextNode(DatabaseTextNode node) {
		return "<" + SynchronizeConstants.ElementLabelNode 
				+ " " + SynchronizeConstants.AttributeLabelType 
				+ "=\"" + Integer.toString(SynchronizeConstants.NodeTypeText) 
				+ "\" " + SynchronizeConstants.AttributeLabelID 
				+ "=\"" + ((RDBMSDatabaseTextNode)node).identifier().nodeID()
				+ "\" " + SynchronizeConstants.AttributeLabelPre 
				+ "=\"" + node.getpre()
				+ "\" " + SynchronizeConstants.AttributeLabelPost
				+ "=\"" + node.getpost()
				+ "\" " + SynchronizeConstants.AttributeLabelParent
				+ "=\"" + node.getparent()
				+ "\" " + SynchronizeConstants.AttributeLabelTimeSequence
				+ "=\"" + node.getTimestamp().toIntString()+ "\">"; 
	}

	@Override
	public void startEntry() throws WikiException {
		;
		
	}

	@Override
	public void endEntry() throws WikiException {
		;
		
	}

}
