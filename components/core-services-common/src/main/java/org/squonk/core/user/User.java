/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squonk.core.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User implements Serializable {

    private final Long id;
    private final String username;

    public User(
            @JsonProperty("id") Long id,
            @JsonProperty("username") String username
    ) {
        this.id = id;
        this.username = username;
    }
    
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}
