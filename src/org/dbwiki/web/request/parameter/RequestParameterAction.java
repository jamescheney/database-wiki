/*
    BEGIN LICENSE BLOCK
    Copyright 2010-2014, Heiko Mueller, Sam Lindley, James Cheney, 
    Ondrej Cierny, Mingjun Han, and
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
	
	public static final String ActionAnnotate    = "annotate";
	public static final String ActionCancel      = "cancel";
	public static final String ActionSchemaNode  = "schema_node";
	public static final String ActionInsert      = "insert";
	public static final String ActionUpdate      = "update";
	public static final String ActionUpdateUsers = "update_users";
	public static final String ActionUpdateAuthorization = "update_authorization";
	public static final String ActionCancelAuthorizationUpdate = "cancel_authorization_update";
	public static final String ActionUpdateEntryAuthorization = "update_entry_authorization";
	public static final String ActionCancelEntryAuthorizationUpdate = "cancel_entry_authorization_update";
	
	//zhuowei 
	public static final String ActionAddRole = "add_role";
	public static final String ActionDeleteRole = "delete_role";
	public static final String ActionExitRoleManagement = "exit_role_management";
	public static final String ActionUpdateRoleAuthorization = "update_role_authorization";
	public static final String ActionCancelRoleAuthorizationUpdate = "cancel_role_authorization_update";
	public static final String ActionUpdateRoleAssignment = "update_role_assignment";
	public static final String ActionCancelRoleAssignmentUpdate = "cancel_role_assignment_update";
	public static final String ActionUpdateRoleName = "update_role_name";
	public static final String ActionCancelRoleNameUpdate = "cancel_role_name_update";
	public static final String ActionAssignUser = "assign_user";
	public static final String ActionUnassignUser = "unassign_user";
	public static final String ActionSearchUsers = "search_users";
	public static final String ActionExitRoleAssignment = "exit_role_assignment";
	
	
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
	
	//zhuowei
	public abstract boolean actionUpdateRoleManagement();
	public abstract boolean actionCancelRoleManagementUpdate();
	public abstract boolean actionAddRole();
	public abstract boolean actionUpdateRoleAuthorization();
	public abstract boolean actionCancelRoleAuthorizationUpdate();
	public abstract boolean actionUpdateRoleAssignment();
	public abstract boolean actionCancelRoleAssignmentUpdate();
}
