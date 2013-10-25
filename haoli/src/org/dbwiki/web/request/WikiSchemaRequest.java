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

import org.dbwiki.data.resource.WRI;
import org.dbwiki.data.schema.SchemaNode;
import org.dbwiki.data.time.VersionIndex;
import org.dbwiki.web.server.DatabaseWiki;

public class WikiSchemaRequest extends WikiRequest {
	// TODO: Much of this code is shared with WikiDataRequest.
	// Perhaps we should refactor...
	
	private static final byte requestTypeUNKNOWN  = 0;
	private static final byte requestTypeGET      = 1;
	private static final byte requestTypePOST     = 2;
	
	private WRI _wri;
	
	private boolean _isRootRequest;
	private SchemaNode _schemaNode;
	private byte _requestType = requestTypeUNKNOWN;

	/**
	 * Constructors
	 */
	public WikiSchemaRequest(DatabaseWiki wiki, RequestURL url) throws org.dbwiki.exception.WikiException {
		super(wiki, url);
		
		_isRootRequest = url.isRoot();
    	_wri = new WRI(wiki.database().identifier(), wiki.database().getSchemaNodeIdentifierForURL(url));
    	_schemaNode = this.wiki().database().getSchemaNode(_wri.resourceIdentifier());
    	
	    if (url.exchange().getRequestMethod().equalsIgnoreCase("GET")) {
	    	_requestType = requestTypeGET;
	    } else if (url.exchange().getRequestMethod().equalsIgnoreCase("POST")) {
	    	_requestType = requestTypePOST;
	    }	
	}
	
	public boolean isGET() {
		return (_requestType == requestTypeGET);
	}
	
	public boolean isPOST() {
		return (_requestType == requestTypePOST);
	}
	
	public boolean isRootRequest() {
		return _isRootRequest;
	}

	public SchemaNode schema() {
    	return _schemaNode;
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
