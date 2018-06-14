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

import java.io.Serializable;
import java.util.Set;

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
    private final Set<String> roles;

    
    public UserDetails(String authenticator, String userid, String email, String firstName, String lastName, Set<String> roles) {
        this.authenticator = authenticator;
        this.userid = userid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
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

    public Set<String> getRoles() {
        return roles;
    }

    /** Get the name to display for the user, determined as best we can based on what is present as firstName, lastName and userid
     *
     * @return Human friendly user name
     */
    public String getDisplayName() {
        if (firstName == null && lastName == null) {
            return getUserid();
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("UserDetails [")
                .append("userid:").append(userid)
                .append(" firstName:").append(firstName)
                .append(" lastName:").append(lastName)
                .append(" email:").append(email)
                .append(" authenticator:").append(authenticator)
                .append("]");
        return b.toString();
    }
}
