package org.dbwiki.lib;

import org.dbwiki.lib.Option;

public class Some<T> extends Option<T> {
	T _elt;
	public Some(T elt) {
		_elt = elt;
	}
	public boolean exists() { return true; }
	public T elt() { return _elt; }
}