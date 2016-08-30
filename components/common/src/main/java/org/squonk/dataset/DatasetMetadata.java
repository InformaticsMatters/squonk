package org.squonk.dataset;

import com.fasterxml.jackson.annotation.*;
import org.squonk.types.BasicObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Metadata for a Dataset. Allows 3 main types of information to be managed.
 * <p>
 * <p><b>1. Field data types</b></p>
 * <p>
 * The Classes of the dataset's data are manged using the valueClassMappings which is a Map keyed by field name whose
 * values are the Classes of the data for that field. Implicit in this is that all values for a single field must be of
 * the same data type. This is handled separately to other field metadata for practical and historical reasons.
 * </p>
 * <p>
 * <p><b>2. Other Field metadata</b></p>
 * <p>Any other metadata relating to fields is handled by the fieldMetaProps Map. Keys of this Map are the field names)
 * and the values are a PropertiesHolder which is essentially a wrapper around a Map that allows type information to handled
 * in JSON. The keys of this inner Map are the property names and values are the value for that property. All values must be
 * serializable as JSON.</p>
 * <p>
 * p><b>3. Metadata of the dataset as a whole</b></p>
 * <p>Metadata that related to the entire dataset (as opposed to fields in the dataset) are handled in the properties Map.
 * All values must be serializable as JSON.</p>
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"type", "size", "valueClassMappings"})
public class DatasetMetadata<T extends BasicObject> implements Serializable {

    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_SOURCE = "source";
    public static final String PROP_CREATED = "created";
    public static final String PROP_HISTORY = "history";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

    /**
     * The number of records present, or -1 if unknown
     */
    private int size = -1;

    /**
     * The type of the data items. e.g. BasicObject, MoleculeObject ...
     */
    private Class<T> type;

    /**
     * The names and types (java class) of the values in the items of the dataset
     */
    private final Map<String, Class> valueClassMappings = new LinkedHashMap<>();

    /**
     * Properties of the dataset as a whole (as opposed to properties or an individual item in the
     * dataset.
     * Note: the values must be serializable/deserializable to/from JSON by Jackson
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private final Map<String, Object> properties = new LinkedHashMap<>();

    //@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    private final Map<String, PropertiesHolder> fieldMetaProps = new LinkedHashMap<>();

    public static String formatDate() {
        return DATE_FORMAT.format(new Date());
    }

    public DatasetMetadata(
            @JsonProperty("type") Class<T> type,
            @JsonProperty("valueClassMappings") Map<String, Class> valueClassMappings,
            @JsonProperty("fieldMetaProps") List<PropertiesHolder> fieldMetaProps,
            @JsonProperty("size") int size,
            @JsonProperty("properties") Map<String, Object> properties) {
        assert type != null;
        this.type = type;
        if (valueClassMappings != null && !valueClassMappings.isEmpty()) {
            this.valueClassMappings.putAll(valueClassMappings);
        }
        if (fieldMetaProps != null && !fieldMetaProps.isEmpty()) {
            for (PropertiesHolder h : fieldMetaProps) {
                this.fieldMetaProps.put(h.getFieldName(), h);
            }
        }
        this.size = size;
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    public DatasetMetadata(
            Class<T> type,
            Map<String, Class> valueClassMappings,
            int size,
            Map<String, Object> properties) {
        this(type, valueClassMappings, null, size, properties);
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

    public Object getProperty(String propName) {
        return properties.get(propName);
    }

    public List<PropertiesHolder> getFieldMetaProps() {
        return new ArrayList(fieldMetaProps.values());
    }

    @JsonIgnore
    public Map<String, PropertiesHolder> getFieldMetaPropsMap() {
        return fieldMetaProps;
    }

    /** Helper method for adding a new field
     *
     * @param name
     * @param source
     * @param description
     * @param type
     * @return The formatted date that can be used for adding addition properties
     */
    public String createField(String name, String source, String description, Class type) {
        String now = now();
        putFieldMetaProp(name, DatasetMetadata.PROP_CREATED, now);
        if (type != null) { getValueClassMappings().put(name, type); }
        if (source != null) { putFieldMetaProp(name, DatasetMetadata.PROP_SOURCE, source); }
        if (description != null) { putFieldMetaProp(name, DatasetMetadata.PROP_DESCRIPTION, description); }
        appendDatasetHistory("Added field " + name);

        return now;
    }


