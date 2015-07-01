package org.dbwiki.data.security;

public class Permission {
	public static final int positivePermission = 1;
	public static final int negativePermission = -1;
	public static final int neutralPermission = 0;
	
	private int _readPermission;
	private int _insertPermission;
	private int _updatePermission;
	private int _deletePermission;
	
	public Permission(int rp, int ip, int up, int dp) {
		_readPermission = rp;
		_insertPermission = ip;
		_updatePermission = up;
		_deletePermission = dp;
	}
	
	public boolean positiveReadPermission() {
		if(_readPermission == positivePermission) {
			return true;
		}
		return false;
	}
	
	public boolean negativeReadPermission() {
		if(_readPermission == negativePermission) {
			return true;
		}
		return false;
	}
	
	public boolean neutralReadPermission() {
		if(_readPermission == neutralPermission) {
			return true;
		}
		return false;
	}
	
	public boolean positiveInsertPermission() {
		if(_insertPermission == positivePermission) {
			return true;
		}
		return false;
	}
	
	public boolean negativeInsertPermission() {
		if(_insertPermission == negativePermission) {
			return true;
		}
		return false;
	}
	
	public boolean neutralInsertPermission() {
		if(_insertPermission == neutralPermission) {
			return true;
		}
		return false;
	}
	public boolean positiveUpdatePermission() {
		if(_updatePermission == positivePermission) {
			return true;
		}
		return false;
	}
	
	public boolean negativeUpdatePermission() {
		if(_updatePermission == negativePermission) {
			return true;
		}
		return false;
	}
	
	public boolean neutralUpdatePermission() {
		if(_updatePermission == neutralPermission) {
			return true;
		}
		return false;
	}
	
	public boolean positiveDeletePermission() {
		if(_deletePermission == positivePermission) {
			return true;
		}
		return false;
	}
	
	public boolean negativeDeletePermission() {
		if(_deletePermission == negativePermission) {
			return true;
		}
		return false;
	}
	
	public boolean neutralDeletePermission() {
		if(_deletePermission == neutralPermission) {
			return true;
		}
		return false;
	}
	
	public int getReadPermission() {
		return _readPermission;
	}
	
	public int getInsertPermission() {
		return _insertPermission;
	}
	
	public int getUpdatePermission() {
		return _updatePermission;
	}
	
	public int getDeletePermission() {
		return _deletePermission;
	}
	
}
