package org.dbwiki.lib;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public final class JDBC {

	/*
	 * Public Methods
	 */
	
	public static boolean hasColumn(ResultSet rs, String name) throws java.sql.SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int numCol = meta.getColumnCount();
		for (int iColumn = 1; iColumn < numCol + 1; iColumn++) {
		    if(meta.getColumnName(iColumn).equals(name)) {
		    	return true;}
		}
		return false;
	}
}
