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

import java.net.URLEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.data.index.VectorDatabaseListing;

import org.dbwiki.data.resource.PageIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;

import org.dbwiki.data.wiki.DatabaseWikiPage;

import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.DatabaseConstants;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.exception.data.WikiDataException;

import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;

/** 
 * A simple wiki class that provides basic functionality implemented on top of an rdbms.
 * @author jcheney
 *
 */
public class SimpleWiki implements Wiki {
	/*
	 * Private Variables
	 */
	
	private DatabaseConnector _connector;
	private String _relName;
	private UserListing _users;
	
	/*
	 * Constructors
	 */
	public SimpleWiki(String relName, DatabaseConnector connector, UserListing users) {
		_connector = connector;
		_relName = relName;
		_users = users;
	}
	
	/*
	 * Public Methods
	 */
	/** Get the content listing of the wiki
	 * 
	 */
	public synchronized DatabaseContent content() throws org.dbwiki.exception.WikiException {
		try {
			VectorDatabaseListing content = new VectorDatabaseListing();
			Connection con = _connector.getConnection();
			PreparedStatement pStmtSelectPages = con.prepareStatement(
					"SELECT DISTINCT " + DatabaseConstants.RelPagesColName + " " +
					"FROM " +  _relName + DatabaseConstants.RelationPages + " " +
					"ORDER BY " + DatabaseConstants.RelPagesColName);
			ResultSet rs = pStmtSelectPages.executeQuery();
			while (rs.next()) {
				String title = rs.getString(DatabaseConstants.RelPagesColName);
				content.add(new WikiPageDescription(title, new PageIdentifier(URLEncoder.encode(title, "UTF-8"))));
			}
			rs.close();
			pStmtSelectPages.close();
			con.close();
			return content;
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		} catch (java.io.UnsupportedEncodingException exception) {
			throw new WikiFatalException(exception);
		}
	}
	
