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
	
	//zhuowei 
	public static final String AddRole = "add_role";
	public static final String AddMutex = "add_mutex";
	public static final String UpdateSuperRoles = "update_super_roles";
	public static final String UpdateRoleAuthority = "update_role_authority";
	public static final String UpdateRoleName = "update_role_name";
	public static final String DeleteRole = "delete_role";
	public static final String DeleteMutex = "delete_mutex";
	public static final String AssignUser = "assign_user";
	public static final String UnassignUser = "unassign_user";
	public static final String SearchUsers = "search_users";
	public static final String UpdateEntryRoleAuthority = "update_entry_role_authority";
	
	
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
