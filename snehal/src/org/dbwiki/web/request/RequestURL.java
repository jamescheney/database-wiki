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

import org.dbwiki.web.server.DatabaseWiki;

import com.sun.net.httpserver.HttpExchange;

public class RequestURL {
	/*
	 * Private Variables
	 */
	
	/* 'annotation' enum type is added for handling annotation request
	 * 'annotationStructure' enum type is added for visualisation of annotation request
	 * 'provenanceStructure' enum type is added for visualisation of provenance request
	 * 'provenanceComparison' enum type is added for comparison of two versions request
	 * 'provenanceTree' enum type is added for displaying provenance tree request*/
	public enum Type {
		Data, Page, Schema, Annotation, annotationStructure, provenanceStructure, provenanceComparison, provenanceTree; 
	}
	
	private Type _type;
	
	private Vector<URLComponent> _components;
	private HttpExchange _exchange = null;
	
	private boolean _isGETRequest = false;
	private RequestParameterList _parameters = null;
	
	
	/*
	 * Constructors
	 */
	
	public RequestURL(HttpExchange exchange, String ignorePathPrefix) throws org.dbwiki.exception.WikiException {
		_exchange = exchange;
		
		URI uri = exchange.getRequestURI();
		
		// assume this is a request for data until proven otherwise
		_type = Type.Data;
		_components = this.split(uri.getPath().substring(ignorePathPrefix.length()));
		
		
		if (_components.size() > 0) {
			
			String firstComponent = _components.get(0).decodedText(); //Added to get the first component of request URL
			String lastComponent = _components.lastElement().decodedText(); //Added to get the last component of request URL
			
			if (firstComponent.equals(DatabaseWiki.WikiPageRequestPrefix)) {
				_components.remove(0);
				_type = Type.Page;
			} else if (firstComponent.equals(DatabaseWiki.SchemaRequestPrefix)) {
				_components.remove(0);
				_type = Type.Schema;
			} 
			
			/* These if cases are added to check the request by its keywords present in request URL 
			 * if yes then removes its last component and
			 * also assigns its type */
			else if(lastComponent.equals(DatabaseWiki.AnnotationRequestPrefix)) {
				_components.remove((_components.size())-1);
				_type = Type.Annotation;
			}
			else if(lastComponent.equals(DatabaseWiki.AnnotationStructureRequestPrefix)) {
				_components.remove(0);
				_type = Type.annotationStructure;
			}	
			else if(lastComponent.equals(DatabaseWiki.ProvenanceStructureRequestPrefix)) {
				_components.remove(0);
				_type = Type.provenanceStructure;
			}	
			else if(lastComponent.equals(DatabaseWiki.ProvenanceComparisonRequestPrefix)) {
				_components.remove(1);
				_type = Type.provenanceComparison;
			}	
			else if(lastComponent.equals(DatabaseWiki.ProvenanceTreeRequestPrefix)) {
				_components.remove(1);
				_type = Type.provenanceTree;
			}
		}

		String urlParameter = null;
		try {
		    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
		    	_isGETRequest = true;
		    	String rawQuery = exchange.getRequestURI().getRawQuery();
				if (rawQuery != null) {
					urlParameter = URLDecoder.decode(rawQuery, "UTF-8");
				}
		    } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
		    	_isGETRequest = false;
				BufferedReader in = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
				String line;
				while ((line = in.readLine()) != null) {
					if (urlParameter != null) {
						urlParameter = urlParameter + line;
					} else {
						urlParameter = line;
					}
				}
				in.close();

				// There seems to be a problem when using UTF-8 here, especially for
				// &nbsp; in the parameter value. But ISO-8859-1 does not seem to
				// work all the time either???
				//urlParameter = URLDecoder.decode(urlParameter, "ISO-8859-1");
				urlParameter = URLDecoder.decode(urlParameter, "UTF-8");
		    }
		} catch (java.io.IOException ioException) {
			throw new WikiFatalException(ioException);
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
	
	public HttpExchange exchange() {
		return _exchange;
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
	
	/* Added function to check the request for annotation */
	public boolean isAnnotationRequest() {
		return _type.equals(Type.Annotation);
	}

	/* Added function to check the request for annotation visualisation */
	public boolean isAnnotationStructureRequest() {
		return _type.equals(Type.annotationStructure);
	}
	
	/* Added function to check the request for provenance visualisation */
	public boolean isProvenanceStructureRequest() {
		return _type.equals(Type.provenanceStructure);
	}
	
	/* Added function to check the request for comparison of 2 provenance records */
	public boolean isProvenanceComparisonRequest() {
		return _type.equals(Type.provenanceComparison);
	}
	
	/* Added function to check the request for display provenance tree */
	public boolean isProvenanceTreeRequest() {	
		return _type.equals(Type.provenanceTree);
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
