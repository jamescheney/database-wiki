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
package org.dbwiki.web.security;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.dbwiki.exception.WikiException;
import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;
import org.dbwiki.user.UserListing;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.HttpRequest;
import org.dbwiki.web.request.RequestURL;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.Authorization;
import org.dbwiki.web.server.DBPolicy;
import org.dbwiki.web.server.Entry;
import org.dbwiki.web.server.HtmlSender;
import org.dbwiki.web.server.HttpExchangeWrapper;
import org.dbwiki.web.server.WikiServer;
import org.dbwiki.web.ui.HtmlContentGenerator;
import org.dbwiki.web.ui.HtmlTemplateDecorator;
import org.dbwiki.web.ui.ServerResponseHandler;
import org.dbwiki.web.ui.printer.server.DatabaseAccessDeniedPrinter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

/** An authenticator class for a DatabaseWiki 
 * 
 * @author jcheney
 *
 */

@SuppressWarnings("restriction")
public class WikiAuthenticator extends Authenticator {
	/*
	 * Public Constants
	 */
	
	public static final int AuthenticateAlways = 0;
	public static final int AuthenticateNever = 1;
	public static final int AuthenticateWriteOnly = 2;
	
	public static final int NoAccessPermission = 0;
	public static final int ReadOnlyPermission = 1;
	public static final int ReadAndWritePermission = 2;
	
	public static final boolean HoldPermission = true;
	
	

	
	/*
	 * Private Variables
	 */
	
	private int _mode;
	private String _realm;
	private UserListing _users;
	private Vector<Authorization> _authorizationListing;
	private File _formTemplate = null;
	
	private boolean isReadRequest;
	private boolean isInsertRequest;
	private boolean isDeleteRequest;
	private boolean isUpdateRequest;
	private boolean isEntryLevelRequest;
	private int entryId;
	
	/*
	 * Constructors
	 */
	
	public WikiAuthenticator(String realm, int mode, UserListing users, Vector<Authorization> authorizationListing, File template) {
		_mode = mode;
		_realm = realm;
		_users = users;
		_authorizationListing = authorizationListing;
		_formTemplate = template;
	}
	
	
	/*
	 * Public Methods
	 */
	
