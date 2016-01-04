package org.squonk.security;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author timbo
 */
public interface UserDetailsManager {

    public static String USER_DETAILS_SESSION_ATTR = "org.squonk.security.UserDetailsManager#UserDetails";

    /**
     * Get details of the authenticated user whose details such as username, email that
     * will have been populated through the authentication process.
     *
     * @param request
     * @return The details of the authenticated user, or null if not authenticated
     */
    UserDetails getAuthenticatedUser(HttpServletRequest request);

    /** Get details of the authenticated user, caching the UserDetails in the session.
     *
     * @param request
     * @param cache whether to use the session as a cache
     */
    default UserDetails getAuthenticatedUser(HttpServletRequest request, boolean cache) {
        UserDetails user;
        if (cache) {
            user = (UserDetails)request.getSession().getAttribute(USER_DETAILS_SESSION_ATTR);
            if (user != null) {
                return user;
            }
        }
        user = getAuthenticatedUser(request);
        if (cache && user != null) {
            request.getSession().setAttribute(USER_DETAILS_SESSION_ATTR, user);
        }
        return user;
    }

    default void clearUserDetailsFromSession(HttpSession session) {
        session.removeAttribute(USER_DETAILS_SESSION_ATTR);
    }
    
    /**
     * Get the URI to use to log out the authenticated user. Following that URI will 
     * logout the user and redirect to the specified page (e.g. the login page).
     * If there is no authenticated user then null should be returned.
     * 
     * @param request
     * @param redirectTo The URI to redirect to once the logout is complete.
     * @return 
     */
    URI getLogoutUrl(HttpServletRequest request, String redirectTo);

}
