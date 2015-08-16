package org.dbwiki.data.security;

public class Permission {
	public static final int positive = 1;
	public static final int negative = -1;
	public static final int neutral = 0;
	
	private int _read;
	private int _insert;
	private int _update;
	private int _delete;
	
	public Permission() {
		_read = negative;
		_insert = negative;
		_update = negative;
		_delete = negative;
	}
	
	public Permission(int rp, int ip, int up, int dp) {
		_read = rp;
		_insert = ip;
		_update = up;
		_delete = dp;
	}
	
	public boolean isPositiveRead() {
		if(_read == positive) {
			return true;
		}
		return false;
	}
	
	public boolean isNegativeRead() {
		if(_read == negative) {
			return true;
		}
		return false;
	}
	
	public boolean isNeutralRead() {
		if(_read == neutral) {
			return true;
		}
		return false;
	}
	
	public boolean isPositiveInsert() {
		if(_insert == positive) {
			return true;
		}
		return false;
	}
	
	public boolean isNegativeInsert() {
		if(_insert == negative) {
			return true;
		}
		return false;
	}
	
	public boolean isNeutralInsert() {
		if(_insert == neutral) {
			return true;
		}
		return false;
	}
	public boolean isPositiveUpdate() {
		if(_update == positive) {
			return true;
		}
		return false;
	}
	
	public boolean isNegativeUpdate() {
		if(_update == negative) {
			return true;
		}
		return false;
	}
	
	public boolean isNeutralUpdate() {
		if(_update == neutral) {
			return true;
		}
		return false;
	}
	
	public boolean isPositiveDelete() {
		if(_delete == positive) {
			return true;
		}
		return false;
	}
	
	public boolean isNegativeDelete() {
		if(_delete == negative) {
			return true;
		}
		return false;
	}
	
	public boolean isNeutralDelete() {
		if(_delete == neutral) {
			return true;
		}
		return false;
	}
	
	public int getRead() {
		return _read;
	}
	
	public int getInsert() {
		return _insert;
	}
	
	public int getUpdate() {
		return _update;
	}
	
	public int getDelete() {
		return _delete;
	}
	
	public void setRead(int rp) {
		_read = rp;
	}
	
	public void setInsert(int ip) {
		_insert = ip;
	}
	
	public void setUpdate(int up) {
		_update = up;
	}
	
	public void setDelete(int dp) {
		_delete = dp;
	}
	
	public boolean isFullAccess() {
		if (_read == positive &&
				_insert == positive &&
				_update == positive &&
				_delete == positive) {
			return true;
		}
		return false;
	}
}
