package com.squonk.security;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author timbo
 */
public interface UserDetailsManager {

    /**
     * Get the authenticated user whose details such as username, email that will have been populated
     * through the authentication process.
     *
     * @param request
     * @return The details of the authenticated user, or null if not authenticated
     */
    UserDetails getAuthenticatedUser(HttpServletRequest request);

}
