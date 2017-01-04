package org.squonk.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.util.Utils;

import java.io.Serializable;

/**
 * Created by timbo on 07/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IODescriptor<P,Q> implements Serializable {

    private final String name;
    private final String mediaType;
    private final Class<P> primaryType;
    private final Class<Q> secondaryType;
    private final IOMultiplicity multiplicity;

    public IODescriptor(
            @JsonProperty("name") String name,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("primaryType") Class<P> primaryType,
            @JsonProperty("secondaryType") Class<Q> secondaryType,
            @JsonProperty("multiplicity") IOMultiplicity multiplicity
        ) {
        this.name = name;
        this.mediaType = mediaType;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
        this.multiplicity = multiplicity;
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Class<P> getPrimaryType() {
        return primaryType;
    }

    public Class<Q> getSecondaryType() {
        return secondaryType;
    }

    public IOMultiplicity getMultiplicity() {
        return multiplicity;
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("IODescriptor[")
                .append("name=").append(name)
                .append(" mediaType=").append(mediaType)
                .append(" primaryType=").append(primaryType)
                .append(" secondaryType=").append(secondaryType)
                .append(" multiplicity=").append(multiplicity)
                .append("]");
        return b.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IODescriptor) {
            IODescriptor other = (IODescriptor)o;
            return Utils.safeEquals(name, other.getName())
                    && Utils.safeEquals(mediaType, other.getMediaType())
                    && Utils.safeEquals(primaryType, other.getPrimaryType())
                    && Utils.safeEquals(secondaryType, other.getSecondaryType())
                    && Utils.safeEquals(mediaType, other.getMediaType())
                    && Utils.safeEquals(multiplicity, other.getMultiplicity())
                    ;
        }
        return false;
    }
}
