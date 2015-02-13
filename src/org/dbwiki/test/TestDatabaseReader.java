package org.dbwiki.test;

import java.io.File;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.driver.rdbms.DatabaseReader;
import org.dbwiki.driver.rdbms.PSQLDatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.web.server.WikiServer;

public class TestDatabaseReader {
	public static void main(String[] args) {
		  try {
				WikiServer wikiserver=new WikiServer(org.dbwiki.lib.IO.loadProperties(new File(args[0])));
				wikiserver.start();
				PSQLDatabaseConnector psql = new PSQLDatabaseConnector("jdbc:postgresql://localhost", "dbwiki", "password");
				RDBMSDatabase database= new RDBMSDatabase(wikiserver.get(1), psql);
				NodeIdentifier nodeidentifier= new NodeIdentifier();
				//DatabaseReader reader= new DatabaseReader();
				DatabaseReader.get(psql.getConnection(), database, nodeidentifier);
			    database.query("wpath://child::COUNTRY/child::NAME");
			    database.query("wpath://COUNTRY[CATEGORY/PROPERTY/*='Europe']/NAME");
			    database.query("wpath://COUNTRY[NAME='United States']/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP (purchasing power parity)']");
			    System.out.println(wikiserver.size());
				   
				 
	 


		  } catch (Exception exception) {
				exception.printStackTrace();
				System.exit(0);
			}
			 
		 }
	 
}
