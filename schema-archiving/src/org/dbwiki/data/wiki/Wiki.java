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
package org.dbwiki.data.wiki;

import java.util.List;

import org.dbwiki.data.index.DatabaseContent;

import org.dbwiki.data.resource.PageIdentifier;

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.user.User;

/** Interface for HTML wiki objects, providing a collection of entries each of which is a WikiPageDescription
 * FIXME #wiki: Is this abstraction necessary?
 * @author jcheney
 *
 */
public interface Wiki {
	/*
	 * Interface Methods
	 */
	/** Get the content listing of the wiki
	 * 
	 */
	public DatabaseContent content() throws org.dbwiki.exception.WikiException;
	/** 
	 * Delete a wiki page with a given identifier
	 * @param identifier
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void delete(PageIdentifier identifier) throws org.dbwiki.exception.WikiException;
	/** 
	 * Get wiki page associated with a given identifier
	 * @param identifier
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public DatabaseWikiPage get(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException;
	/** Insert a wiki page with a given user as creator
	 * 
	 * @param page
	 * @param user
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void insert(DatabaseWikiPage page, User user) throws org.dbwiki.exception.WikiException;
	/** Update a wiki page id with content and with a given user as creator
	 * 
	 * @param identifier
	 * @param page
	 * @param user
	 * @throws org.dbwiki.exception.WikiException
	 */
	public void update(PageIdentifier identifier, DatabaseWikiPage page, User user) throws org.dbwiki.exception.WikiException;
	/**
	 * List all the versions of a wiki page
	 */
	public List<DatabaseWikiPage> versions(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException;
}
