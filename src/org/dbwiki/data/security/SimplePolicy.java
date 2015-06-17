package org.dbwiki.data.security;

import org.dbwiki.exception.WikiFatalException;
import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;
import org.dbwiki.web.request.parameter.RequestParameter;
import org.dbwiki.web.request.parameter.RequestParameterList;
import org.dbwiki.web.server.DatabaseWikiProperties;
import org.dbwiki.web.server.WikiServerConstants;

// FIXME: Split into server-wide and DatabaseWiki-specific policies
public class SimplePolicy implements WikiServerConstants, Policy {

	protected int _authenticationMode;
    
    
    public SimplePolicy(int mode) {
    	_authenticationMode = mode;
    }
    
    public int getAuthenticationMode() {
    	return _authenticationMode;
    }
    
    public void setAuthenticationMode(int mode) {
    	_authenticationMode = mode;
    }
    
    // TODO: Check that this is restrictive enough, e.g. disallows going up in the file system outside
    // the server root dir
	public boolean allowedFileRequest(Exchange<?> exchange) {
		// If the request is for a file (as specified by the 
    	// regular expressions above), then no authorization is
        // required.
		for (String m : fileMatches) {
			if (exchange.getRequestURI().getPath().matches(m)) 
				return true;
    	}
		return false;
    }
	
	/* TODO: Would be nice to define this as a method of Exchange */
	protected static boolean isUpdateRequest(Exchange<?> exchange) {
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

    protected static boolean isDeleteRequest(Exchange<?> exchange) {
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
    
    

    protected static boolean isInsertRequest(Exchange<?> exchange) {
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
    protected static boolean isProtectedRequest(Exchange<?> exchange) {
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

   
    
   
    /** Check a request by user uname to do request specified by exchange */
    public boolean checkRequest(User user,Exchange<?> exchange) {
    	if(user.is_admin()) { 
    		return true; 
    	}     	// if no rule matches, deny access
    	return false; 
    }
    
    public boolean isControlledRequest(Exchange <?> exchange) {
    	return ((_authenticationMode == DatabaseWikiProperties.AuthenticateAlways) ||
    			(_authenticationMode == DatabaseWikiProperties.AuthenticateWriteOnly) 
    			&& isProtectedRequest(exchange));
    }
   


    
    
}
