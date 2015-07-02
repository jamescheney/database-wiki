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
package org.dbwiki.web.request.parameter;


public abstract class RequestParameterAction {
	/*
	 * Public Constants
	 */
	
	public static final String ActionAnnotate   = "annotate";
	public static final String ActionCancel     = "cancel";
	public static final String ActionSchemaNode = "schema_node";
	public static final String ActionInsert     = "insert";
	public static final String ActionUpdate     = "update";
	public static final String ActionUpdateUsers = "update_users";
	public static final String ActionUpdateAuthorization = "update_authorization";
	public static final String ActionCancelAuthorizationUpdate = "cancel_authorization_update";
	public static final String ActionUpdateEntryAuthorization = "update_entry_authorization";
	public static final String ActionCancelEntryAuthorizationUpdate = "cancel_entry_authorization_update";
	
	/*
	 * Abstract Methods
	 */
	
	public abstract boolean actionAnnotate();
	public abstract boolean actionCancel();
	public abstract boolean actionSchemaNode();
	public abstract boolean actionInsert();
	public abstract boolean actionUpdate();
	public abstract boolean actionUpdateUsers();
	public abstract boolean actionUpdateAuthorization();
	public abstract boolean actionCancelAuthorizationUpdate();
	public abstract boolean actionUpdateEntryAuthorization();
	public abstract boolean actionCancelEntryAuthorizationUpdate();
}
