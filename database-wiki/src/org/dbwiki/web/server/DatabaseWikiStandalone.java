/*
    BEGIN LICENSE BLOCK
    Copyright 2010-2014, Heiko Mueller, Sam Lindley, James Cheney, 
    Ondrej Cierny, Mingjun Han, and
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

package org.dbwiki.web.server;

import java.sql.Connection;
import org.dbwiki.data.wiki.SimpleWiki;
import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.driver.rdbms.SQLVersionIndex;

/**
 * Implements a basic database wiki suitable for command line execution
 * 
 * @author jcheney
 * 
 */
public class DatabaseWikiStandalone extends DatabaseWiki {
	
	protected WikiServerStandalone _server;

	/**
	 * Create new DatabaseWiki from given data. Used in
	 * WikiServer.getWikiListing.
	 * 
	 */
	public DatabaseWikiStandalone(int id, String name, String title,
			int autoSchemaChanges, int authenticationMode,
			ConfigSetting setting, DatabaseConnector connector,
			WikiServerStandalone server)
			throws org.dbwiki.exception.WikiException {
		_authenticationMode = authenticationMode;
		_id = id;
		_server = server;
		_name = name;
		_title = title;

		reset(setting.getLayoutVersion(), setting.getTemplateVersion(),
				setting.getStyleSheetVersion(),
				setting.getURLDecodingRulesVersion());

		_database = new RDBMSDatabase(this, connector);
		_database.validate();
		_wiki = new SimpleWiki(name, connector, server.users());
	}

	// HACK: pass in and use an existing connection and version index.
	// Used only in WikiServer.RegisterDatabase to create a new database.
	public DatabaseWikiStandalone(int id, String name, String title,
			int autoSchemaChanges, int authenticationMode,
			DatabaseConnector connector, WikiServerStandalone server,
			Connection con, SQLVersionIndex versionIndex)
			throws org.dbwiki.exception.WikiException {
		_authenticationMode = authenticationMode;
		_autoSchemaChanges = autoSchemaChanges;
		_id = id;
		_server = server;
		_name = name;
		_title = title;

		ConfigSetting setting = new ConfigSetting();

		reset(setting.getLayoutVersion(), setting.getTemplateVersion(),
				setting.getStyleSheetVersion(),
				setting.getURLDecodingRulesVersion());

		_database = new RDBMSDatabase(this, connector, con, versionIndex);
		_wiki = new SimpleWiki(name, connector, server.users());
	}



	/*
	 * Getters
	 */

	@Override
	public WikiServer server() {
		return _server;
	}


}
