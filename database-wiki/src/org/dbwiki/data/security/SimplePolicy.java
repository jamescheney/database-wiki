package org.dbwiki.data.security;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Vector;

import org.dbwiki.data.index.DatabaseContent;
import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.lib.None;
import org.dbwiki.lib.Option;
import org.dbwiki.lib.Some;
import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.DatabaseWiki;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.WikiServerConstants;

public class SimplePolicy implements WikiServerConstants {

    private Vector<Authorization> _authorizationListing;
    
    private int _authenticationMode;
    
    
    public SimplePolicy(int mode, SimplePolicy parent) {
    	_authenticationMode = mode;
    	_authorizationListing = parent._authorizationListing;
    }
    
    
    public SimplePolicy(int mode) {
    	_authenticationMode = mode;
    }
    
    public int getAuthenticationMode() {
    	return _authenticationMode;
    }
    
    public void setAuthenticationMode(int mode) {
    	_authenticationMode = mode;
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

	public static boolean allowedFileRequest(Exchange<?> exchange) {
		// If the request is for a file (as specified by the 
    	// regular expressions above), then no authorization is
        // required.
		for (String m : SimplePolicy.fileMatches) {
			if (exchange.getRequestURI().getPath().matches(m)) 
				return true;
    	}
		return false;
    }
	
	/* TODO: Would be nice to define this as a method of Exchange */
	private static boolean isUpdateRequest(Exchange<?> exchange) {
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

    private static boolean isDeleteRequest(Exchange<?> exchange) {
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
    
    

    private static boolean isInsertRequest(Exchange<?> exchange) {
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
    private static boolean isProtectedRequest(Exchange<?> exchange) {
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
    private static String[] fileMatches = { 
    	".*/html/.*\\.html",
    	".*/html/style/.*\\.css",
    	".*/js/.*\\.js",
    	".*/.*\\.txt",
    	".*/.*\\.ico",
    	".*/pictures/.*\\.gif",
    };

   
    /** Checks whether a request is an entry-level request
     *
     * @param uri the request uri
     * @return boolean
     * @throws SQLException
     * @throws WikiException
     */
    private static Option<Integer> isEntryLevelRequest(URI uri, DatabaseWiki wiki) {
    	String path = uri.getPath();
    	if (uri.getRawQuery() != null) {
    		path = path + "?" + uri.getRawQuery();
    	}
    	String[] items = path.split("/");
    	if(items.length >= 3) {
    		if(items[2].contains("?")) {
    			if(items[2].split("\\?")[0].length() == 0) {
    				return new None<Integer>();
    			} else {
    				try {
    					return new Some<Integer>(Integer.parseInt(items[2].split("\\?")[0],16));
    				} catch (NumberFormatException e) {
    					return new None<Integer>();
    				}
    			}
    		} else {
    			try {
    				DatabaseContent entries = wiki.database().content();
    				try {
    					int entry = Integer.parseInt(items[2],16);
    					if(entries.get(entry)!=null) {
    						return new Some<Integer>(entry);
    					}
    				} catch (NumberFormatException e) {
    					return new None<Integer>();
    				}
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    	return new None<Integer>();
    }

    /** Checks whether we have entry-level policies for the specific request
     *
     * @param exchange HttpExchange
     * @return boolean
     */
    private static boolean havePolicies(Map<Integer, Map<Integer,DBPolicy>> listing, int userId, int entryId) {
    	if(listing != null) {
    		if(listing.get(userId) != null) {
    			if(listing.get(userId).get(entryId) != null) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
   
    /** Check a request by user uname to do request specified by exchange */
    public boolean checkRequest(User user, DatabaseWiki wiki, Exchange<?> exchange) {
    	boolean isProtectedRequest = isProtectedRequest(exchange);
    	if(user.is_admin()) { 
    		return true; 
    	} else if (wiki != null) { // FIXME: wiki is nonnull only if this is a wiki-specific request
    		for(int i = 0;i< _authorizationListing.size();i++){
    			int user_id = _authorizationListing.get(i).user_id();
    			String database_name = _authorizationListing.get(i).database_name();
    			//String user_login = _users.get(user_id).login();
    			if(user.id() == user_id && database_name.equals(wiki.name())) {
    				//get the access permissions in the database
    				boolean isRead = _authorizationListing.get(i).capability().isRead();
    				boolean isInsert = _authorizationListing.get(i).capability().isInsert();
    				boolean isDelete = _authorizationListing.get(i).capability().isDelete();
    				boolean isUpdate = _authorizationListing.get(i).capability().isUpdate();

    				URI uri = exchange.getRequestURI();
    				Option<Integer> entryIdOpt = SimplePolicy.isEntryLevelRequest(uri, wiki);
    				// FIXME: This should become part of the policy state.
    				Map<Integer,Map<Integer,DBPolicy>> policyListing = wiki.getDBPolicyListing(user_id);

    				// check what kind of request it is
    				if (isProtectedRequest) {
    					//insert request
    					if (SimplePolicy.isInsertRequest(exchange) && isInsert == true){
    						// if this is an entry-level request
    						if(entryIdOpt.exists()) {
    							int entryId = entryIdOpt.elt();
    							// and we have an entry-level policy
    							if(SimplePolicy.havePolicies(policyListing, user_id,entryId)) {
    								return policyListing.get(user_id).get(entryId).capability().isInsert();
    							}
    						} // otherwise allow it
    						return true; 
    						// delete request
    					} else if (SimplePolicy.isDeleteRequest(exchange) && isDelete == true){
    						if(entryIdOpt.exists()) {
    							int entryId = entryIdOpt.elt();    
    							if(SimplePolicy.havePolicies(policyListing, user_id,entryId)) {
    								return policyListing.get(user_id).get(entryId).capability().isDelete();
    							}
    						}
    						return true; 
    						// update request
    					} else if (SimplePolicy.isUpdateRequest(exchange) && isUpdate == true){
    						if(entryIdOpt.exists()){
    							int entryId = entryIdOpt.elt();    
    							if(SimplePolicy.havePolicies(policyListing, user_id,entryId)) {
    								return policyListing.get(user_id).get(entryId).capability().isUpdate();
    							}
    						}
    						return true;
    					} else {
    						return false;
    					}
    				} else { // !isProtectedRequest
    					//read request
    					if (isRead == true) {
    						if (entryIdOpt.exists()){
    							int entryId = entryIdOpt.elt();    
    							if(SimplePolicy.havePolicies(policyListing, user_id,entryId)) {
    								return policyListing.get(user_id).get(entryId).capability().isRead();
    							}
    						}
    						return true;
    					} else { // !isRead
    						return false;
    					}
    				}
    			}
    		}
    	}
    	// if no rule matches, deny access
    	return false; 
    }
   
    public boolean isControlledRequest(Exchange <?> exchange) {
    	return ((_authenticationMode == DatabaseWikiProperties.AuthenticateAlways) ||
    			(_authenticationMode == DatabaseWikiProperties.AuthenticateWriteOnly) 
    			&& isProtectedRequest(exchange));
    }
   
    /** Determine whether there exists any authorization for user user_index in database wiki_name. 
     * FIXME: It would be better to build a map from user ids and db names to capabilities.
     * @param user_index
     * @param wiki_name
     * @return
     */
    public boolean find (int user_index, String wiki_name) {
    	int j = 0;
    	for(j = 0; j< _authorizationListing.size();j++){
    		int user_id = _authorizationListing.get(j).user_id();
    		String database_name = _authorizationListing.get(j).database_name();
    		if(user_id==user_index && database_name.equals(wiki_name)){
    			return true;
    		}
    	}
    	return false;
    }

    /** Return the authorization/capability for user user_index in database wiki_name
     * FIXME: It would be better to build a map from user ids and db names to capabilities.
     * @param user_index
     * @param wiki_name
     * @return
     */
    public Capability findCapability(int user_index , String wiki_name) {
    	int j = 0;
    	for(j = 0; j< _authorizationListing.size();j++){
    		int user_id = _authorizationListing.get(j).user_id();
    		String database_name = _authorizationListing.get(j).database_name();
    		if(user_id==user_index && database_name.equals(wiki_name)){
    			return _authorizationListing.get(j).capability();
    		}
    	}
    	return null;
    }
    
    /* FIXME: #security It would be better if this also updated the authorization listing. */
    public void updateCapability(Connection con, int user_index, String wiki_name, Capability cap) 
    	throws SQLException {
    	PreparedStatement pStmt = null;
		
		if (find(user_index, wiki_name)) {
			pStmt = con.prepareStatement("UPDATE "
					+ RelationAuthorization + " " + "SET "
					+ RelAuthenticationColRead + " = ?, "
					+ RelAuthenticationColInsert + " = ?, "
					+ RelAuthenticationColDelete + " = ?, "
					+ RelAuthenticationColUpdate + " = ? "
					+ "WHERE " + RelAuthenticationColUserID
					+ " = ? " + "AND "
					+ RelAuthenticationColDatabaseName + " = ?");

			pStmt.setBoolean(1, cap.isRead());
			pStmt.setBoolean(2, cap.isInsert());
			pStmt.setBoolean(3, cap.isDelete());
			pStmt.setBoolean(4, cap.isUpdate());
			pStmt.setInt(5, user_index);
			pStmt.setString(6, wiki_name);
			pStmt.execute();
			pStmt.close();
		} else {
			pStmt = con.prepareStatement("INSERT INTO "
					+ RelationAuthorization + "("
					+ RelAuthenticationColDatabaseName + ", "
					+ RelAuthenticationColUserID + ", "
					+ RelAuthenticationColRead + ", "
					+ RelAuthenticationColInsert + ", "
					+ RelAuthenticationColDelete + ", "
					+ RelAuthenticationColUpdate
					+ ") VALUES(?, ?, ?, ?, ?, ?)");

			pStmt.setString(1, wiki_name);
			pStmt.setInt(2, user_index);
			pStmt.setBoolean(3, cap.isRead());
			pStmt.setBoolean(4, cap.isInsert());
			pStmt.setBoolean(5, cap.isDelete());
			pStmt.setBoolean(6, cap.isUpdate());
			pStmt.execute();
			pStmt.close();
		}
    }

}
