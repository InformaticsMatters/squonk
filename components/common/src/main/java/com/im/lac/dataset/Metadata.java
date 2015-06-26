package com.im.lac.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds metadata about a set of data items.
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Metadata {

    public enum Type {

        ITEM, ARRAY, TEXT
    }

    public static final String FORMAT = "format";

    /**
     * The type of record
     */
    @JsonProperty
    Type type;

    /**
     * The java class for the item, or the items in the array.
     */
    @JsonProperty
    String className;

    /**
     * The number of records present (if Type is ARRAY)
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

    public Metadata() {

    }

    public Metadata(String className, Type type, int size, Map<String, Object> metaProps, Map<String, Class> propertyTypes) {
        this(className, type, size);
        this.metaProps = metaProps;
        this.propertyTypes = propertyTypes;
    }

    public Metadata(String className, Type type, int size) {
        this.className = className;
        this.type = type;
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
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
                .append(", type=").append(type)
                .append(", #propertyTypes=").append(propertyTypes.size())
                .append(", #metaProps=").append(metaProps.size())
                .append("]");
        return b.toString();
    }

}
