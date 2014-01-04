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

import org.dbwiki.data.database.DatabaseNode;

import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.data.resource.WRI;

import org.dbwiki.data.time.VersionIndex;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;

public class WikiDataRequest<T> extends WikiRequest<T> {
	/*
	 * Private Constants
	 */
	
	private static final byte requestTypeUNKNOWN  = 0;
	private static final byte requestTypeGET      = 1;
	private static final byte requestTypePOST     = 2;
	
	
	/*
	 * Private Variables
	 */
	
	private boolean _isRootRequest;
	private DatabaseNode _node;
	private byte _requestType = requestTypeUNKNOWN;
	private WRI _wri;
	
	
	/*
	 * Constructor
	 */
	
	public WikiDataRequest(DatabaseWiki wiki, RequestURL<T> url) throws org.dbwiki.exception.WikiException {
		super(wiki, url);
		_isRootRequest = url.isRoot();
		
		ResourceIdentifier nodeIdentifier = null;
		if (url.size() <= 1) {
			try {
				nodeIdentifier = wiki.database().getNodeIdentifierForURL(url);
			} catch (org.dbwiki.exception.WikiException wikiException) {
			}
		}
		if ((nodeIdentifier == null) && (wiki.urlDecoder().size() > 0)) {
			nodeIdentifier = wiki.urlDecoder().decode(wiki.database(), url, RequestParameter.versionParameter(this.parameters().get(RequestParameter.ParameterVersion)));
		}
		
    	_wri = new WRI(wiki.database().identifier(), nodeIdentifier);
    	_node = null;
    	
    	/*
	    if (url.exchange().getRequestMethod().equalsIgnoreCase("GET")) {
	    	_requestType = requestTypeGET;
	    } else if (url.exchange().getRequestMethod().equalsIgnoreCase("POST")) {
	    	_requestType = requestTypePOST;
	    }
	    */	
    	if (url.isGET()) {
	    	_requestType = requestTypeGET;
	    } else if (url.isPOST()) {
	    	_requestType = requestTypePOST;
	    }
	}
	
	/*
	 * Public Methods
	 */
	
	public boolean isGET() {
		return (_requestType == requestTypeGET);
	}
	
	public boolean isPOST() {
		return (_requestType == requestTypePOST);
	}
	
	@Override
	public boolean isRootRequest() {
		return _isRootRequest;
	}

	public DatabaseNode node() throws org.dbwiki.exception.WikiException {
    	if ((_node == null) && (_wri.resourceIdentifier() != null)) {
    		_node = this.wiki().database().get(_wri.resourceIdentifier());
    	}
    	
    	return _node;
	}
	
	@Override
	public String toString() {
		return getRequestURI().toString();
	}
	
	@Override
	public VersionIndex versionIndex() {
		return this.wiki().database().versionIndex();
	}

	@Override
	public WRI wri() {
		return _wri;
	}
}
