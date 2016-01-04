package org.squonk.security.impl

import org.keycloak.KeycloakSecurityContext
import org.keycloak.representations.IDToken
import org.squonk.security.UserDetails
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import java.security.Principal

/**
 * Created by timbo on 04/01/16.
 */
class KeycloakUserDetailsManagerSpec extends Specification {

    void "keycloak token"() {

        setup:
        IDToken token = Mock()
        token.getPreferredUsername() >> 'squonk'
        token.getEmail() >> 'squonk@somewhere.com'
        token.getGivenName() >> 'the'
        token.getFamilyName() >> 'squonk'
        token.getIssuer() >> 'https://keycloak/auth/realms/squonk'

        KeycloakSecurityContext context = Mock()
        context.getIdToken() >> token
        context.getRealm() >> 'squonk'

        HttpServletRequest request = Mock()
        request.getAttribute(_) >> context


        when:
        KeycloakUserDetailsManager manager = new KeycloakUserDetailsManager()
        UserDetails user = manager.getAuthenticatedUser(request)
        String logout = manager.getLogoutUrl(request, "http://nowhere.com/diequietly")

        then:
        user != null
        user.authenticator == UserDetails.AUTHENTICATOR_KEYCLOAK
        user.userid == 'squonk'
        user.firstName == 'the'
        user.lastName == 'squonk'
        user.email == 'squonk@somewhere.com'

        logout == 'https://keycloak/auth/realms/squonk/protocol/openid-connect/logout?redirect_uri=http%3A%2F%2Fnowhere.com%2Fdiequietly'
    }

    void "fallback"() {

        setup:
        Principal principal = Mock()
        principal.getName() >> 'squonk'
        HttpServletRequest request = Mock()
        request.getUserPrincipal() >> principal

        when:
        KeycloakUserDetailsManager manager = new KeycloakUserDetailsManager()
        UserDetails user = manager.getAuthenticatedUser(request)
        String logout = manager.getLogoutUrl(request, "http://nowhere.com/diequietly")

        then:
        user != null
        user.authenticator == UserDetails.AUTHENTICATOR_SERVLET
        user.userid == 'squonk'
        user.firstName == null
        user.lastName == null
        user.email == null

        logout == null
    }
}
