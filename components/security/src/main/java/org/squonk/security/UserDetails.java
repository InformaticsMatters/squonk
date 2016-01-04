package org.squonk.security;

import java.io.Serializable;

/** Represents details of a user
 * Additional properties might be added later
 *
 * @author timbo
 */
public class UserDetails implements Serializable {

    public static final String AUTHENTICATOR_SERVLET = "servlet";
    public static final String AUTHENTICATOR_KEYCLOAK = "keycloak";

    private final String authenticator;
    private final String userid;
    private final String email;
    private final String firstName;
    private final String lastName;

    
    public UserDetails(String authenticator, String userid, String email, String firstName, String lastName) {
        this.authenticator = authenticator;
        this.userid = userid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getAuthenticator() {
        return authenticator;
    }

    /**
     * Get the string that's used to identify the user
     * @return 
     */
    public String getUserid() {
        return userid;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

}
