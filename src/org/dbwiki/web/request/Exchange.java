package org.dbwiki.web.request;

import java.io.InputStream;
import java.net.URI;

import org.dbwiki.web.html.HtmlPage;

public interface Exchange<T> {
	public URI getRequestURI();
	public String getUsername();
	public String getCookie();
	public boolean isGet();
	public boolean isPost();
	public InputStream getRequestBody();
	public void send(HtmlPage page) throws java.io.IOException ;
	public void send(HtmlPage page, int responseCode) throws java.io.IOException;
	public T get();
	}
