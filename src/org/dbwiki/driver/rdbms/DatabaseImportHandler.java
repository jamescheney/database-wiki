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
package org.dbwiki.driver.rdbms;

import java.net.URL;
import java.sql.Connection;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.io.ImportHandler;
import org.dbwiki.data.provenance.ProvenanceImport;
import org.dbwiki.data.time.Version;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;

/** Import handler that loads the xml data being imported into RDBMSDatabase 
 * FIXME: This should really be part of the Database interface, since to use this we currently need to case Database to RDBMSDatabase
 * @author jcheney
 *
 */
public class DatabaseImportHandler implements ImportHandler {
	/*
	 * Private Variables
	 */
	
	private Connection _con;
	private Database _database;
	private Version _importVersion;
	
	
	/*
	 * Constructors
	 */
	
	public DatabaseImportHandler(Connection con, Database database) {
		_con = con;
		_database = database;
	}
	
	
	/*
	 * Public Methods
	 */
	
	public void endImport() throws org.dbwiki.exception.WikiException {
		try {
			_database.versionIndex().add(_importVersion);
			((SQLVersionIndex)_database.versionIndex()).store(_con);
		} catch (org.dbwiki.exception.WikiException wikiException) {
			throw wikiException;
		}
	}

	public void importDocument(DocumentGroupNode document) throws org.dbwiki.exception.WikiException {
		try {
			_con.setAutoCommit(false);
			boolean success = true;
			try {
				new DatabaseWriter(_con, _database).insertRootNode(document, _importVersion);
			} catch (Exception exception) {
				_con.rollback();
				success = false;
				throw new WikiFatalException(exception);
			}
			if (success) {
				_con.commit();
			}
			_con.setAutoCommit(true);
		} catch (java.sql.SQLException sqlException) {
			throw new WikiFatalException(sqlException);
		}
	}

	public void startImport(User user, URL sourceURL) throws org.dbwiki.exception.WikiException {
		_importVersion = _database.versionIndex().getNextVersion(new ProvenanceImport(user, sourceURL));
	}
}
