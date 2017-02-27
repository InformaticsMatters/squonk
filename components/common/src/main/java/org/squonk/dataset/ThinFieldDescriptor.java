package org.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** Defines a field mapping for use in thin execution.
 * See the docs for the constructor for usage.
 *
 * Created by timbo on 22/02/17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ThinFieldDescriptor implements Serializable {

    private final String optionName;
    private final String fieldName;

    /**
     * The following combinations are supported in specifying what to include in the data that is sent:
     *
     * <dl>
     * <dt>fieldName is null, optionName is specified</dt>
     * <dd>A field of the specified name is retrieved from the input and written to the data that is sent using the same name</dd>
     * <dt>fieldName is specified, optionName is specified</dt>
     * <dd>A field of the specified name is retrieved from the input and written to the data that is sent using the name specified by fieldName</dd>
     * <dt>fieldName is specified, optionName is null</dt>
     * <dd>A field of the name specified by fieldName is retrieved from the input and written to the data that is sent using the same name</dd>
     * </dl>
     *
     *
     * @param fieldName The name of the field.
     * @param optionName The name of the option that provides the name of the field. The value is a String of the field name,
     *                   or an object whose toString() method provides the field name.
     */
    public ThinFieldDescriptor(@JsonProperty("fieldName") String fieldName, @JsonProperty("optionName") String optionName) {
        this.optionName = optionName;
        this.fieldName = fieldName;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ThinFieldDescriptor:[");
        if (fieldName != null) {
            b.append("fieldName:").append(fieldName);
        }
        if (optionName != null) {
            if (fieldName != null) {
                b.append(",");
            }
            b.append("optionName:").append(optionName);
        }
        b.append("]");
        return b.toString();
    }
}
