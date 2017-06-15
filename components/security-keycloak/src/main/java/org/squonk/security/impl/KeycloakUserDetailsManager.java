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

package org.squonk.security.impl;

import org.squonk.security.DefaultUserDetailsManager;
import org.squonk.security.UserDetails;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.representations.IDToken;

/**
 *
 * @author timbo
 */
public class KeycloakUserDetailsManager extends DefaultUserDetailsManager {

    private static final Logger LOG = Logger.getLogger(KeycloakUserDetailsManager.class.getName());

    @Override
    public UserDetails getAuthenticatedUser(HttpServletRequest request) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (session != null) {
            IDToken token = session.getIdToken();
            if (token != null) {
                UserDetails ud = new UserDetails(UserDetails.AUTHENTICATOR_KEYCLOAK, token.getPreferredUsername(), token.getEmail(), token.getGivenName(), token.getFamilyName());
                LOG.fine("Found authenticated user from Keycloak token: " + ud.toString());
                return ud;
            }
        }
        return super.getAuthenticatedUser(request);
    }

    @Override
    public URI getLogoutUrl(HttpServletRequest request, String redirectTo) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (session != null) {
            IDToken token = session.getIdToken();
            if (token != null) {
                String realm = session.getRealm();
                String issuer = token.getIssuer(); // https://192.168.59.103/auth/realms/samplerealm
                // ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH = "/realms/{realm-name}/protocol/openid-connect/logout";
                URI uri = KeycloakUriBuilder.fromUri(issuer).path("/protocol/openid-connect/logout").queryParam("redirect_uri", redirectTo).build(realm);

                // returns something like this:
                // https://192.168.59.103/auth/realms/samplerealm/protocol/openid-connect/logout?redirect_uri=http%3A%2F%2F192.168.59.103%3A8080%2Fsampleapp%2Findex.html
                return uri;
            }
        }
        return null;
    }

    @Override
    public Map<String,String> getSecurityHeaders(HttpServletRequest request) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (session != null) {
            String token = session.getTokenString();
            LOG.fine("Authorization header: " + token);
            return Collections.singletonMap("Authorization", "Bearer " + token);
        } else {
            return super.getSecurityHeaders(request);
        }
    }
}
