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

import java.util.List;

import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.PageIdentifier;
import org.dbwiki.data.resource.WRI;

import org.dbwiki.data.time.VersionIndex;

import org.dbwiki.data.wiki.DatabaseWikiPage;
import org.dbwiki.data.wiki.Wiki;

import org.dbwiki.exception.data.WikiNodeException;

import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.DatabaseWiki;

// TODO: Hoist this to subclass of HttpRequest with no dependency on WikiRequest
public class WikiPageRequest extends WikiRequest  {
	/*
	 * Private Variables
	 */
	
	private Wiki _wiki;
	private DatabaseWikiPage _page = null;
	private WRI _wri;
	
	
	/*
	 * Constructors
	 */
	
	public WikiPageRequest(DatabaseWiki wiki, RequestURL url) throws org.dbwiki.exception.WikiException {
		super(wiki, url);
		
		_wiki = wiki.wiki();
		
		DatabaseIdentifier wikiIdentifier = new DatabaseIdentifier(wiki.identifier().databaseHomepage() + "/" + RequestURL.WikiPageRequestPrefix);
		if (url.size() == 0) {
			_wri = new WRI(wikiIdentifier, new PageIdentifier());
		} else if (url.size() == 1) {
			long timestamp = -1;
			RequestParameter timestampParameter =
				url.parameters().get(RequestParameter.ParameterVersion);
			if(timestampParameter != null) {
				timestamp = Long.parseLong(timestampParameter.value());
			}
			_wri = new WRI(wikiIdentifier, new PageIdentifier(url.get(0).encodedText(), timestamp));
		} else {
			throw new WikiNodeException(WikiNodeException.InvalidIdentifierFormat, url.toString());
		}
	}
	
	
	/*
	 * Public Methods
	 */
	
	public DatabaseWikiPage page() throws org.dbwiki.exception.WikiException {
    	if ((_page == null) && (_wri.resourceIdentifier() != null)) {
    		_page = _wiki.get(_wri.resourceIdentifier());
    	}
    	return _page;
	}
	
	// differs from page() in that it just gets title and value from request without db access 
	public DatabaseWikiPage getWikiPage() {
		String title = null;
		String value = null;
		
		if (parameters().hasParameter(RequestParameter.ActionValuePageTitle)) {
			title = parameters().get(RequestParameter.ActionValuePageTitle).value();
		}
		if (parameters().hasParameter(RequestParameter.ActionValuePageValue)) {
			value = parameters().get(RequestParameter.ActionValuePageValue).value();
		}
		return new DatabaseWikiPage(-1, title, value, -1, user());
	}

	public List<DatabaseWikiPage> versions() throws org.dbwiki.exception.WikiException {
		return _wiki.versions(_wri.resourceIdentifier());
	}
	
	public VersionIndex versionIndex() {
		return null;
	}

	public WRI wri() {
		return _wri;
	}
}
