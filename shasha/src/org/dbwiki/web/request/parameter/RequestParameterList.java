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
package org.dbwiki.web.request.parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.dbwiki.exception.WikiFatalException;

public class RequestParameterList {
	/*
	 * Private Variables
	 */
	
	private Hashtable<String, RequestParameter> _parameterIndex;
	private Vector<RequestParameter> _parameterList;
	
	
	/*
	 * Constructors
	 */
	
	public RequestParameterList() {
		_parameterIndex = new Hashtable<String, RequestParameter>();
		_parameterList = new Vector<RequestParameter>();
	}
	
	public RequestParameterList(String urlParameter) throws WikiFatalException {
		this();
		
		if (urlParameter != null) {
			StringTokenizer tokens = new StringTokenizer(urlParameter, "&");
	    	while (tokens.hasMoreTokens()) {
	    		try {
					this.add(new RequestParameter(URLDecoder.decode(tokens.nextToken(), "UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					throw new WikiFatalException(e);
				}
	    	}
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void add(RequestParameter parameter) {
		if ((!parameter.name().endsWith(".x")) && (!parameter.name().endsWith(".y"))) {
			_parameterIndex.put(parameter.name(), parameter);
			_parameterList.add(parameter);
		}
	}
	
	public RequestParameter get(int index) {
		return _parameterList.get(index);
	}
	
	public RequestParameter get(String key) {
		return _parameterIndex.get(key);
	}

	public boolean hasParameter(String key) {
		return _parameterIndex.containsKey(key);
	}
	
	public int size() {
		return _parameterList.size();
	}
	
	@Override
	public String toString() {
		if (_parameterList.size() > 0) {
			String line = "?" + _parameterList.get(0).toString();
			for (int iPara = 1; iPara < _parameterList.size(); iPara++) {
				line = line + "&" + _parameterList.get(iPara).toString();
			}
			return line;
		} else {
			return "";
		}
	}
}
