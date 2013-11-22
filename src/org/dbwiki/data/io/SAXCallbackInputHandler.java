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

import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/** 
 * Takes an InputHandler and uses its callbacks instead of the default SAX handler
 * @author jcheney
 *
 */
public class SAXCallbackInputHandler extends DefaultHandler {
	/*
	 * Private Constants
	 */
	
	private static final String elementValue = "value";
	
	
	/*
	 * Private Variables
	 */
	
	private char[] _buffer = null;
	private boolean _hadAttributes;
	private boolean _nestTextNodes;
	private InputHandler _nodeHandler;
	
	
	/*
	 * Constructors
	 */
	
	public SAXCallbackInputHandler(InputHandler nodeHandler, boolean nestTextNodes) {
		_nodeHandler = nodeHandler;
		_nestTextNodes = nestTextNodes;
	}

	
	/*
	 * Public Methods
	 */
	
    @Override
	public void endDocument() throws SAXException {
    	try {
	    	this.checkPendingTextNode();
	    	_nodeHandler.endDocument();
    	} catch (org.dbwiki.exception.WikiException wikiExcption) {
    		throw new SAXException(wikiExcption);
    	}
    }

    @Override
	public void endElement(String namespaceURI, String simpleName, String qualifiedName) throws SAXException {
		String elementName = null;
        if (simpleName.equals("")) {
        	elementName = qualifiedName;
        } else {
        	elementName = simpleName;
        }

        try {
	    	this.checkPendingTextNode();
	    	_nodeHandler.endElement(elementName);
    	} catch (org.dbwiki.exception.WikiException wikiException) {
    		wikiException.printStackTrace();
    		throw new SAXException(wikiException);
    	}

    	_hadAttributes = false;
    	
    }

    @Override
	public void error(SAXParseException excpt) throws SAXException {
    	throw excpt;
    }

    public void exception(Exception excpt) {
    	_nodeHandler.exception(excpt);
    }
    
    @Override
	public void fatalError(SAXParseException excpt) throws SAXException {
    	throw excpt;
    }

    @Override
	public void startDocument() throws SAXException {
    	try {
    		_nodeHandler.startDocument(); 
    	} catch (org.dbwiki.exception.WikiException wikiExcption) {
    		throw new SAXException(wikiExcption);
    	}
    }

    @Override
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attrs) throws SAXException {
		String elementName = null;
        if (localName.equals("")) {
        	elementName = qualifiedName;
        } else {
        	elementName = localName;
        }

        _hadAttributes = (attrs.getLength() > 0);
        
        try {
			this.checkPendingTextNode();
			if (attrs.getLength() > 0) {
				Attribute[] attributes = new Attribute[attrs.getLength()];
	    		for (int iAttr = 0; iAttr < attrs.getLength(); iAttr++) {
		    		String attrName = attrs.getLocalName(iAttr);
		    		if (attrName.equals("")) {
		    			attrName = attrs.getQName(iAttr);
		    		}
		    		attributes[iAttr] = new Attribute(attrName, attrs.getValue(iAttr));
	    		}
	    		_nodeHandler.startElement(elementName, attributes);
			} else {
				_nodeHandler.startElement(elementName);
			}
    	} catch (org.dbwiki.exception.WikiException wikiExcption) {
    		wikiExcption.printStackTrace();
    		throw new SAXException(wikiExcption);
    	}
    }

    @Override
	public void characters(char buf[], int offset, int len) throws SAXException {
		if (_buffer == null) {
			_buffer = new char[len];
			for (int iChar = 0; iChar < len; iChar++) {
				_buffer[iChar] = buf[offset + iChar];
			}
		} else {
			char[] newBuffer = new char[_buffer.length + len];
			for (int iChar = 0; iChar < _buffer.length; iChar++) {
				newBuffer[iChar] = _buffer[iChar];
			}
			for (int iChar = 0; iChar < len; iChar++) {
				newBuffer[_buffer.length + iChar] = buf[offset + iChar];
			}
			_buffer = newBuffer;
		}
    }
    
    @Override
	public void warning(SAXParseException excpt) throws SAXException {
    	throw excpt;
    }

    
    /*
     * Private Methods
     */
    
    protected void checkPendingTextNode() throws org.dbwiki.exception.WikiException {
    	if (_buffer != null) {
    		boolean isEmpty = new String(_buffer).trim().equals("");
    		if ((_hadAttributes) && (!isEmpty) && (_nestTextNodes)) {
				_nodeHandler.startElement(elementValue);
    		}
    		_nodeHandler.text(_buffer);
    		if ((_hadAttributes) && (!isEmpty) && (_nestTextNodes)) {
    			_nodeHandler.endElement(elementValue);
    		}
     		_buffer = null;
    	}
    }
    
    
    public  void parse(InputStream inputStream, boolean validating, boolean xmlSchema) throws java.io.IOException, org.xml.sax.SAXException {
		if (inputStream != null) {
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	        parser.setFeature("http://xml.org/sax/features/validation", validating);
	        parser.setFeature("http://apache.org/xml/features/validation/schema", xmlSchema);
	        parser.setContentHandler(this);
	        parser.setErrorHandler(this);
	        parser.parse(new InputSource(inputStream));
			inputStream.close();
		} else {
			this.startDocument();
			this.endDocument();
		}
	}
}
