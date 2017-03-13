package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.squonk.util.CommonConstants.*;

/**
 * Describes an option that needs to be defined in order to execute a service.
 * <br>e.g. the threshold for a similarity search
 * <br>e.g. the query structure for a structure search
 * <p>
 * TODO - validation: Maybe specify a regexp that can be used to validate the value (but this assumes it a
 * string) and also need to provide a reason why the value is invalid, or at least a description of
 * what types of values are valid
 * <p>
 * This class uses the JsonTypeInfo mechanism to allow subclasses to be used and marshalled/unmarshalled to/from json.
 * Also, the "properties" property provides a generic way for subclasses to persist their custom properties.
 * These custom properties are persisted automatically avoiding the need for subclasses to need to deal with persistence.
 * All that should be needed in simple cases is to read and write any custom properties to this properties map and the sub-class
 * will be marshalled/unmarshalled correctly. You must however implement the full arg form of the constructor.
 * Of course, if you need something more complex then you can define your own persistence using the standard mechanisms
 * Jackson provides.
 *
 * @author Tim Dudgeon
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class OptionDescriptor<T> implements Serializable {

    public static OptionDescriptor FILTER_MODE = new OptionDescriptor<>(String.class,
            "query." + OPTION_FILTER_MODE, "Filter mode", "How to filter results", Mode.User)
            .withValues(new String[]{VALUE_INCLUDE_PASS, VALUE_INCLUDE_FAIL, VALUE_INCLUDE_ALL})
            .withDefaultValue(VALUE_INCLUDE_PASS)
            .withMinMaxValues(1, 1);

    public enum Mode {
        User, Advanced, Input, Output, Ignore
    }

    private final TypeDescriptor<T> typeDescriptor;
    private final String key;
    private String label;
    private String description;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private T[] values;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private T defaultValue;
    private boolean editable;
    private boolean visible;
    private Integer minValues;
    private Integer maxValues;
    private Mode[] modes;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    /**
     * Full constructor.
     *
     * @param typeDescriptor The TypeDescriptor of the option
     * @param key            The code name of the option. Must be unique among a set of options.
     * @param label          The name of the option as displayed to the user.
     * @param description    A description of the option e.g. to be displayed as a tooltip.
     * @param values         A list of legal values. If null then any value us allowed
     * @param defaultValue   The default value. Can be null.
     * @param visible        Is the option visible to the user.
     * @param editable       Can the user edit the option (if not then a default would normally be specified)
     * @param minValues      The minimum number of values. If 0 then the option is optional. If 1 it is required. If greater than
     *                       1 then at least this many values need to be specified. If null then assumed to be 1.
     * @param maxValues      The maximum number of values. If null then any number are allowed.
     * @param modes
     * @param properties     Any custom properties needed by subclasses
     */
    public OptionDescriptor(
            @JsonProperty("typeDescriptor") TypeDescriptor<T> typeDescriptor,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") T[] values,
            @JsonProperty("defaultValue") T defaultValue,
            @JsonProperty("visible") boolean visible,
            @JsonProperty("editable") boolean editable,
            @JsonProperty("minValues") Integer minValues,
            @JsonProperty("maxValues") Integer maxValues,
            @JsonProperty("modes") Mode[] modes,
            @JsonProperty("properties") Map<String, Object> properties
    ) {
        this.typeDescriptor = typeDescriptor;
        this.key = key;
        this.label = label;
        this.description = description;
        this.values = values;
        this.defaultValue = defaultValue;
        this.visible = visible;
        this.editable = editable;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.modes = (modes == null ? new Mode[]{Mode.User} : modes);
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    public OptionDescriptor(
            TypeDescriptor<T> typeDescriptor,
            String key,
            String label,
            String description,
            T[] values,
            T defaultValue,
            boolean visible,
            boolean editable,
            Integer minValues,
            Integer maxValues,
            Mode mode) {
        this(typeDescriptor, key, label, description, values, defaultValue, visible, editable, minValues, maxValues, new Mode[]{mode}, null);
    }

    public OptionDescriptor(TypeDescriptor<T> type, String key, String label, String description, Mode mode) {
        this(type, key, label, description, null, null, true, true, 1, null, mode);
    }

    /**
     * Create an OptionDescriptor whose typeDescriptor is a {@link SimpleTypeDescriptor&lt;T&gt;}
     *
     * @param type
     * @param key
     * @param label
     * @param description
     * @param mode
     */
    public OptionDescriptor(Class<T> type, String key, String label, String description, Mode mode) {
        this(new SimpleTypeDescriptor<T>(type), key, label, description, null, null, true, true, 1, null, mode);
    }

    public OptionDescriptor<T> withDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public OptionDescriptor<T> withValues(T[] values) {
        this.values = values;
        return this;
    }

    public OptionDescriptor<T> withAccess(boolean visible, boolean editable) {
        this.visible = visible;
        this.editable = editable;
        return this;
    }

    public OptionDescriptor<T> withMinValues(int minValues) {
        this.minValues = minValues;
        return this;
    }

    public OptionDescriptor<T> withMaxValues(int maxValues) {
        this.maxValues = maxValues;
        return this;
    }

    public OptionDescriptor<T> withMinMaxValues(int minValues, int maxValues) {
        this.minValues = minValues;
        this.maxValues = maxValues;
        return this;
    }

    /**
     * The code name for the parameter. Will be returned as the key, with the user chosen value as
     * the value.
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * The label do use to identify the parameter in the UI
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * A description of the parameter to use as a tooltip in the UI
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    public TypeDescriptor<T> getTypeDescriptor() {
        return typeDescriptor;
    }

    /**
     * A list of legal values. Unless {@link #getDefaultValue} is specified the first option is the
     * default.
     *
     * @return
     */
    public T[] getValues() {
        return values;
    }

    /**
     * The default value to use. If {@link #getValues} is specified must be one of those values)
     *
     * @return
     */
    public T getDefaultValue() {
        return defaultValue;
    }


    /**
     * Is the value visible to the user.
     *
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Is the value editable.
     *
     * @return
     */
    public boolean isEditable() {
        return editable;
    }

    public Integer getMinValues() {
        return minValues;
    }

    public Integer getMaxValues() {
        return maxValues;
    }

    public Mode[] getModes() {
        return modes;
    }

    /**
     * Get the custom properties of this descriptor.
     *
     * @return
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonIgnore
    protected Object getProperty(String name) {
        return properties.get(name);
    }

    @JsonIgnore
    protected <P> P getProperty(String name, Class<P> cls) {
        return (P) properties.get(name);
    }

    @JsonIgnore
    protected void putProperty(String name, Object value) {
        properties.put(name, value);
    }

    @JsonIgnore
    public boolean isRequired() {
        return minValues == null || minValues > 0;
    }

    @JsonIgnore
    public boolean isMode(Mode mode) {
        for (Mode m : getModes()) {
            if (mode == m) {
                return true;
            }
        }
        return false;
    }


}
