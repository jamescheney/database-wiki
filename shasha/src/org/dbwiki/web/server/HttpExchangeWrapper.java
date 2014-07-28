package org.dbwiki.web.server;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;

import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.request.Exchange;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
/**
 * Wrapper for HttpExchange to provide just the methods that we need in RequestURL.
 * @author jcheney
 *
 */
@SuppressWarnings("restriction")

public class HttpExchangeWrapper  implements Exchange<HttpExchange> {
	
	HttpExchange _exchange;

	public HttpExchangeWrapper(HttpExchange exchange) {
		_exchange = exchange;
	}

	@Override
	public URI getRequestURI() {
		return _exchange.getRequestURI();
	}

	@Override
	public String getUsername() {
		if(_exchange.getPrincipal() != null) {
			return _exchange.getPrincipal().getUsername();
		}
		return null;
	}

	@Override
	public String getCookie() {
		if (_exchange.getRequestHeaders().getFirst("Cookie") != null) {
			return _exchange.getRequestHeaders().getFirst("Cookie");
		}	
		return null;
	}

	@Override
	public boolean isGet() {
		return _exchange.getRequestMethod().equalsIgnoreCase("GET");
	}

	@Override
	public boolean isPost() {
		return _exchange.getRequestMethod().equalsIgnoreCase("POST");
	}

	@Override
	public InputStream getRequestBody() {
		return _exchange.getRequestBody();
	}

		
	@Override
	public  void send(HtmlPage page) throws java.io.IOException {
		send(page, HttpURLConnection.HTTP_OK);
	}
	
	@Override
	public void send(HtmlPage page, int responseCode) throws java.io.IOException {
    	Headers responseHeaders = _exchange.getResponseHeaders();
    	responseHeaders.set("Content-Type", "text/html");
    	_exchange.sendResponseHeaders(responseCode, 0);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(_exchange.getResponseBody()));
    	for (int iLine = 0; iLine < page.size(); iLine++) {
    		out.write(page.get(iLine));
    		out.newLine();
    	}
    	out.close();
	}


	@Override
	public HttpExchange get() {
		return _exchange;
	}
}
