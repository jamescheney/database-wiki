package org.dbwiki.web.server;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
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

	public URI getRequestURI() {
		return _exchange.getRequestURI();
	}

	public String getUsername() {
		if(_exchange.getPrincipal() != null) {
			return _exchange.getPrincipal().getUsername();
		}
		return null;
	}

	public String getCookie() {
		if (_exchange.getRequestHeaders().getFirst("Cookie") != null) {
			return _exchange.getRequestHeaders().getFirst("Cookie");
		}	
		return null;
	}

	public boolean isGet() {
		return _exchange.getRequestMethod().equalsIgnoreCase("GET");
	}

	public boolean isPost() {
		return _exchange.getRequestMethod().equalsIgnoreCase("POST");
	}

	public InputStream getRequestBody() {
		return _exchange.getRequestBody();
	}

		
	public  void send(HtmlPage page) throws java.io.IOException {
		send(page, HttpURLConnection.HTTP_OK);
	}
	
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
	
	public String contentType() {
		String filename = _exchange.getRequestURI().getPath();
		
		int pos = filename.lastIndexOf('.');
		if (pos != -1) {
			String suffix = filename.substring(pos);
			if (suffix.equalsIgnoreCase(".uu")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".exe")) {
				return "application/octet-stream";
			} else if (suffix.equalsIgnoreCase(".ps")) {
				return "application/postscript";
			} else if (suffix.equalsIgnoreCase(".zip")) {
				return "application/zip";
			} else if (suffix.equalsIgnoreCase(".sh")) {
				return "application/x-shar";
			} else if (suffix.equalsIgnoreCase(".tar")) {
				return "application/x-tar";
			} else if (suffix.equalsIgnoreCase(".snd")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".au")) {
				return "audio/basic";
			} else if (suffix.equalsIgnoreCase(".wav")) {
				return "audio/x-wav";
			} else if (suffix.equalsIgnoreCase(".gif")) {
				return "image/gif";
			} else if (suffix.equalsIgnoreCase(".jpg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".jpeg")) {
				return "image/jpeg";
			} else if (suffix.equalsIgnoreCase(".htm")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".html")) {
				return "text/html";
			} else if (suffix.equalsIgnoreCase(".text")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".c")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".cc")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".css")) {
				return "text/css";
			} else if (suffix.equalsIgnoreCase(".c++")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".h")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".pl")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".txt")) {
				return "text/plain";
			} else if (suffix.equalsIgnoreCase(".java")) {
				return "text/plain";
			} else {
				return "content/unknown";
			}
		} else {
			return "content/unknown";
		}
	}



	public void sendData(String contentType, InputStream is) throws java.io.IOException {
		Headers responseHeaders = _exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", contentType);
		_exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
		OutputStream os = _exchange.getResponseBody();
		int n;
		byte[] buf = new byte[2048];
		while ((n = is.read(buf)) > 0) {
			os.write(buf, 0, n);
		}
		is.close();
		os.close();
	}
	
	public void sendXML(InputStream is) throws java.io.IOException {
		sendData("application/xml", is);
	}
	
	public void sendJSON(InputStream is) throws java.io.IOException {
		sendData("application/json", is);
	}

	public HttpExchange get() {
		return _exchange;
	}

	public void setResponseHeader(String header, String value) {
		Headers responseHeaders = _exchange.getResponseHeaders();
		responseHeaders.set(header, value);
	}

	public int getLocalPort() {
		return _exchange.getLocalAddress().getPort();
	}
}
