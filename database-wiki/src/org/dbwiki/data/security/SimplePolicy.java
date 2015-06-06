package org.dbwiki.data.security;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.WikiServerConstants;

public class SimplePolicy implements WikiServerConstants {

    public Vector<Authorization> _authorizationListing;
    public int _mode;
    
    
    public SimplePolicy(int mode, Vector<Authorization> authorizationListing) {
    	_mode = mode;
    	_authorizationListing = authorizationListing;
    }
    
    
    public SimplePolicy(int mode) {
    	_mode = mode;
    }
    
	/**
	 * Initialize authorization listing from database
	 * 
	 */
	public void getAuthorizationListing(Connection connection)
			throws SQLException {
		_authorizationListing = new Vector<Authorization>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM "
				+ RelationAuthorization);
		while (rs.next()) {
			_authorizationListing.add(new Authorization(
					rs.getString(RelAuthenticationColDatabaseName), 
					rs.getInt(RelAuthenticationColUserID), 
					rs.getBoolean(RelAuthenticationColRead), 
					rs.getBoolean(RelAuthenticationColInsert), 
					rs.getBoolean(RelAuthenticationColDelete), 
					rs.getBoolean(RelAuthenticationColUpdate)));
		}
		
		rs.close();
		stmt.close();

	}

	/* TODO: Would be nice to define this as a method of Exchange */
	public static boolean isUpdateRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
	                    return true;
	                } 
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isDeleteRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
	                    return true;
	                } 
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }
    
    

    public static boolean isInsertRequest(Exchange<?> exchange) {
        if (exchange.isGet()) {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            if (rawQuery != null) {
                try {
                	RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
	                    return true;
	                }
                } catch (WikiFatalException e) {
                    e.printStackTrace();
                    // is this really what we want to do?
                    return false;
                }
            }
        }
        return false;
    }


    /** Checks whether a request accesses a protected resource
    *
    * @param exchange HttpExchange
    * @return boolean
    */
   public static boolean isProtectedRequest(Exchange<?> exchange) {
       if (exchange.isGet()) {
           String rawQuery = exchange.getRequestURI().getRawQuery();
           if (rawQuery != null) {
               try {
                   RequestParameterList parameters = new RequestParameterList(rawQuery);
	                if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
	                    return true;
	                } else if(parameters.hasParameter(RequestParameter.ParameterAllUsers)){
	                    return true;
	                } else if(parameters.hasParameter(RequestParameter.ParameterAuthorization)){
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterLayout)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterPaste)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterReset)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterTemplate)) {
	                    return true;
	                } else if (parameters.hasParameter(RequestParameter.ParameterStyleSheet)) {
	                    return true;
	                } else {
	                    return false;
	                }
               } catch (WikiFatalException e) {
                   e.printStackTrace();
                   // is this really what we want to do?
                   return true;
               }
               
           }
       }
       return false;
   	}

   
   
   // Hardwired policy filtering file requests
   public static String[] fileMatches = { 
   		".*/html/.*\\.html",
   		".*/html/style/.*\\.css",
   		".*/js/.*\\.js",
   		".*/.*\\.txt",
   		".*/.*\\.ico",
   		".*/pictures/.*\\.gif",
   		};

}
