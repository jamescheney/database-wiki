package org.dbwiki.web.security;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.dbwiki.exception.WikiException;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.server.Authorization;
import org.dbwiki.web.server.DBPolicy;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.Entry;
import org.dbwiki.web.server.WikiServer;



/** A servlet-based authenticator class for DatabaseWiki 
 * 
 * @author o.cierny
 *
 */

public class WikiServletAuthenticator {

	private int _mode;
	private UserListing _users;
	private String _realm;
	private Vector<Authorization> _authorizationListing;
	private boolean isReadRequest;
	private boolean isInsertRequest;
	private boolean isDeleteRequest;
	private boolean isUpdateRequest;
	private boolean isEntryLevelRequest;
	private int entryId;
	
	public WikiServletAuthenticator(int mode, String realm, UserListing users, Vector<Authorization> authorizationListing) {
		_mode = mode;
		_users = users;
		_realm = realm;
		_authorizationListing = authorizationListing;
	}
	
	public boolean authenticate(HttpServletRequest request) {
		String path = request.getRequestURI();
		
		// File requests don't require authentication
		if(path.indexOf('.') != -1) {
			return true;
		}
		
		boolean needsAuth = this.isProtectedRequest(request);
		if(request.getUserPrincipal() == null) {
			if ((_mode == DatabaseWikiProperties.AuthenticateAlways) || 
				(_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
				return false;
			}
			else {
				return true;
			}
		} else if ((_mode == DatabaseWikiProperties.AuthenticateAlways) || 
			(_mode == DatabaseWikiProperties.AuthenticateWriteOnly) && needsAuth) {
			String uname = request.getUserPrincipal().getName();
			// check if user is administrator
			if(_users.get(uname).is_admin()) {
				return true;
			} else {
				Map<Integer, Map<Integer,DBPolicy>> policyListing = new HashMap<Integer, Map<Integer,DBPolicy>>();
				for(int i = 0; i<_authorizationListing.size(); i++) {
					int user_id = _authorizationListing.get(i).user_id();
					String database_name = _authorizationListing.get(i).database_name();
					String dbname = "/" + database_name;
					String user_login = _users.get(user_id).login();
					if(user_login.equals(uname) && dbname.equals(_realm)) {
						//get the access permissions in the database
						boolean isRead = _authorizationListing.get(i).is_read();
						boolean isInsert = _authorizationListing.get(i).is_insert();
						boolean isDelete = _authorizationListing.get(i).is_delete();
						boolean isUpdate = _authorizationListing.get(i).is_update();
						// check what kind of request it is
						if(needsAuth) {
							// insert request
							if(isInsertRequest == true && isInsert == true) {
								if(isEntryLevelRequest == true) {
									policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
									if(havePolicies(policyListing, user_id)) {
										return policyListing.get(user_id).get(entryId).isInsert();
									}
								}
								return true;
							// delete request
							} else if(isDeleteRequest == true && isDelete == true) {
								if(isEntryLevelRequest == true) {
									policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
									if(havePolicies(policyListing, user_id)) {
										return policyListing.get(user_id).get(entryId).isDelete();
									}
								}
								return true;
							// update request
							} else if(isUpdateRequest == true && isUpdate == true){
								if(isEntryLevelRequest == true) {
									policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
									if(havePolicies(policyListing, user_id)) {
										return policyListing.get(user_id).get(entryId).isUpdate();
									}
								}
								return true;
							} else {
								return false;
							}
						} else {
							// read request
							isReadRequest = true;
							if(isReadRequest == true && isRead == true) {
								if(isEntryLevelRequest(request)) {
									policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
									if(havePolicies(policyListing, user_id)) {
										return policyListing.get(user_id).get(entryId).isRead();
									}
								}
								return true;
							} else {
								return false;
							}
						}
					}
				}
			}
			return false;
		} else {
			return true;
		}
	}
	
	/** Checks whether we have entry-level policies for the specific request 
	 * 
	 * @param exchange HttpExchange
	 * @return boolean
	 */
	private boolean havePolicies(Map<Integer, Map<Integer,DBPolicy>> listing, int userId) {
		if(listing != null) {
			if(listing.get(userId) != null) {
				if(listing.get(userId).get(entryId) != null) {
					return true;
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
	private boolean isProtectedRequest(HttpServletRequest request) {
		if ("GET".equalsIgnoreCase(request.getMethod())) {
	    	List<?> parameters = Collections.list((Enumeration<?>)request.getParameterNames());
			if (parameters != null) {
				isReadRequest = true;
				isInsertRequest = false;
				isDeleteRequest = false;
				isUpdateRequest = false;
				if(parameters.size() == 0) {
					return false;
				} else if (parameters.contains(RequestParameter.ParameterActivate)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterCreate)) {
					isInsertRequest = true;
					if(isEntryLevelRequest(request)) {
						isEntryLevelRequest = true;
					} else {
						isEntryLevelRequest = false;
					}
					return true;
				} else if (parameters.contains(RequestParameter.ParameterAllUsers)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterAuthorization)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterCreateSchemaNode)) {
					isInsertRequest = true;
					if(isEntryLevelRequest(request)) {
						isEntryLevelRequest = true;
					} else {
						isEntryLevelRequest = false;
					}
					return true;
				} else if (parameters.contains(RequestParameter.ParameterDelete)) {
					isDeleteRequest = true;
					if(isEntryLevelRequest(request)) {
						isEntryLevelRequest = true;
					} else {
						isEntryLevelRequest = false;
					}
					return true;
				} else if (parameters.contains(RequestParameter.ParameterEdit)) {
					isUpdateRequest = true;					
					if(isEntryLevelRequest(request)) {
						isEntryLevelRequest = true;
					} else {
						isEntryLevelRequest = false;
					}
					return true;
				} else if (parameters.contains(RequestParameter.ParameterLayout)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterPaste)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterReset)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterTemplate)) {
					return true;
				} else if (parameters.contains(RequestParameter.ParameterStyleSheet)) {
					return true;
				} else {
					return false;
				}
			}
	    }
	    return false;
	}
	
	/** Checks whether a request is an entry-level request
	 * 
	 * @param uri the request uri
	 * @return boolean
	 * @throws SQLException
	 * @throws WikiException
	 */
	private boolean isEntryLevelRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (request.getQueryString() != null) {
	        uri = uri + "?" + request.getQueryString();
	    }
		String[] items = uri.split("/");
		if(items.length >= 3) {
			if(items[2].contains("?")) {
				if(items[2].split("\\?")[0].length() == 0) {
					return false;
				} else {
					try {
						this.entryId = Integer.parseInt(items[2].split("\\?")[0],16);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				}
			} else {
				try {
					Map<Integer, Entry> entryListing = WikiServer.getEntryListing(_realm.substring(1));
					try {
						int entry = Integer.parseInt(items[2],16);
						if(entryListing.get(entry)!=null) {
							this.entryId = entry;
							return true;
						}
					} catch (NumberFormatException e) {
						return false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		return false;
	}
	
	public int getAuthenticationMode() {
		return _mode;
	}
	
	public void setAuthenticationMode(int mode) {
		_mode = mode;
	}
	
	public void updateAuthorizationListing(Vector<Authorization> auth) {
		_authorizationListing = auth;
	}

}
