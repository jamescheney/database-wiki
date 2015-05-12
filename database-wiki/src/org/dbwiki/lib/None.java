package org.dbwiki.lib;

import org.dbwiki.lib.Option;

public class None<T> extends Option<T> {
	public None() {}
	public boolean exists() { return false; }
	public T elt() { return null; }
}