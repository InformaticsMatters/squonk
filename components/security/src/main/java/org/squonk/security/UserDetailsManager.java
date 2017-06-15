/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.security;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author timbo
 */
public interface UserDetailsManager {

    String USER_DETAILS_SESSION_ATTR = "org.squonk.security.UserDetailsManager#UserDetails";

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
     * @param cache whether to use the session as a cache for the generated UserDetails.
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

    /** Removed the cached UserDetails from the session.
     *
     * @param session
     */
    default void clearUserDetailsFromSession(HttpSession session) {
        session.removeAttribute(USER_DETAILS_SESSION_ATTR);
    }
    
    /**
     * Get the URI to use to log out the authenticated user. Following that URI will 
     * logout the user and redirect to the specified page (e.g. the login page).
     * This default implementation returns null. Subclasses might override to provide something different
     * 
     * @param request
     * @param redirectTo The URI to redirect to once the logout is complete.
     * @return 
     */
    default URI getLogoutUrl(HttpServletRequest request, String redirectTo) {
        return null;
    }

    Map<String,String> getSecurityHeaders(HttpServletRequest request);

}
