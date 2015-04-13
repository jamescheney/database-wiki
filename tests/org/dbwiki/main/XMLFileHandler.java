package org.dbwiki.main;

import java.util.Stack;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.io.Attribute;
import org.dbwiki.data.io.CopyPasteConstants;
import org.dbwiki.data.io.InputHandler;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.pegdown.ast.TextNode;

public class XMLFileHandler implements InputHandler {

	private Exception exception;
	private Stack<DatabaseNode> readStack;
	private DatabaseNode root;

	@Override
	public void endDocument() throws WikiException {
		if (!readStack.isEmpty()) {
			throw new WikiFatalException("Invalid document format");
		}
	}

	@Override
	public void endElement(String label) throws WikiException {
		if (label.equals(CopyPasteConstants.ElementLabelNode)) {
			readStack.pop();
		}
	}

	@Override
	public void exception(Exception excpt) {
		exception = excpt;

	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public boolean hasException() {
		return exception != null;
	}

	@Override
	public void startDocument() throws WikiException {
		readStack = new Stack<DatabaseNode>();
		root = null;
	}

	@Override
	public void startElement(String label) throws WikiException {
		throw new WikiFatalException("Invalid method call: " + this.getClass().getName() + ".startElement(" + label + ")");
	}

	@Override
	public void startElement(String label, Attribute[] attrs)
			throws WikiException {
		int nodeType = Integer.parseInt(this.getAttribute(attrs, "type").value());
		TimeSequence timestamp = TimeSequence.parseTimeSequence(this.getAttribute(attrs, "timesequence").value(), new VersionIndex());
		switch (nodeType) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
		}

	}

	@Override
	public void text(char[] value) throws WikiException {
		// TODO Auto-generated method stub

	}


	private Attribute getAttribute(Attribute[] attrs, String name) throws org.dbwiki.exception.WikiException {
		for (int iAttr = 0; iAttr < attrs.length; iAttr++) {
			if (attrs[iAttr].name().equals(name)) {
				return attrs[iAttr];
			}
		}
		throw new WikiFatalException("Missing attribute " + name);
	}
}
