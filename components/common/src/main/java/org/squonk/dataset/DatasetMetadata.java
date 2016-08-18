package org.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.types.BasicObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Metadata for a Dataset. Allows 3 main types of information to be managed.
 *
 * <p><b>1. Field data types</b></p>
 * <p>
 * The Classes of the dataset's data are manged using the valueClassMappings which is a Map keyed by field name whose
 * values are the Classes of the data for that field. Implicit in this is that all values for a single field must be of
 * the same data type. This is handled separately to other field metadata for practical and historical reasons.
 * </p>
 *
 * <p><b>2. Other Field metadata</b></p>
 * <p>Any other metadata relating to fields is handled by the fieldMetaProps Map. Values of this Map are a Map (keyed by
 * field name), and this Map has keys of property names and values of the value for that property. All values must be serializable
 * as JSON.</p>
 *
 * p><b>3. Metadata of the dataset as a whole</b></p>
 * <p>Metadata that related to the entire dataset (as opposed to fields in the dataset) are handled in the properties Map.
 * All values must be serializable as JSON.</p>
 *
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"type", "size", "valueClassMappings"})
public class DatasetMetadata<T extends BasicObject> {

    public static final String PROP_DISPLAY_NAME = "displayName";
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
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    private final Map<String, Object> properties = new LinkedHashMap<>();

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    private final Map<String, Map<String,Object>> fieldMetaProps = new LinkedHashMap<>();

    public static String formatDate() {
        return DATE_FORMAT.format(new Date());
    }

    public DatasetMetadata(
            @JsonProperty("type") Class<T> type,
            @JsonProperty("valueClassMappings") Map<String, Class> valueClassMappings,
            @JsonProperty("fieldMetaProps") Map<String, Map<String,Object>> fieldMetaProps,
            @JsonProperty("size") int size,
            @JsonProperty("properties") Map<String, Object> properties) {
        assert type != null;
        this.type = type;
        if (valueClassMappings != null && !valueClassMappings.isEmpty()){
            this.valueClassMappings.putAll(valueClassMappings);
        }
        if (fieldMetaProps != null && !fieldMetaProps.isEmpty()){
            this.fieldMetaProps.putAll(fieldMetaProps);
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

    public Map<String, Map<String,Object>> getFieldMetaProps() {
        return fieldMetaProps;
    }

    public Object putFieldMetaProp(String fieldName, String propertyName, Object value) {
        if (value == null) {
            return clearFieldMetaProp(fieldName, propertyName);
        }
        Map<String,Object> map = fieldMetaProps.get(fieldName);
        if (map == null) {
            map = new LinkedHashMap<>();
            fieldMetaProps.put(fieldName, map);
        }
        return map.put(propertyName, value);
    }

    public Object clearFieldMetaProp(String fieldName, String propertyName) {
        Map<String,Object> map = fieldMetaProps.get(fieldName);
        if (map == null) {
            return null;
        } else {
            return map.remove(propertyName);
        }
    }

    public Object getFieldMetaProp(String fieldName, String propertyName) {
        Map<String,Object> map = fieldMetaProps.get(fieldName);
        if (map == null) {
            return null;
        } else {
            return map.get(propertyName);
        }
    }

    public <S> S getFieldMetaProp(String fieldName, String propertyName, Class<S> type) {
        return (S)getFieldMetaProp(fieldName, propertyName);
    }

    /** Utility method that collects all the values for the specified property. The resulting map is keyed by field name.
     * NOTE: modifying the returned Map has no impact on the underlying Metadata
     *
     * @param propName
     * @return
     */
    public Map<String,Object> collectFieldMetaProps(String propName) {
        Map<String,Object> results = new LinkedHashMap<>();
        for (Map.Entry<String,Map<String,Object>> e: fieldMetaProps.entrySet()) {
            Map<String,Object> map = e.getValue();
            Object value = map.get(propName);
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

    public String now() {
        return DATE_FORMAT.format(new Date());
    }

    public String nowFormatted() {
        return "[" + now() + "] ";
    }

    public void appendFieldHistory(String fldName, String msg) {

        String old = (String)getFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY);
        if (old == null) {
            putFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY, nowFormatted() + msg);
        } else {
            putFieldMetaProp(fldName, DatasetMetadata.PROP_HISTORY, old + "\n" + nowFormatted() + msg);
        }
    }

    public void appendDatasetHistory(String msg) {
        String old = (String)getProperties().get(DatasetMetadata.PROP_HISTORY);
        if (old == null) {
            getProperties().put(DatasetMetadata.PROP_HISTORY, nowFormatted() + msg);
        } else {
            getProperties().put(DatasetMetadata.PROP_HISTORY, old + "\n" + nowFormatted() + msg);
        }
    }
}
