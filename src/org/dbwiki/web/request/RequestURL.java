/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.web.request;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.URLDecoder;

import java.util.StringTokenizer;
import java.util.Vector;

import org.dbwiki.exception.WikiFatalException;

import org.dbwiki.web.request.parameter.RequestParameterList;


public class RequestURL {
	/*
	 * Public Constants
	 */
	public static final String WikiPageRequestPrefix = "wiki";
	public static final String SchemaRequestPrefix = "schema";
	
	/*
	 * Private Variables
	 */
	
	public enum Type {
		Data, Page, Schema;
	}
	
	private Type _type;
	private URI _uri;
	private String _username = null;
	private String _cookie = null;
	
	private Vector<URLComponent> _components;
	private Exchange<?> _exchange = null;
	
	private boolean _isGETRequest = false;
	private RequestParameterList _parameters = null;
	
	
	/*
	 * Constructors
	 */
	
	// TODO #request: Factor the URL / request parsing code out
	public RequestURL(Exchange<?> exchange, String ignorePathPrefix) throws org.dbwiki.exception.WikiFatalException {
		_exchange = exchange;
		
		_uri = exchange.getRequestURI();
		_username = exchange.getUsername();
		_cookie = exchange.getCookie();
		
		// assume this is a request for data until proven otherwise
		_type = Type.Data;
		_components = this.split(_uri.getPath().substring(ignorePathPrefix.length()));
		if (_components.size() > 0) {
			String firstComponent = _components.get(0).decodedText();
			if (firstComponent.equals(WikiPageRequestPrefix)) {
				_components.remove(0);
				_type = Type.Page;
			} else if (firstComponent.equals(SchemaRequestPrefix)) {
				_components.remove(0);
				_type = Type.Schema;
			}
		}

		if (_exchange.isGet()) {
			_isGETRequest = true;
		} else if (_exchange.isPost()) {
			_isGETRequest = false;
		}
		
		String urlParameter = null;
		// FIXME: It should be an error if the request is neither GET nor POST.
		try {
		    if (_isGETRequest) {
		    	String rawQuery = _uri.getRawQuery();
				if (rawQuery != null) {
					urlParameter = URLDecoder.decode(rawQuery, "UTF-8");
				}
		    } else {
		    	BufferedReader in = new BufferedReader(new InputStreamReader(_exchange.getRequestBody()));
				String line;
				while ((line = in.readLine()) != null) {
					if (urlParameter != null) {
						urlParameter = urlParameter + line;
					} else {
						urlParameter = line;
					}
				}
				in.close();
		    }
		} catch (Exception exception) {
			throw new WikiFatalException(exception);
		}
		_parameters = new RequestParameterList(urlParameter);
	}


	
	public RequestURL(String path) {
		_components = this.split(path);
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(URLComponent component) {
		_components.add(component);
	}
	

	public URLComponent get(int index) {
		return _components.get(index);
	}
	
	public boolean isDataRequest() {
		return _type.equals(Type.Data);
	}
	
	public boolean isPageRequest() {
		return _type.equals(Type.Page);
	}

	public boolean isSchemaRequest() {
		return _type.equals(Type.Schema);
	}
	
	public boolean isGET() {
		return _isGETRequest;
	}
	
	public boolean isPOST() {
		return !_isGETRequest;
	}
	
	public boolean isRoot() {
		return (_components.size() == 0);
	}
	
	public RequestParameterList parameters() {
		return _parameters;
	}
	
	public int size() {
		return _components.size();
	}

	public String toString() {
		String url = "";
		for (int iComponent = 0; iComponent < _components.size(); iComponent++) {
			url = url + "/" + _components.get(iComponent).decodedText();
		}
		return url + _parameters.toString();
	}
	
	public URI getRequestURI() {
		return _uri;	
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getCookie () {
		return _cookie;
	}
	/*
	 * Private Methods
	 */
	
	private Vector<URLComponent> split(String path) {
		Vector<URLComponent> components = new Vector<URLComponent>();
		if (!path.equals("/")) {
			StringTokenizer tokens = null;
			if (path.startsWith("/")) {
				tokens = new StringTokenizer(path.substring(1), "/");
			} else {
				tokens = new StringTokenizer(path, "/");
			}
			while (tokens.hasMoreTokens()) {
				components.add(new URLComponent(tokens.nextToken()));
			}
		}
		return components;
	}
}
