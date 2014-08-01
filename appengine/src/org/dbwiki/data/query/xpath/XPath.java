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
package org.dbwiki.data.query.xpath;

/** XPath expression for XAQL queries and XPath queries.
*
* @author hmueller
*
*/
import java.util.Vector;

import org.dbwiki.data.query.condition.AttributeConditionListing;

public class XPath {

	/*
	 * Private Variables
	 */
	
	private Vector<XPathComponent> _elements;
	// FIXME: is this the depth of the start of the path? why is this better than just taking subvectors?
	private int _start; 
	
	/*
	 * Constructors
	 */
	
	public XPath() {
		
		_elements = new Vector<XPathComponent>();
		_start = 0;
	}
	
	public XPath(Vector<XPathComponent> elements, int start) {
		
		_elements = elements;
		_start = start;
	}
	
	public XPath(XPathComponent prefix) {
		
		this();

		this.add(prefix);
	}

	public XPath(XPathComponent prefix, XPath suffix) {
		
		this(prefix);

		for (int iElement = 0; iElement < suffix.size(); iElement++) {
			this.add(suffix.get(iElement));
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(XPathComponent element) {
		
		_elements.add(element);
	}
	
	public XPathComponent firstElement() {
		
		return _elements.get(_start);
	}
	
	public XPathComponent get(int index) {
		
		return _elements.get(_start + index);
	}
	
	public AttributeConditionListing getConditionListing() {
		
		AttributeConditionListing listing = new AttributeConditionListing();
		this.listConditions(listing);
		return listing;
	}
	
	public boolean hasSubPathConditons() {
		
		for (int iElement = 0; iElement < this.size(); iElement++) {
			XPathComponent element = this.get(iElement);
			if (element.hasCondition()) {
				if (element.condition().isSubPathCondition()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public XPathComponent lastElement() {
		
		return _elements.lastElement();
	}
	
	public void listConditions(AttributeConditionListing listing) {
	
		for (int iElement = 0; iElement < this.size(); iElement++) {
			XPathComponent element = this.get(iElement);
			if (element.hasCondition()) {
				if (element.condition().isSubPathCondition()) {
					((SubPathCondition)element.condition()).listConditions(listing);
				}
			}
		}
	}
	
	public int size() {
		
		return (_elements.size() - _start);
	}
	
	public XPath subpath(int start) {
		
		return new XPath(_elements, (_start + start));
	}
	
	public String toString() {
		
		String line = "";
		for (int iElement = 0; iElement < this.size(); iElement++) {
			line = line + "/" + this.get(iElement).toString();
		}
		return line;
	}
}
