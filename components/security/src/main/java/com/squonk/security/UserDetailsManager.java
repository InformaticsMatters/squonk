package com.squonk.security;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author timbo
 */
public interface UserDetailsManager {

    /**
     * Get details of authenticated user whose details such as username, email that  
     * will have been populated through the authentication process.
     *
     * @param request
     * @return The details of the authenticated user, or null if not authenticated
     */
    UserDetails getAuthenticatedUser(HttpServletRequest request);
    
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
