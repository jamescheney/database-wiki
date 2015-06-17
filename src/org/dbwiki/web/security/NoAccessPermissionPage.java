package org.dbwiki.web.security;

import org.dbwiki.web.html.HtmlPage;

/** 
 * Responds to a unprivileged request from user.
 *
 */

@Deprecated
public class NoAccessPermissionPage extends HtmlPage{
	
	/*
	 * Constructors
	 */
	
	public NoAccessPermissionPage() {
		this.add("<html><head><title>Database Wiki - No Access Permission</title></head><body><h1>No Access Permission<h2><p>You don't have permissions, please contact the administrator!</p><p><a href='/?action=cancel'>Back to Server Home.</a></p>");
	}
}

