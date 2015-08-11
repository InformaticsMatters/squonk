package com.im.lac.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Describes a property that needs to be defined in order to execute the service. 
 * <br>e.g. the threshold for a similarity search
 * <br>e.g. the query structure for a structure search
 *
 * @author simetrias
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServicePropertyDescriptor implements Serializable {

    private Type type = Type.STRING;
    private String key;
    private String label;
    private String description;

    public ServicePropertyDescriptor(
            @JsonProperty("type") Type type,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description) {
        this.type = type;
        this.key = key;
        this.label = label;
        this.description = description;
    }
    
    public String getkey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {

        STRING,
        INTEGER,
        FLOAT,
        BOOLEAN,
        STRUCTURE

    }
}