	/** 
	 * Delete a wiki page with a given identifier
	 * @param identifier
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized void delete(PageIdentifier identifier) throws org.dbwiki.exception.WikiException {
		try {
			Connection con = _connector.getConnection();
			PreparedStatement pStmtDeletePage = con.prepareStatement(
					"DELETE FROM " + _relName + DatabaseConstants.RelationPages + " " +
					"WHERE " + DatabaseConstants.RelPagesColName + " = ?");
			pStmtDeletePage.setString(1, identifier.toQueryString());
			pStmtDeletePage.execute();
			pStmtDeletePage.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	/** 
	 * Get wiki page associated with a given identifier
	 * @param identifier
	 * @return
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized DatabaseWikiPage get(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException {
		DatabaseWikiPage page = null;
		
		PageIdentifier p = (PageIdentifier)identifier;
		
		// FIXME #wikipages: do something sensible for the case where
		// identifier represents the root of the wiki pages
		//
		// Perhaps we should simply return null if the page
		// isn't present?
				
		String name = p.toQueryString();
		long timestamp = p.getTimestamp();
		
		try {
			Connection con = _connector.getConnection();
			
			PreparedStatement pStmtSelectPage = null;
			if(timestamp == -1) {
				pStmtSelectPage = con.prepareStatement(
						"SELECT " +
						DatabaseConstants.RelPagesColID + ", " +
						DatabaseConstants.RelPagesColContent + ", " +
						DatabaseConstants.RelPagesColTimestamp + ", " +
						DatabaseConstants.RelPagesColUser + " " +
						"FROM " + _relName + DatabaseConstants.RelationPages + " " +
						"WHERE " + DatabaseConstants.RelPagesColName + " = ? " +
						"ORDER BY " + DatabaseConstants.RelPagesColTimestamp + " DESC");
			} else {
				pStmtSelectPage = con.prepareStatement(
						"SELECT " +
						DatabaseConstants.RelPagesColID + ", " +
						DatabaseConstants.RelPagesColContent + ", " +
						DatabaseConstants.RelPagesColTimestamp + ", " +
						DatabaseConstants.RelPagesColUser + " " +
						"FROM " + _relName + DatabaseConstants.RelationPages + " " +
						"WHERE " + DatabaseConstants.RelPagesColName + " = ?" +
						"AND " + DatabaseConstants.RelPagesColTimestamp + " = ?");
				pStmtSelectPage.setLong(2, timestamp);
			}
		
			pStmtSelectPage.setString(1, name);
			
			ResultSet rs = pStmtSelectPage.executeQuery();
			
			if (rs.next()) {
				page =
					new DatabaseWikiPage(
							rs.getInt(DatabaseConstants.RelPagesColID),
							name,
							rs.getString(DatabaseConstants.RelPagesColContent),
							rs.getLong(DatabaseConstants.RelPagesColTimestamp),
							_users.get(rs.getInt(DatabaseConstants.RelPagesColUser)));
			}

			rs.close();
			pStmtSelectPage.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
		if (page != null) {
			return page;
		} else {
			throw new WikiDataException(WikiDataException.UnknownResource, identifier.toParameterString());
		}
	}
	
	/**
	 * List all the versions of a wiki page
	 */
	public synchronized List<DatabaseWikiPage> versions(ResourceIdentifier identifier) throws org.dbwiki.exception.WikiException {
		List<DatabaseWikiPage> versions = new ArrayList<DatabaseWikiPage>();
		
		try {
			Connection con = _connector.getConnection();
			PreparedStatement pStmtSelectPage = con.prepareStatement(
					"SELECT " +
					DatabaseConstants.RelPagesColID + ", " +
					DatabaseConstants.RelPagesColTimestamp + ", " +
					DatabaseConstants.RelPagesColUser + " " +
					"FROM " + _relName + DatabaseConstants.RelationPages + " " +
					"WHERE " + DatabaseConstants.RelPagesColName + " = ? " +
					"ORDER BY " + DatabaseConstants.RelPagesColTimestamp + " DESC");
			
			String name = ((PageIdentifier)identifier).toQueryString();
			
			pStmtSelectPage.setString(1, name);
			ResultSet rs = pStmtSelectPage.executeQuery();
			
			while (rs.next()) {
				versions.add(
						new DatabaseWikiPage(rs.getInt(DatabaseConstants.RelPagesColID),
										  	 name, null,
										  	 rs.getLong(DatabaseConstants.RelPagesColTimestamp),
										  	 _users.get(rs.getInt(DatabaseConstants.RelPagesColUser))));
			}
			
			rs.close();
			pStmtSelectPage.close();
			con.close();
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
		
		if (versions.size() > 0) {
			return versions;
		} else {
			throw new WikiDataException(WikiDataException.UnknownResource, identifier.toParameterString());
		}
	}
	

	/** Insert a wiki page with a given user as creator
	 * 
	 * @param page
	 * @param user
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized void insert(DatabaseWikiPage page, User user) throws org.dbwiki.exception.WikiException {
		if ((page.getName() != null) && (page.getContent() != null)) {
			if ((!page.getName().trim().equals("")) && (!page.getContent().trim().equals(""))) {
				try {
					Connection con = _connector.getConnection();
					PreparedStatement pStmtInsertPage = con.prepareStatement(
							"INSERT INTO " + _relName + DatabaseConstants.RelationPages + "(" +
							DatabaseConstants.RelPagesColName + ", " +
							DatabaseConstants.RelPagesColContent + ", " +
							DatabaseConstants.RelPagesColTimestamp + ", " +
							DatabaseConstants.RelPagesColUser + ") VALUES(?, ?, ?, ?)");
					
					long timestamp = page.getTimestamp();
					if(timestamp == -1) {
						timestamp = (new Date()).getTime();
					}
					
					int uid = User.UnknownUserID;
					if(user != null) {
						uid = user.id();
					}
					
					pStmtInsertPage.setString(1, page.getName().trim());
					pStmtInsertPage.setString(2, page.getContent().trim());
					pStmtInsertPage.setLong(3, timestamp);
					pStmtInsertPage.setInt(4, uid);
					pStmtInsertPage.execute();
					pStmtInsertPage.close();
					con.close();
				} catch (java.sql.SQLException sqlException) {
					throw new WikiFatalException(sqlException);
				}
			}
		}
	}
	
	/** Update a wiki page id with content and with a given user as creator
	 * 
	 * @param identifier
	 * @param page
	 * @param user
	 * @throws org.dbwiki.exception.WikiException
	 */
	public synchronized void update(PageIdentifier identifier, DatabaseWikiPage page, User user) throws org.dbwiki.exception.WikiException {
		if ((page.getName() != null) && (page.getContent() != null)) {
			if ((!page.getName().trim().equals("")) && (!page.getContent().trim().equals(""))) {
				insert(page, user);
//				try {
//					Connection con = _connector.getConnection();
//					PreparedStatement pStmtUpdatePage = con.prepareStatement(
//							"UPDATE " + _relName + DatabaseConstants.RelationPages + " " +
//							"SET " + DatabaseConstants.RelPagesColName + " = ?, " +
//							DatabaseConstants.RelPagesColContent + " = ? " +
//							"WHERE " + DatabaseConstants.RelPagesColName + " = ?");
//
//					pStmtUpdatePage.setString(1, page.getName().trim());
//					pStmtUpdatePage.setString(2, page.getContent().trim());
//					pStmtUpdatePage.setString(3, identifier.toQueryString());
//					pStmtUpdatePage.execute();
//					pStmtUpdatePage.close();
//					con.close();
//				} catch (java.sql.SQLException sqlException) {
//					throw new WikiFatalException(sqlException);
//				}
			}
		}
	}
}
