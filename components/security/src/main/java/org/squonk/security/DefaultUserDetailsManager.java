package org.squonk.security;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;

/**
 * Created by timbo on 04/01/16.
 */
public class DefaultUserDetailsManager implements UserDetailsManager {


    @Override
    public UserDetails getAuthenticatedUser(HttpServletRequest request) {
        Principal p = request.getUserPrincipal();
        if (p != null) {
            String username = p.getName();
            return new UserDetails(UserDetails.AUTHENTICATOR_SERVLET, username, null, null, null);
        }
        return null;
    }

    @Override
    public URI getLogoutUrl(HttpServletRequest request, String redirectTo) {
        return null;
    }
}
