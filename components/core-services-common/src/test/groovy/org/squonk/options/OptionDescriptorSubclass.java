package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by timbo on 18/03/16.
 */
public class OptionDescriptorSubclass extends OptionDescriptor<Integer> {

    OptionDescriptorSubclass(Class type, String key, String label, String description) {
        super(type, key, label, description);
    }

    @JsonCreator
    public OptionDescriptorSubclass(
            @JsonProperty("typeDescriptor") TypeDescriptor<Integer> typeDescriptor,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") Integer[] values,
            @JsonProperty("defaultValue") Integer defaultValue,
            @JsonProperty("visible") boolean visible,
            @JsonProperty("editable") boolean editable,
            @JsonProperty("minValues") Integer minValues,
            @JsonProperty("maxValues") Integer maxValues,
            @JsonProperty("properties") Map<String,Object> properties
    ) {
        super(typeDescriptor, key, label, description, values, defaultValue, visible, editable, minValues, maxValues, properties);
    }
}
