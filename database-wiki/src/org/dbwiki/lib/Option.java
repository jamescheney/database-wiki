package org.dbwiki.lib;

	
public abstract class Option<T> {
	abstract public boolean exists();
	abstract public T elt();
}