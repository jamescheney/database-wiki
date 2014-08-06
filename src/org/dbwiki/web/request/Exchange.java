package org.dbwiki.web.request;

import java.io.InputStream;
import java.net.URI;

import org.dbwiki.web.html.HtmlPage;

public interface Exchange<T> {
	public URI getRequestURI();
	public String getUsername();
	public String getCookie();
	public int getLocalPort();
	public boolean isGet();
	public boolean isPost();
	public String contentType();
	public InputStream getRequestBody() throws java.io.IOException;
	public void send(HtmlPage page) throws java.io.IOException;
	public void send(HtmlPage page, int responseCode) throws java.io.IOException;
	public void sendData(String contentType, InputStream is) throws java.io.IOException;
	public void sendXML(InputStream is) throws java.io.IOException;
	public void sendJSON(InputStream is) throws java.io.IOException;
	
	public T get();
	public void setResponseHeader(String string, String string2);
	}
