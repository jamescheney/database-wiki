package org.dbwiki.data.io;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;



public class ExportJSONNodeWriter extends NodeWriter {
 
	@Override
	public void writeInit() throws java.io.IOException {
		
	}
	
	@Override
	public void startDatabase(Database database, int version) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("{\"name\":" + "\"" + database.name()+ "\"" + ",  \"version\" : \"" + version + "\",\"entries\" : [");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	@Override
	public void endDatabase(Database database) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("]}");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	@Override
	public void startGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("\"" + node.label() + "\" : {");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	@Override
	public void endGroupNode(DatabaseGroupNode node, boolean isLast) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("}");
			if (!isLast) {
				this.write(",");
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	@Override
	public void writeAttributeNode(DatabaseAttributeNode node, DatabaseTextNode value, boolean isLast) throws org.dbwiki.exception.WikiException {
		try {
			this.write("\"" + node.label() + "\" : ");
			this.write("\"" + value.getValue() + "\"");
			if (!isLast) {
				this.write(",");
			}
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	@Override
	public void writeTextNode(DatabaseTextNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.write("\"" + node.getValue() + "\"");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	@Override
	public void startEntry() throws WikiException {
		try {
			this.write("{");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	@Override
	public void endEntry() throws WikiException {
		try {
			this.write("}");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
}
