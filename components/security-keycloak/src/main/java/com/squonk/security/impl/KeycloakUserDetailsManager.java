package com.squonk.security.impl;

import com.squonk.security.UserDetails;
import com.squonk.security.UserDetailsManager;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.IDToken;

/**
 *
 * @author timbo
 */
public class KeycloakUserDetailsManager implements UserDetailsManager {

    @Override
    public UserDetails getAuthenticatedUser(HttpServletRequest request) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        
        IDToken token = session.getIdToken();
        if (token == null) {
            return null;
        }
        
        return new UserDetails(token.getPreferredUsername(), token.getEmail(), token.getGivenName(), token.getFamilyName());
    }

    public URI getLogoutUrl(HttpServletRequest request, String redirectTo) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        String realm = session.getRealm();
        String basePath = "https://192.168.59.103/auth"; // How to get this?
        URI uri = KeycloakUriBuilder.fromUri(basePath).path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).queryParam("redirect_uri", redirectTo).build(realm);
        return uri;
    }
}
