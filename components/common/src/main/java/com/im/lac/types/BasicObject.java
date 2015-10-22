package com.im.lac.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple generic object that only has properties. Specialized types can be created by sub-classing
 * this class. For instance, see {@link MoleculeObject}.
 *
 * @author Tim Dudgeon
 */
@JsonIgnoreProperties({"value"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BasicObject implements Serializable {

    protected final UUID uuid;

    /**
     * Properties of the object.
     */
    protected Map<String, Object> values;

    public BasicObject(UUID uuid) {
        this.uuid = (uuid == null ? UUID.randomUUID() : uuid);
        this.values = new LinkedHashMap<>();
    }

    public BasicObject() {
        this(UUID.randomUUID());
    }

    public BasicObject(Map<String, Object> values) {
        this(null, values);
        
    }

    public BasicObject(UUID uuid, Map<String, Object> values) {
        this(uuid);
        if (values != null && !values.isEmpty()) {
            this.values.putAll(values);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public <T> T getValue(String key, Class<T> type) {
        return (T) values.get(key);
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    public void putValues(Map<String, Object> values) {
        this.values.putAll(values);
    }

}
