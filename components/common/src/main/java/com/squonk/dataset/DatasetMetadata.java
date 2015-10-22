package com.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.im.lac.types.BasicObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"type", "size", "valueClassMappings"})
public class DatasetMetadata<T extends BasicObject> {

    /**
     * The number of records present, or -1 if unknown
     */
    private int size = -1;

    /**
     * The type of the data items
     */
    private Class<T> type;

    /**
     * The names and types (java class) of the values in the items of the dataset
     */
    private final Map<String, Class> valueClassMappings;

    /**
     * Properties of the dataset as a whole (as opposed to properties or an individual item in the
     * dataset.
     * Note: the values must be serializable/deserializable to/from JSON by Jackson 
     *
     */
    private final Map<String, Object> properties;

    public DatasetMetadata(
            @JsonProperty("type") Class<T> type,
            @JsonProperty("valueClassMappings") Map<String, Class> valueClassMappings,
            @JsonProperty("size") int size,
            @JsonProperty("properties") Map<String, Object> properties) {
        this.type = type;
        this.valueClassMappings = valueClassMappings;
        this.size = size;
        this.properties = properties;
    }

    public DatasetMetadata(Class<T> type, Map<String, Class> valueClassMappings, int size) {
        this(type, valueClassMappings, size, new HashMap<>());
    }

    public DatasetMetadata(Class<T> type, Map<String, Class> valueClassMappings) {
        this(type, valueClassMappings, -1, new HashMap<>());
    }

    public DatasetMetadata(Class<T> type) {
        this(type, new HashMap<>(), -1, new HashMap<>());
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Class<T> getType() {
        return type;
    }

    public Map<String, Class> getValueClassMappings() {
        return valueClassMappings;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
