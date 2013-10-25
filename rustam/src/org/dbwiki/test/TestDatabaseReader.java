package org.dbwiki.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.sql.Connection;

import org.dbwiki.data.database.Database;
import org.dbwiki.data.document.DocumentGroupNode;
import org.dbwiki.data.document.DocumentNode;
import org.dbwiki.data.query.QueryResultSet;
import org.dbwiki.data.resource.DatabaseIdentifier;
import org.dbwiki.data.resource.NodeIdentifier;
import org.dbwiki.data.resource.ResourceIdentifier;
import org.dbwiki.driver.rdbms.DatabaseReader;
import org.dbwiki.driver.rdbms.DatabaseWriter;
import org.dbwiki.driver.rdbms.PSQLDatabaseConnector;
import org.dbwiki.driver.rdbms.RDBMSDatabase;
import org.dbwiki.exception.WikiException;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.WikiDataRequest;
import org.dbwiki.web.server.WikiServer;
import org.omg.CORBA.Request;

public class TestDatabaseReader {
	public static void main(String[] args) {
		  try {
				WikiServer wikiserver=new WikiServer(org.dbwiki.lib.IO.loadProperties(new File(args[0])));
				wikiserver.start();
				PSQLDatabaseConnector psql = new PSQLDatabaseConnector("jdbc:postgresql://localhost", "postgres", "postgres");
				RDBMSDatabase database= new RDBMSDatabase(wikiserver.get(1), psql); // wikiserver.get() returns DatabaseWiki
				
				//NodeIdentifier nodeidentifier= new NodeIdentifier();
				//DatabaseReader reader= new DatabaseReader();
				
				// reader.get(psql.getConnection(), database, nodeidentifier);
				long time1 = System.currentTimeMillis();
				QueryResultSet var = database.query("nid://6"); //wpath://catalog/book[price < 10]/title
								
				long time2 = System.currentTimeMillis();
				
				// old scheme
				//4.921
				//4.63
				//4.672
				//4.741 average
				
				// DDE scheme
				//4.588
				//4.336
				//4.338
				//4.42 average
				
			    //database.query("wpath://child::COUNTRY/child::NAME");
			    //database.query("wpath://COUNTRY[CATEGORY/PROPERTY/*='Europe']/NAME");
				/*long start, end, result;
				start = System.currentTimeMillis();
			    database.query("wpath://COUNTRY[NAME='United States']/CATEGORY[NAME='Economy']/PROPERTY[NAME='GDP (purchasing power parity)']");
			    end = System.currentTimeMillis();
			    System.out.println((end-start+0.0)/1000);
			    System.out.println(wikiserver.size());*/


		  } catch (Exception exception) {
				exception.printStackTrace();
				System.exit(0);
			}
			 
		 }
	 
}
