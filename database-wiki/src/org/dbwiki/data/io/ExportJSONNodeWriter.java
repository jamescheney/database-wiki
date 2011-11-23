package org.dbwiki.data.io;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.exception.WikiFatalException;

// TODO #json: Change to generate valid json.
// Avoid trailing commas
// Generate object brackets around group nodes

public class ExportJSONNodeWriter extends NodeWriter {
 
	public void writeInit() throws java.io.IOException {
		
	}
	
	public void startDatabase(Database database, int version) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("{\"name\":" + "\"" + database.name()+ "\"" + ",  \"version\" : \"" + version + "\",\"entries\" : [");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	public void endDatabase(Database database) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("]}");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	public void startGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("\"" + node.label() + "\" : {");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}
	
	public void endGroupNode(DatabaseGroupNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.writeln("}");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public void writeAttributeNode(DatabaseAttributeNode node, DatabaseTextNode value) throws org.dbwiki.exception.WikiException {
		try {
			this.write("\"" + node.label() + "\" : ");
			this.write("\"" + value.getValue() + "\",");
			
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

	public void writeTextNode(DatabaseTextNode node) throws org.dbwiki.exception.WikiException {
		try {
			this.write("\"" + node.getValue() + "\"");
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
		}
	}

}
