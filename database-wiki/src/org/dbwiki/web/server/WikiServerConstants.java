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
package org.dbwiki.web.server;

/** A collection of constants used by the wiki server and databases
 * 
 * @author jcheney
 *
 */
public interface WikiServerConstants {
	/*
	 * Public Constants
	 */
	
	//
	// Special folders and Files
	//
	public static final String SpecialFileDatabaseWikiDefaultCSS = "/html/style/wiki_template.css";
	public static final String SpecialFolderDatabaseWikiStyle = "/html/style/dbwiki";
	public static final String SpecialFolderLogin = "/login";
	public static final String SpecialFolderMarkdown = "/markdown";
	public static final String SpecialFolderPage = "/page";

	
	//
	// Relation Database Schema
	//
	
	//
	// Database listing
	//
	public static final String RelationDatabase = "_database";
	
	public static final String RelDatabaseColAuthentication = "authentication";
	public static final String RelDatabaseColAutoSchemaChanges = "auto_schema_changes";
	public static final String RelDatabaseColCSS = "css_version";
	public static final String RelDatabaseColID = "id";
	public static final String RelDatabaseColIsActive = "is_active";
	public static final String RelDatabaseColLayout = "layout_version";
	public static final String RelDatabaseColName = "name";
	public static final String RelDatabaseColTemplate = "template_version";
	public static final String RelDatabaseColTitle = "title";
	public static final String RelDatabaseColUser = "uid";
	
	public static final int RelDatabaseColIsActiveValFalse = 0;
	public static final int RelDatabaseColIsActiveValTrue = 1;
	
	
	//
	// Presentation
	//
	public static final String RelationPresentation = "_presentation";
	
	public static final String RelPresentationColType     = "type";
	public static final String RelPresentationColVersion  = "version";
	public static final String RelPresentationColTime     = "time";
	public static final String RelPresentationColUser     = "uid";
	public static final String RelPresentationColValue    = "value";
	public static final String RelPresentationColDatabase = "database";

	public static final int RelConfigFileColFileTypeValCSS = 1;
	public static final int RelConfigFileColFileTypeValLayout = 2;
	public static final int RelConfigFileColFileTypeValTemplate = 3;

	public static final int RelConfigFileColFileVersionValUnknown = -1;
	
	
	//
	// User listing
	//
	public static final String RelationUser = "_user";

	public static final String RelUserColID = "id";
	public static final String RelUserColFullName = "full_name";
	public static final String RelUserColLogin = "login";
	public static final String RelUserColPassword = "password";
	
}
