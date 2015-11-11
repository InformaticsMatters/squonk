package com.squonk.security;

import java.io.Serializable;

/** Represents details of a user
 * Additional properties might be added later
 *
 * @author timbo
 */
public class UserDetails implements Serializable {

    private final String userid;
    private final String email;
    private final String firstName;
    private final String lastName;
    
    public UserDetails(String userid, String email, String firstName, String lastName) {
        this.userid = userid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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
