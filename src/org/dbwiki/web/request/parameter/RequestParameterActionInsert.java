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


public class RequestParameterActionInsert extends RequestParameterAction {
	/*
	 * Public Methods
	 */
	
	public boolean actionAnnotate() {
		return false;
	}

	public boolean actionCancel() {
		return false;
	}
	
	public boolean actionSchemaNode() {
		return false;
	}
	
	public boolean actionInsert() {
		return true;
	}

	public boolean actionUpdate() {
		return false;
	}
	
	public boolean actionUpdateUsers() {
		return false;
	}

	public String toURLString() {
		return RequestParameter.ParameterAction + "=" + RequestParameterAction.ActionInsert;
	}

	@Override
	public boolean actionUpdateAuthorization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actionCancelAuthorizationUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actionUpdateEntryAuthorization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actionCancelEntryAuthorizationUpdate() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//zhuowei
	public boolean actionUpdateRoleManagement(){
		return false;
	}
	
	public boolean actionCancelRoleManagementUpdate(){
		return false;
	}
	
	public boolean actionAddRole(){
		return false;
	}
	
	public boolean actionUpdateRoleAuthorization(){
		return false;
	}
	
	public boolean actionCancelRoleAuthorizationUpdate(){
		return false;
	}
	
	public boolean actionUpdateRoleAssignment(){
		return false;
	}
	
	public boolean actionCancelRoleAssignmentUpdate(){
		return false;
	}
}
