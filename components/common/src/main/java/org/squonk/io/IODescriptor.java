package org.squonk.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by timbo on 07/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IODescriptor implements Serializable {


    private final String name;
    private final String mediaType;
    private final Class primaryType;
    private final Class genericType;
    private final IOMultiplicity multiplicity;
    private final IORoute route;

    public IODescriptor(
            @JsonProperty("name") String name,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("primaryType") Class primaryType,
            @JsonProperty("genericType") Class genericType,
            @JsonProperty("multiplicity") IOMultiplicity multiplicity,
            @JsonProperty("route") IORoute route) {
        this.name = name;
        this.mediaType = mediaType;
        this.primaryType = primaryType;
        this.genericType = genericType;
        this.multiplicity = multiplicity;
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Class getPrimaryType() {
        return primaryType;
    }

    public Class getGenericType() {
        return genericType;
    }

    public IOMultiplicity getMultiplicity() {
        return multiplicity;
    }

    public IORoute getRoute() {
        return route;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("IODescriptor[")
                .append("name=").append(name)
                .append(" mediaType=").append(mediaType)
                .append(" primaryType=").append(primaryType)
                .append(" genericType=").append(genericType)
                .append(" multiplicity=").append(multiplicity)
                .append(" route=").append(route)
                .append("]");
        return b.toString();

    }
}
