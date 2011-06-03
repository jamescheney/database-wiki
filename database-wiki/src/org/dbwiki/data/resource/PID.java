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
package org.dbwiki.data.resource;

import java.util.Vector;


import org.dbwiki.web.request.RequestURL;

/** PIDs are vectors of label:index steps
 * Such a path uniquely addresses a node in the tree, but 
 * not stably over time.
 * @author jcheney
 *
 */
@Deprecated
public class PID implements ResourceIdentifier {
	/*
	 * Public Constants
	 */
	
	public static final String NodeIdentifierKeyDelimiter  = ":";
	
	
	/*
	 * Private Variables
	 */
	
	private Vector<LID> _path;
	
	
	/*
	 * Constructors
	 */
	
	public PID() {
		_path = new Vector<LID>();
	}
	
	public PID(String rootPath) throws org.dbwiki.exception.WikiException {
		_path = new Vector<LID>();

		String path = rootPath;
		int pos;
		
		while ((pos = path.indexOf('/')) != -1) {
			String component = path.substring(0, pos);
			if (!component.equals("")) {
				_path.add(new LID(component));
			}
			path = path.substring(pos + 1);
		}
		if (!path.equals("")) {
			_path.add(new LID(path));
		}
	}
	
	public PID(String label, int id) {
		_path = new Vector<LID>();
		_path.add(new LID(label, id));
	}
	
	public PID(RequestURL url) throws org.dbwiki.exception.WikiException {
		_path = new Vector<LID>();
		for (int iComponent = 0; iComponent < url.size(); iComponent++) {
			_path.add(new LID(url.get(iComponent).decodedText()));
		}
	}
	
	public PID(Vector<LID> path) {
		_path = path;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public boolean equals(ResourceIdentifier identifier) {
		PID pid = (PID)identifier;
		
		if (this.size() == pid.size()) {
			for (int iLID = 0; iLID < this.size(); iLID++) {
				if (!this.get(iLID).equals(pid.get(iLID))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public LID get(int index) {
		return _path.get(index);
	}
	
	public PID subpath(int startIndex) {
		return this.subpath(startIndex, this.size());
	}

	public PID subpath(int startIndex, int endIndex) {
		Vector<LID> path = new Vector<LID>();
		for (int iComponent = startIndex; iComponent < endIndex; iComponent++) {
			path.add(this.get(iComponent));
		}
		return new PID(path);
	}
	
	public boolean isRootIdentifier() {
		return (_path.size() == 0);
	}
	
	public int size() {
		return _path.size();
	}
	
	public String toParameterString() {
		return this.toURLString();
	}
	
	public String toURLString() {
		String path = "";
		
		for (int iNI = 0; iNI < _path.size(); iNI++) {
			path = path + "/" + _path.get(iNI).toURLString();
		}
		return path;
	}
}
