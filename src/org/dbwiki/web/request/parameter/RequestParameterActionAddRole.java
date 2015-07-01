package org.dbwiki.web.request.parameter;

public class RequestParameterActionAddRole extends RequestParameterAction {
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
	return false;
}

public boolean actionUpdate() {
	return false;
}

public boolean actionUpdateUsers() {
	return false;
}

public String toURLString() {
	return RequestParameter.ParameterAction + "=" + RequestParameterAction.ActionCancelEntryAuthorizationUpdate;
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
	return true;
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