    public Object putFieldMetaProp(String fieldName, String propertyName, Object value) {
        if (value == null) {
            return clearFieldMetaProp(fieldName, propertyName);
        }
        PropertiesHolder h = getOrCreatePropertiesHolder(fieldName);
        return h.putValue(propertyName, value);
    }

    public Object clearFieldMetaProp(String fieldName, String propertyName) {
        PropertiesHolder h = fieldMetaProps.get(fieldName);
        if (h == null) {
            return null;
        } else {
            return h.removeValue(propertyName);
        }
    }

    public Object getFieldMetaProp(String fieldName, String propertyName) {
        PropertiesHolder h = fieldMetaProps.get(fieldName);
        if (h == null) {
            return null;
        } else {
            return h.getValue(propertyName);
        }
    }

    public <S> S getFieldMetaProp(String fieldName, String propertyName, Class<S> type) {
        return (S) getFieldMetaProp(fieldName, propertyName);
    }

    /**
     * Utility method that collects all the values for the specified property. The resulting map is keyed by field name.
     * NOTE: modifying the returned Map has no impact on the underlying Metadata
     *
     * @param propName
     * @return
     */
    public Map<String, Object> collectFieldMetaProps(String propName) {
        Map<String, Object> results = new LinkedHashMap<>();
        for (Map.Entry<String, PropertiesHolder> e : fieldMetaProps.entrySet()) {
            PropertiesHolder h = e.getValue();
            Object value = h.getValue(propName);
            if (value != null) {
                results.put(e.getKey(), value);
            }
        }
        return results;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("DatasetMetadata [size:").append(size).append(" type:").append(type == null ? "null" : type.getName()).append(" properties:[");
        int count = 0;
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(":").append(e.getValue());
        }
        b.append("] valueClassMappings:[");
        count = 0;
        for (Map.Entry<String, Class> e : valueClassMappings.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(":").append(e.getValue().getName());
        }
        b.append("]]");
        return b.toString();
    }

    private void appendMap(StringBuilder b, Map<? extends Object, ? extends Object> map) {

        int count = 0;
        for (Map.Entry<? extends Object, ? extends Object> e : map.entrySet()) {
            if (count > 0) {
                b.append(" ");
            }
            count++;
            b.append(e.getKey()).append(":").append(e.getValue());
        }
    }

    public static String now() {
        return DATE_FORMAT.format(new Date());
    }

    public void appendDatasetHistory(String msg) {
        appendDatasetHistory(msg, now());
    }

    public void appendDatasetHistory(String msg, String now) {

        String old = (String) properties.get(DatasetMetadata.PROP_HISTORY);
        if (old == null) {
            properties.put(DatasetMetadata.PROP_HISTORY, "[" + now + "] " + msg);
        } else {
            properties.put(DatasetMetadata.PROP_HISTORY, old + "\n" + "[" + now + "] " + msg);
        }
    }

    public void appendFieldHistory(String fldName, String msg) {
        appendFieldHistory(fldName, msg, now());
    }

    public void appendFieldHistory(String fldName, String msg, String now) {

        String old = (String) getFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY);
        if (old == null) {
            putFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY, "[" + now + "] " + msg);
        } else {
            putFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY, old + "\n" + "[" + now + "] " + msg);
        }
    }

    public void concatDatasetProperty(String propName, Object value) {

        Object old = properties.get(propName);
        if (old == null) {
            properties.put(propName, value);
        } else {
            properties.put(propName, old + "\n" + value);
        }
    }

    public void concatFieldProperty(String fldName, String propName, Object value) {

        Object old = getFieldMetaProp(fldName, propName);
        if (old == null) {
            putFieldMetaProp(fldName, propName, value);
        } else {
            putFieldMetaProp(fldName, propName, old + "\n" + value);
        }
    }

    /** Merge these properties assuming they are existing ones and correctly formatted
     *
     * @param propName
     * @param value
     */
    public void mergeDatasetProperty(String propName, Object value) {

        if (value == null) {
            return;
        }

        // handle the well known properties
        if (PROP_HISTORY.equals(propName) || PROP_DESCRIPTION.equals(propName)) {
            concatDatasetProperty(propName, value);
            return;
        }

        handleMerge(properties, propName, value);
    }

    /** Append these properties assuming the value isa  new property and might need formatting
     *
     * @param propName
     * @param value
     */
    public void appendDatasetProperty(String propName, Object value) {

        if (value == null) {
            return;
        }

        // handle the well known properties
        if (PROP_HISTORY.equals(propName)) {
            appendDatasetHistory(value.toString());
            return;
        } else if (PROP_DESCRIPTION.equals(propName)) {
            concatDatasetProperty(propName, value);
            return;
        }

        handleMerge(properties, propName, value);
    }

    public void mergeFieldProperty(String fieldName, String propName, Object value) {

        if (value == null) {
            return;
        }

        // handle the well known properties
        if (PROP_HISTORY.equals(propName) || PROP_DESCRIPTION.equals(propName)) {
            concatFieldProperty(fieldName, propName, value);
            return;
        }

        PropertiesHolder h = getOrCreatePropertiesHolder(fieldName);
        handleMerge(h.getValues(), propName, value);
    }

    public void appendFieldProperty(String fieldName, String propName, Object value) {

        if (value == null) {
            return;
        }

        // handle the well known properties
        if (PROP_HISTORY.equals(propName)) {
            appendFieldHistory(fieldName, value.toString());
            return;
        } else if (PROP_DESCRIPTION.equals(propName)) {
            concatFieldProperty(fieldName, propName, value);
            return;
        }

        PropertiesHolder h = getOrCreatePropertiesHolder(fieldName);
        handleMerge(h.getValues(), propName, value);
    }

    private PropertiesHolder getOrCreatePropertiesHolder(String fieldName) {
        PropertiesHolder h = fieldMetaProps.get(fieldName);
        if (h == null) {
            h = new PropertiesHolder(fieldName);
            fieldMetaProps.put(fieldName, h);
        }
        return h;
    }

    private void handleMerge(Map<String,Object> map, String propName, Object value) {
        Object old = map.get(propName);

        if (old == null) {
            map.put(propName, value);
        } else {
            if (old instanceof String) {
                map.put(propName, old + "\n" + value);
            } else if (old instanceof Integer) {
                if (value instanceof Integer) {
                    map.put(propName, ((Number)old).intValue() + ((Number)value).intValue());
                } else if (value instanceof Float) {
                    map.put(propName, ((Number)old).floatValue() + ((Number)value).floatValue());
                } else if (value instanceof Double) {
                    map.put(propName, ((Number)old).doubleValue() + ((Number)value).doubleValue());
                } else {
                    map.put(propName, old + "\n" + value);
                }
            } else if (old instanceof Float) {
                if (value instanceof Double) {
                    map.put(propName, ((Number)old).doubleValue() + ((Number)value).doubleValue());
                } else if (value instanceof Number) {
                    map.put(propName, ((Number)old).floatValue() + ((Number)value).floatValue());
                } else {
                    map.put(propName, old + "\n" + value);
                }
            } else if (old instanceof Double) {
                if (value instanceof Number) {
                    map.put(propName, ((Number)old).doubleValue() + ((Number)value).doubleValue());
                } else {
                    map.put(propName, old + "\n" + value);
                }
            } else {
                map.put(propName, old + "\n" + value);
            }
        }
    }

    public static class PropertiesHolder implements Serializable {

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        private final Map<String, Object> values = new LinkedHashMap<>();
        private final String fieldName;

        public PropertiesHolder(
                @JsonProperty("fieldName") String fieldName,
                @JsonProperty("values") Map<String, Object> values) {
            this(fieldName);
            if (values != null) {
                this.values.putAll(values);
            }
        }

        public PropertiesHolder(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getValue(String name) {
            return values.get(name);
        }

        public Object putValue(String name, Object value) {
            return values.put(name, value);
        }

        public Object removeValue(String name) {
            return values.remove(name);
        }

        public Map<String, Object> getValues() {
            return values;
        }

    }
}
