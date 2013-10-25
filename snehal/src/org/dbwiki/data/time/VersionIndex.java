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
package org.dbwiki.data.time;

import org.dbwiki.data.provenance.Provenance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;
import org.dbwiki.data.database.DatabaseTextNode;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiTimeException;

/** A a version index, listing the versions of an object 
 * @author jcheney
 *
 */
public class VersionIndex {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, Version> _versionNameIndex;
	private Vector<Version> _versionList;

	
	/*
	 * Constructors
	 */
	
	public VersionIndex()  {
		_versionList = new Vector<Version>();
		_versionNameIndex = new Hashtable<String, Version>();
	}

	
	/*
	 * Public Methods
	 */
	/** Adds a new version
	 * Check that there are no duplicates and that the next version being added is the next in the sequence
	 */
	public synchronized void add(Version version) throws org.dbwiki.exception.WikiException {
		
		if (_versionNameIndex.containsKey(version.name())) {
			throw new WikiTimeException(WikiTimeException.DuplicateVersion, "Version " + version.name() + " already exists");
		}
	/* chk with james
		if (version.number() != (_versionList.size() + 1)) {
			throw new WikiFatalException("Invalid version number " + version.number() + ". Expected number is " + (_versionList.size() + 1));
		}
		*/
		_versionList.add(version);
		_versionNameIndex.put(version.name(), version);
	}
	
	/** Get the version at position index in the index
	 * 
	 */
	public synchronized Version get(int index) {
		return _versionList.get(index);
	}

	/** Get a version by its string name
	 * 
	 * @param name
	 * @return
	 */
	public synchronized Version getByName(String name) {
		String key = name.toUpperCase();
		
		if (!_versionNameIndex.containsKey(key)) {
			return null;
		}
		return _versionNameIndex.get(name);
	}

	/** Get version by its version number, i.e. the id stored in the database
	 * 
	 */
	public synchronized Version getByNumber(int number) throws org.dbwiki.exception.WikiException {
		for (int iVersion = 0; iVersion < this.size(); iVersion++) {
			Version version = this.get(iVersion);
			if (version.number() == number) {
				return version;
			}
		}
		throw new WikiTimeException(WikiTimeException.UnknownVersionNumber, String.valueOf(number));
	}

	/** Get the last version in the index, which is the last in the vector
	 * 
	 */
	public synchronized Version getLastVersion() {
		if (!this.isEmpty()) {
			return _versionList.lastElement();
		} else {
			return null;
		}
	}
	
	/** Gets a new version based on the current date/time, whose number is 1+ size of the index
	 * Uses the given provenance record, and uses this as the version index
	 * Not clear that this is safe to "add" if version numbers and positions ever get out of sync
	 */
	public synchronized Version getNextVersion(Provenance provenance) {
		Date date = new Date();
		String name = new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(date);
		int number = size() + 1;
		
		// FIXME #time: the need for the following hack is symptomatic of
		// fundamental flaws in the design of this class.
		//
		// HACK:
		// make sure we don't get any name clashes through
		// generating multiple versions in the same second!
		if(_versionNameIndex.containsKey(name)) {
			name = name + "(" + number + ")";
		}

		return new Version(size() + 1,
				name,
				date.getTime(), provenance, this);
	}
	
	/** Is version index empty?
	 * 
	 * @return
	 */
	public synchronized boolean isEmpty() {
		return (_versionList.size() == 0);
	}

	/** Size of the version index
	 * 
	 */
	public synchronized int size() {
		return _versionList.size();
	}

	
	
	/*
	 * Private Methods
	 */

	
	/** Adds a version to a version hash table
	 * 
	 */
	private void addVersion(int time, Hashtable<Integer, Version> versions) throws org.dbwiki.exception.WikiException {
		Integer key = new Integer(time);
		if (!versions.containsKey(key)) {
			versions.put(key, getByNumber(time));
		}
	}
	
	/** Adds modification points to version hash table.
	 * Adds each start and end point to the table except for the end point if the last interval is open.
	 * @param timestamp
	 * @param versions
	 * @throws org.dbwiki.exception.WikiException
	 */
	private void addModificationPoints(TimeSequence timestamp, Hashtable<Integer, Version> versions) throws org.dbwiki.exception.WikiException {
		TimeInterval[] intervals = timestamp.intervals();
		for (int iInterval = 0; iInterval < intervals.length; iInterval++) {
			TimeInterval interval = intervals[iInterval];
			this.addVersion(interval.start(), versions);
			if (!interval.isOpen()) {
				this.addVersion(interval.end() + 1, versions);
			}
		}
	}
	
	/** Adds changes relevant to node or its descendants
	 * FIXME: This might be better handled in DatabaseElementNode
	 * @param element
	 * @param versions
	 * @throws org.dbwiki.exception.WikiException
	 */
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
				DatabaseElementNode node = group.children().get(iNode);
				if (node.hasTimestamp()) {
					this.addModificationPoints(node.getTimestamp(),  versions);
				}
				this.addNodeChanges(node,  versions);
			}
		}
	}
	
	/** Get the list of versions where something changed affecting node
	 * FIXME #time: Hard to understand, try to simplify.  Seems to side-effect the version index!
	 */
	public Vector<Version> getNodeChanges(DatabaseNode node) throws org.dbwiki.exception.WikiException {
		Hashtable<Integer, Version> versions = new Hashtable<Integer, Version>();
		
		addModificationPoints(node.getTimestamp(), versions);
		
		if (node.isElement()) {
			addNodeChanges((DatabaseElementNode)node, versions);
		}
		
		Vector<Version> result = new Vector<Version>();
		
		// Iterate over all versions.
		Iterator<Version> elements = versions.values().iterator();
		while (elements.hasNext()) { 
			Version version = elements.next(); // For each version... 
			boolean added = false;  
			if (result.size() > 0) { // if some versions have been added
				for (int iVersion = 0; iVersion < result.size(); iVersion++) {
					// add this version in place of the first modification point whose number is greater.
					if (version.number() < result.get(iVersion).number()) {
						// add this version in position of iVersion
						result.add(iVersion, version);
						added = true;
						break;
					}
				}
			}
			// If no versions have been added then add this version.
			if (!added) {
				result.add(version);
			}
		}
		return result;
	}
}
