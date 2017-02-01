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

    public IODescriptor(
            @JsonProperty("name") String name,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("primaryType") Class<P> primaryType,
            @JsonProperty("secondaryType") Class<Q> secondaryType
        ) {
        this.name = name;
        this.mediaType = mediaType;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
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


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("IODescriptor[")
                .append("name=").append(name)
                .append(" mediaType=").append(mediaType)
                .append(" primaryType=").append(primaryType)
                .append(" secondaryType=").append(secondaryType)
                .append("]");
        return b.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof IODescriptor) {
            IODescriptor other = (IODescriptor)o;
            return Utils.safeEqualsIncludeNull(name, other.getName())
                    && Utils.safeEqualsIncludeNull(mediaType, other.getMediaType())
                    && Utils.safeEqualsIncludeNull(primaryType, other.getPrimaryType())
                    && Utils.safeEqualsIncludeNull(secondaryType, other.getSecondaryType())
                    ;
        }
        return false;
    }
}
