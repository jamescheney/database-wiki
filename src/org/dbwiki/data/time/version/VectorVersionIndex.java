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
package org.dbwiki.data.time.version;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiTimeException;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.data.provenance.Provenance;
import org.dbwiki.data.time.TimeInterval;
import org.dbwiki.data.time.Timestamp;
import org.dbwiki.data.time.Version;
import org.dbwiki.data.time.VersionIndex;

public class VectorVersionIndex implements VersionIndex {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, Version> _versionNameIndex;
	private Vector<Version> _versionList;

	
	/*
	 * Constructors
	 */
	
	public VectorVersionIndex()  {
		_versionList = new Vector<Version>();
		_versionNameIndex = new Hashtable<String, Version>();
	}

	
	/*
	 * Public Methods
	 */
	
	public synchronized void add(Version version) throws org.dbwiki.exception.WikiException {
		if (_versionNameIndex.containsKey(version.name())) {
			throw new WikiTimeException(WikiTimeException.DuplicateVersion, "Version " + version.name() + " already exists");
		}
		if (version.number() != (_versionList.size() + 1)) {
			throw new WikiFatalException("Invalid version number " + version.number() + ". Expected number is " + (_versionList.size() + 1));
		}
		
		_versionList.add(version);
		_versionNameIndex.put(version.name(), version);
	}
	
	public synchronized Version get(int index) {
		return _versionList.get(index);
	}

	public synchronized Version getByName(String name) {
		String key = name.toUpperCase();
		
		if (!_versionNameIndex.containsKey(key)) {
			return null;
		}
		return _versionNameIndex.get(name);
	}

	public synchronized Version getByNumber(int number) throws org.dbwiki.exception.WikiException {
		for (int iVersion = 0; iVersion < this.size(); iVersion++) {
			Version version = this.get(iVersion);
			if (version.number() == number) {
				return version;
			}
		}
		throw new WikiTimeException(WikiTimeException.UnknownVersionNumber, String.valueOf(number));
	}

	public synchronized Version getLastVersion() {
		if (!this.isEmpty()) {
			return _versionList.lastElement();
		} else {
			return null;
		}
	}
	
	public synchronized Version getNextVersion(Provenance provenance) {
		Date date = new Date();

		return new VersionImpl(this.size() + 1, new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(date), date.getTime(), provenance, this);
	}
	
	public synchronized boolean isEmpty() {
		return (_versionList.size() == 0);
	}

	public synchronized int size() {
		return _versionList.size();
	}


	public synchronized String toString(Timestamp timestamp) throws org.dbwiki.exception.WikiException {
		TimeInterval[] intervals = timestamp.intervals();
		String text = this.getTextString(intervals[0]);
		for (int iInterval = 1; iInterval < intervals.length; iInterval++) {
			text = text + ", " + this.getTextString(intervals[iInterval]);
		}
		return text;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private String getTextString(TimeInterval interval) throws org.dbwiki.exception.WikiException {
		String text = this.getByNumber(interval.start()).name();
		if (interval.isOpen()) {
			return text + "-now";
		} else if (interval.start() != interval.end()) {
			return text + "-" + this.getByNumber(interval.end()).name();
		} else {
			return text;
		}
	}
	
	
	
	private void addVersion(int time, Hashtable<Integer, Version> versions) throws org.dbwiki.exception.WikiException {
		Integer key = new Integer(time);
		if (!versions.containsKey(key)) {
			versions.put(key, getByNumber(time));
		}
	}
	
	private void addModificationPoints(Timestamp timestamp, Hashtable<Integer, Version> versions) throws org.dbwiki.exception.WikiException {
		TimeInterval[] intervals = timestamp.intervals();
		for (int iInterval = 0; iInterval < intervals.length; iInterval++) {
			TimeInterval interval = intervals[iInterval];
			this.addVersion(interval.start(), versions);
			if (!interval.isOpen()) {
				this.addVersion(interval.end() + 1, versions);
			}
		}
	}
	
	private void addNodeChanges(DatabaseElementNode element, Hashtable<Integer, Version> versions) throws org.dbwiki.exception.WikiException {
		if (element.isAttribute()) {
			DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
			for (int iValue = 0; iValue < attribute.value().size(); iValue++) {
				DatabaseTextNode value = attribute.value().get(iValue);
				if (value.hasTimestamp()) {
					this.addModificationPoints(value.getTimestamp(), versions);
				}
			}
		} else {
			DatabaseGroupNode group = (DatabaseGroupNode)element;
			for (int iNode = 0; iNode < group.children().size(); iNode++) {
				DatabaseElementNode node = (DatabaseElementNode)group.children().get(iNode);
				if (node.hasTimestamp()) {
					this.addModificationPoints(node.getTimestamp(),  versions);
				}
				this.addNodeChanges(node,  versions);
			}
		}
	}
	
	public Vector<Version> getNodeChanges(DatabaseNode node) throws org.dbwiki.exception.WikiException {
		Hashtable<Integer, Version> versions = new Hashtable<Integer, Version>();
		
		addModificationPoints(node.getTimestamp(), versions);
		
		if (node.isElement()) {
			addNodeChanges((DatabaseElementNode)node, versions);
		}
		
		Vector<Version> result = new Vector<Version>();
		
		Iterator<Version> elements = versions.values().iterator();
		while (elements.hasNext()) {
			Version version = elements.next();
			boolean added = false;
			if (result.size() > 0) {
				for (int iVersion = 0; iVersion < result.size(); iVersion++) {
					if (version.number() < result.get(iVersion).number()) {
						result.add(iVersion, version);
						added = true;
						break;
					}
				}
			}
			if (!added) {
				result.add(version);
			}
		}
		return result;
	}
	
}
