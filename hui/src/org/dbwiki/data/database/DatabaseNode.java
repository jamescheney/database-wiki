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
package org.dbwiki.data.database;

import org.dbwiki.data.annotation.AnnotationList;
import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.time.TimeSequence;
import org.dbwiki.data.time.TimestampedObject;


/** DatabaseNode root class.
 * Provides generic functionality to find a database node.  
 * @author jcheney
 *
 */
public abstract class DatabaseNode extends TimestampedObject {
	/*
	 * Private Variables
	 */
	
	private AnnotationList _annotation;
	private DatabaseElementNode _parent;

	private int _pre;
	private int _post;
		

	
	/*
	 * Constructors
	 */
	
	public DatabaseNode(DatabaseElementNode parent, TimeSequence timestamp, AnnotationList annotation, int pre, int post) {
		super(parent, timestamp);
		
		_annotation = annotation;
		_parent = parent;
		_pre= pre;
		_post= post;

	}
	
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean isElement();	
	
	/*
	 * Public Methods
	 */
	
	public AnnotationList annotation() {
		return _annotation;
	}
	
	public boolean hasAnnotation() {
		return (_annotation.size() > 0);
	}
	
	public DatabaseElementNode parent() {
		return _parent;
	}
	
	public boolean isText() {
		return !this.isElement();
	}
    
	public int getpre() {
		
		return _pre;
	}
	

	public int getpost() {
		
		return _post;
	}

	
	/*
	 * Finding nodes
	 * 
	 */
	public abstract DatabaseNode find(ResourceIdentifier identifier) ;
	
	
}
