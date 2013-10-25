package org.dbwiki.data.query.visual;

public class ArgumentNode extends Node {
	private String _arg;
	
	ArgumentNode(String arg) {
		_arg = arg;
	}
	String get() {
		return _arg;
	}
}
