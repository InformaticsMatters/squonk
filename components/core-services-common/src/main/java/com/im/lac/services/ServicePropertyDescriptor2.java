package com.im.lac.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/** 
 * Describes a property that needs to be defined in order to execute a service.
 * <br>e.g. the threshold for a similarity search
 * <br>e.g. the query structure for a structure search
 * 
 * NOTE - this is a prototype for how parameters should be handled. It is not yet in use.
 *
 * TODO - validation: can't use a method call for this as it may be specified from non-java
 * language. So maybe specify a regexp that can be used to validate the value (but this assumes it a
 * string) and also need to provide a reason why the value is invalid, or at least a description of
 * what types of values are valid
 *
 * TODO - specification of text: How to distinguish a short bit of text from multi-line text. Do we 
 * create a specific Text class that allows these sorts of things to be specified?
 *
 * TODO - molecules: how to handle aspects of molecules e.g. for query molecules implicit hydrogens
 * should not be displayed. Do we create a specific QueryMolecule class?
 *
 *
 * @author Tim Dudgeon
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServicePropertyDescriptor2<T> implements Serializable {

    public enum Visibility {

        EDITABLE, NON_EDITABLE, HIDDEN
    }

    private final Class<T> type;
    private final String key;
    private final String label;
    private final String description;
    private final T[] values;
    private final T defaultValue;
    private final boolean required;
    private final Visibility visibility;

    public ServicePropertyDescriptor2(
            @JsonProperty("type") Class<T> type,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") T[] values,
            @JsonProperty("defaultValue") T defaultValue,
            @JsonProperty("required") boolean required,
            @JsonProperty("visibility") Visibility visibility
    ) {
        this.type = type;
        this.key = key;
        this.label = label;
        this.description = description;
        this.values = values;
        this.defaultValue = defaultValue;
        this.required = required;
        this.visibility = visibility;
    }

    /**
     * The code name for the parameter. Will be returned as the key, with the user chosen value as
     * the value.
     *
     * @return
     */
    public String getkey() {
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

    public Class<T> getType() {
        return type;
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
     * Is it mandatory to specify a value, or is it optional
     *
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * How the parameter should appear in the UI.
     * <ul>
     * <li>EDITABLE - the parameter is visible and editable.</li>
     * <li>NON_EDITABLE - the parameter is visible but not editable. A default must be
     * specified.</li>
     * <li>HIDDEN - the parameter is not displayed but its value is returned in the results. A
     * default must be specified.</li>
     * </ul>
     *
     * @return
     */
    public Visibility getVisibility() {
        return visibility;
    }

}
