package org.dbwiki.appengine;

import com.google.appengine.api.rdbms.AppEngineDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.dbwiki.driver.rdbms.DatabaseConnector;
import org.dbwiki.driver.rdbms.MySQLDatabaseConnector;





@SuppressWarnings("serial")
public class DatabaseWikiAppEngineServlet extends HttpServlet {
	
	public void doDrop(DatabaseConnector connector, PrintWriter out) {
		
		try {
			connector.dropServer(true);
			out.print("Dropped database; redirecting...");
		} catch (Exception  e) {
			out.print(e);
		}
	}
	
	public void doCreate(DatabaseConnector connector, PrintWriter out) {
		
		try {	
			connector.createServer(new File("users"));
			out.print("Created database; redirecting...");
		} catch (Exception  e) {
			out.print(e);
		}
		
	}
	
	
	public void doAdduser(String name, String username, String password, String password2, PrintWriter out) {
		
		Connection c = null;
	    try {
	      //Class.forName("com.google.appengine.api.rdbms.AppEngineDriver");
	      DriverManager.registerDriver(new AppEngineDriver());
	      c = DriverManager.getConnection("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki");
	      
	      // TODO: Check validity
	      if (name.equals("") || username.equals("") || password.equals("") || password2.equals("")) {
    	  	out.println("Username, name, and password must not be empty. Try again! Redirecting in 3 seconds...");
	      } else if (!password.equals(password2)) {
  	        	out.println("Passwords must match. Try again! Redirecting in 3 seconds...");
  	      } else {
  	      
    	  String statement ="INSERT INTO _user (full_name,login,password) VALUES( ? , ?, ? )";
	      PreparedStatement stmt = c.prepareStatement(statement);
	      stmt.setString(1, name);
	      stmt.setString(2, username);
	      stmt.setString(3, password);
	      int success = stmt.executeUpdate();
	      if(success == 1) {
	        out.println("Success! Redirecting in 3 seconds...");
	      } else if (success == 0) {
	        out.println("Failure! Please try again! Redirecting in 3 seconds...");
	      }
	     }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } /*catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/ finally {
			if (c != null) { 
				try {
					c.close();
	            } catch (SQLException ignore) {
	            }
			}
		}
	}
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();
		out.print("<html><head><title>DBWiki test</title></head><body>");
		out.print("Redirecting...");		
		out.print("</body></html>");
		resp.setHeader("Refresh","3; url=/server.jsp");

	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {
		
		DatabaseConnector connector = new MySQLDatabaseConnector("jdbc:google:rdbms://database-wiki-cloudsql:test/dbwiki", "", "");
		PrintWriter out = resp.getWriter();
		out.print("<html><head><title>DBWiki test</title></head><body>");
		
		if(req.getServletPath().equals("/drop")) {
			doDrop(connector,out);
		} else if (req.getServletPath().equals("/create")) {
			doCreate(connector,out);
		} else if (req.getServletPath().equals("/adduser")) {
			String name = req.getParameter("name");
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String password2 = req.getParameter("password2");
			doAdduser(name,username,password,password2,out);
		}
		// TODO: Handle database create, drop
		out.print("</body></html>");

		resp.setHeader("Refresh","3; url=/server.jsp");
	}
}
