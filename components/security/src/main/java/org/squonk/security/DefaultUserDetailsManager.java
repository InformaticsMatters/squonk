package org.squonk.security;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;

/** Basic UserDetailsManager that uses the servlet API to provide minimal information about the user.
 * Authentication by the container is required.
 *
 * Created by timbo on 04/01/16.
 */
public class DefaultUserDetailsManager implements UserDetailsManager {


    /** Get info about the user from the request using HttpServletRequest.getUserPrincipal().getName() as the username.
     * Other information is faked as the Servlet API does not provide anything beyond the Principal.
     * However, HttpServletRequest.isUserInRole(String role) should still work correctly.
     *
     * @param request
     * @return
     */
    @Override
    public UserDetails getAuthenticatedUser(HttpServletRequest request) {
        Principal p = request.getUserPrincipal();
        if (p != null) {
            String username = p.getName();
            return new UserDetails(UserDetails.AUTHENTICATOR_SERVLET, username, username + "@nowhere.com", username, username);
        }
        return null;
    }

}
