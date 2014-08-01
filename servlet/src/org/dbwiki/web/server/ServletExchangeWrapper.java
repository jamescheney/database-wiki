package org.dbwiki.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbwiki.web.html.HtmlPage;
import org.dbwiki.web.request.Exchange;
/**
 * Wrapper for the HttpServlet classes to provide methods that we need in RequestURL
 * @author o.cierny
 */

public class ServletExchangeWrapper implements Exchange {
	
	HttpServletRequest _request;
	HttpServletResponse _response;

	public ServletExchangeWrapper(HttpServletRequest request, HttpServletResponse response) {
		_request = request;
		_response = response;
	}

	public URI getRequestURI() {
		if (_request.getQueryString() == null) {
	        return URI.create(_request.getRequestURL().toString());
	    } else {
	        return URI.create(_request.getRequestURL().append('?').append(_request.getQueryString()).toString());
	    }
	}

	public String getUsername() {
		if(_request.getSession().getAttribute("user") != null)
			return _request.getSession().getAttribute("user").toString();
		else {
			return null;
		}
	}

	public String getCookie() {
		Cookie cookies[] = _request.getCookies();
		if (cookies != null) {
			StringBuilder cookString = new StringBuilder();
			for (int i = 0; i < cookies.length; i++) {
				cookString.append(cookies[i].getName()).append('=').append(cookies[i].getValue());
				if ((i + 1) < cookies.length) {
					cookString.append("; ");
				}
			}
			return cookString.toString();
		}	
		return null;
	}

	public boolean isGet() {
		return "GET".equals(_request.getMethod());
	}

	public boolean isPost() {
		return "POST".equals(_request.getMethod());
	}

	public InputStream getRequestBody() throws IOException {
		return _request.getInputStream();
	}

	public  void send(HtmlPage page) throws java.io.IOException {
		send(page, HttpURLConnection.HTTP_OK);
	}
	
	public void send(HtmlPage page, int responseCode) throws java.io.IOException {
    	_response.setContentType("text/html");
    	_response.setStatus(responseCode);
    	PrintWriter out = _response.getWriter();
    	for (int iLine = 0; iLine < page.size(); iLine++) {
    		out.println(page.get(iLine));
    	}
	}

}
