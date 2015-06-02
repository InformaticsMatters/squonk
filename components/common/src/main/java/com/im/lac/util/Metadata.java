package com.im.lac.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds metadata about a set of objects.
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Metadata {

    public static final String FORMAT = "format";

    /**
     * The number of records present
     */
    @JsonProperty
    int size = 0;
    
    /**
     * The names and types (java class) of the properties
     */
    @JsonProperty
    Map<String, Class> propertyTypes = new HashMap<>();
    
    /**
     * metadata describing the dataset
     */
    @JsonProperty
    Map<String, Object> metaProps = new HashMap<>();

    public int getSize() {
        return size;
    }

    public Map<String, Class> getPropertyTypes() {
        return propertyTypes;
    }

    public Object getMetadata(String key) {
        return metaProps.get(key);
    }

    public <T> T getMetadata(String key, Class<T> cls) {
        return (T) metaProps.get(key);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Metadata [")
                .append("size=").append(size)
                .append(", #propertyTypes=").append(propertyTypes.size())
                .append(", #metaProps=").append(metaProps.size())
                .append("]");
        return b.toString();
    }

}