	private boolean allowedFileRequest(String path ) {
		// If the request is for a file (currently indicated by
		// a '.' in the request path), then no authorization is
		// required.
		// FIXME #security: Security hole? Generalize the test for whether something is a file.
		return path .indexOf('.') != -1;
	}
	
	
	public synchronized Result authenticate(HttpExchange exchange){
		// FIXME: #security: If the request is to log in then we should check the username and password no matter what!
		// Currently we don't if we happen to be at a page that doesn't require authentication.
		
		if (allowedFileRequest(exchange.getRequestURI().getPath())) {
			return new Authenticator.Success(new HttpPrincipal("", _realm));
		}
		
		Headers rmap = exchange.getRequestHeaders();
		
		boolean isProtectedRequest = this.isProtectedRequest(exchange);
		
		String auth = rmap.getFirst("Authorization");
		if (auth == null) {
			if ((_mode == AuthenticateAlways)
					|| ((_mode == AuthenticateWriteOnly) && (isProtectedRequest))
					|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
				Headers map = exchange.getResponseHeaders();
				map.set("WWW-Authenticate", "Basic realm=" + "\"" + _realm + "\"");
				return new Authenticator.Retry(401);
			} else {
				return new Authenticator.Success(new HttpPrincipal(User.UnknownUserName, _realm));
			}
		} else {
			int sp = auth.indexOf(' ');
			if (sp == -1 || !auth.substring(0, sp).equals("Basic")) {
				return new Authenticator.Failure(401);
			}
			byte[] b = new Base64().base64ToByteArray(auth.substring(sp + 1));
			String userpass = new String(b);
			int colon = userpass.indexOf(':');
			String uname = userpass.substring(0, colon);
			String pass = userpass.substring(colon + 1);
			Boolean isAdmin = isAdmin(uname);
			if ((_mode == AuthenticateAlways) 
					|| ((_mode == AuthenticateWriteOnly) && (isProtectedRequest))
					|| (exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin))) {
				if (checkCredentials(uname, pass)) {
					if(isAdmin){
						return new Authenticator.Success(new HttpPrincipal(uname, _realm));
					}else{
						if(exchange.getRequestURI().getPath().equals(WikiServer.SpecialFolderLogin)){
							return new Authenticator.Success(new HttpPrincipal(uname, _realm));
						}
						Map<Integer,Map<Integer,DBPolicy>> policyListing = new HashMap<Integer,Map<Integer,DBPolicy>>();
						for(int i = 0;i<_authorizationListing.size();i++){
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
								if(isProtectedRequest) {
									//insert request
									if(isInsertRequest == true && isInsert == true){
										if(isEntryLevelRequest==true){
											policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
											if(havePolicies(policyListing, user_id)) {
												if(policyListing.get(user_id).get(entryId).isInsert()==true){
													return new Authenticator.Success(new HttpPrincipal(uname, _realm));
												}else{
													try {
														sendAccessDenied(exchange);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
										}
										return new Authenticator.Success(new HttpPrincipal(uname, _realm));
									//delete request
									}else if(isDeleteRequest == true && isDelete == true){
										if(isEntryLevelRequest==true){
											policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
											if(havePolicies(policyListing, user_id)) {
												if(policyListing.get(user_id).get(entryId).isDelete()==true){
													return new Authenticator.Success(new HttpPrincipal(uname, _realm));
												}else{
													try {
														sendAccessDenied(exchange);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
										}
										return new Authenticator.Success(new HttpPrincipal(uname, _realm));
									//update request
									}else if(isUpdateRequest == true && isUpdate == true){
										if(isEntryLevelRequest==true){
											policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
											if(havePolicies(policyListing, user_id)) {
												if(policyListing.get(user_id).get(entryId).isUpdate()==true){
													return new Authenticator.Success(new HttpPrincipal(uname, _realm));
												}else{
													try {
														sendAccessDenied(exchange);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
										}
										return new Authenticator.Success(new HttpPrincipal(uname, _realm));
									}else{
										try {
											sendAccessDenied(exchange);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}else{
									//read request
									isReadRequest = true;
									if(isReadRequest == true && isRead == true){
										String uri = exchange.getRequestURI().toString();
										try {
											if(IsEntryLevelRequest(uri)){
												policyListing = WikiServer.getDBPolicyListing(database_name, user_id);
												if(havePolicies(policyListing, user_id)) {
													if(policyListing.get(user_id).get(entryId).isRead()==true){
														return new Authenticator.Success(new HttpPrincipal(uname, _realm));
													}else{
														try {
															sendAccessDenied(exchange);
														} catch (Exception e) {
															e.printStackTrace();
														}
													}
												}
											}
										} catch (SQLException e) {
											e.printStackTrace();
										} catch (WikiException e) {
											e.printStackTrace();
										}
										return new Authenticator.Success(new HttpPrincipal(uname, _realm));
									}else{
										try {
											sendAccessDenied(exchange);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							} 
						}
					}
					try {
						sendAccessDenied(exchange);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return new Authenticator.Success(new HttpPrincipal(uname, _realm));

				} else {
						Headers map = exchange.getResponseHeaders();
						map.set("WWW-Authenticate", "Basic realm=" + "\"" + _realm	+ "\"");
						return new Authenticator.Failure(401);
				}
			} else {
				return new Authenticator.Success(new HttpPrincipal(uname, _realm));
				
			}
		}
	}

	public synchronized int getAuthenticationMode() {
		return _mode;
	}
	
	public String getRealm() {
		return _realm;
	}
	
	public synchronized void setAuthenticationMode(int value) {
		_mode = value;
	}
	
	
	/*
	 * Private Methods
	 */
	
	/**
	 * Check whether password supplied by user claiming to be username is correct.
	 * FIXME #security: Avoid plaintext passwords!
	 */
	private boolean checkCredentials(String username, String password) {
		if (!_users.isEmpty()) {
			if (_users.contains(username)) {
				return _users.get(username).password().equals(password);
			} else {
				return false;
			}
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
	
	/** Checks whether a user is an administrator or not
	 * 
	 * @param username login name of user
	 * @return boolean
	 */
	private boolean isAdmin(String username){
		int i = 1;
		while(_users.get(i)!=null){
			String uname = _users.get(i).login();
			boolean isAdmin = _users.get(i).is_admin();
			if(uname.equals(username)&&isAdmin){
				return true;
			}else{
				return false;
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
	private boolean IsEntryLevelRequest(String uri) throws SQLException, WikiException{
		
		String[] items = uri.split("/");
		
		if(items.length>=3){
			if(items[2].contains("?")){
				if(items[2].split("\\?")[0].length()==0){
					return false;
				} else {
					try {
						this.entryId = Integer.parseInt(items[2].split("\\?")[0],16);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				}
			}else{
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
			}
		} 
		return false;
	}
	/** Checks whether a request accesses a protected resource 
	 * 
	 * @param exchange HttpExchange
	 * @return boolean
	 */
	private boolean isProtectedRequest(HttpExchange exchange) {
	    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
			String rawQuery = exchange.getRequestURI().getRawQuery();
			if (rawQuery != null) {
				RequestParameterList parameters;
				isReadRequest = true;
				isInsertRequest = false;
				isDeleteRequest = false;
				isUpdateRequest = false;
				String uri = exchange.getRequestURI().toString();
				
				try {
					parameters = new RequestParameterList(rawQuery);
				} catch (WikiFatalException e) {
					e.printStackTrace();
					// is this really what we want to do?
					return true;
				} 
				if (parameters.hasParameter(RequestParameter.ParameterActivate)) {
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreate)) {
					isInsertRequest = true;
					try {
						if(IsEntryLevelRequest(uri)){
							isEntryLevelRequest = true;
						}else{
							isEntryLevelRequest = false;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (WikiException e) {
						e.printStackTrace();
					}
					
					return true;
				}else if(parameters.hasParameter(RequestParameter.ParameterAllUsers)){
					return true;
				}else if(parameters.hasParameter(RequestParameter.ParameterAuthorization)){
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterCreateSchemaNode)) {
					isInsertRequest = true;
					try {
						if(IsEntryLevelRequest(uri)){
							isEntryLevelRequest = true;
						}else{
							isEntryLevelRequest = false;
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WikiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterDelete)) {
					isDeleteRequest = true;
					try {
						if(IsEntryLevelRequest(uri)){
							isEntryLevelRequest = true;
						}else{
							isEntryLevelRequest = false;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (WikiException e) {
						e.printStackTrace();
					}
					return true;
				} else if (parameters.hasParameter(RequestParameter.ParameterEdit)) {
					isUpdateRequest = true;					
					try {
						if(IsEntryLevelRequest(uri)){
							isEntryLevelRequest = true;
						}else{
							isEntryLevelRequest = false;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (WikiException e) {
						e.printStackTrace();
					}
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
			}
	    }
	    return false;
	}
	
	private void status() {
		System.out.println("Read = " + (isReadRequest ? "Ya" : "Nah") + "; Insert = " +
			(isInsertRequest ? "Ya" : "Nah") + "; Update = " + (isUpdateRequest ? "Ya" : "Nah") +
				"; Delete = " + (isDeleteRequest ? "Ya" : "Nah") + "; Deep = " + (isEntryLevelRequest ? "Ya" : "Nah"));
	}
	
	private void sendAccessDenied(HttpExchange ex) throws FileNotFoundException, IOException, WikiException
	{
		Exchange<HttpExchange> exchange = new HttpExchangeWrapper(ex);
		ServerResponseHandler responseHandler = new ServerResponseHandler(new HttpRequest(new RequestURL(exchange, ""), _users), "Access Denied");
		responseHandler.put(HtmlContentGenerator.ContentContent, new DatabaseAccessDeniedPrinter());
		exchange.send(HtmlTemplateDecorator.decorate(new BufferedReader(new FileReader(_formTemplate)), responseHandler));
	}

	public void updateAuthorizationListing(Vector<Authorization> authorizationListing) {
		this._authorizationListing = authorizationListing;
	}
}
