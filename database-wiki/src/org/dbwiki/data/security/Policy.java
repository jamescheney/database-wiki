package org.dbwiki.data.security;


import org.dbwiki.user.User;
import org.dbwiki.web.request.Exchange;

// FIXME: Maybe this should not depend on Exchange<?> ?

public interface Policy {

	boolean checkRequest(User user, Exchange<?> exchange);

	boolean allowedFileRequest(Exchange<?> exchange);

	boolean isControlledRequest(Exchange<?> exchange);

}
