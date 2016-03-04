package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransformValueTransform extends AbstractTransform {

    private final String fieldName;
    private final Object match;
    private final Object result;

    protected TransformValueTransform(
            @JsonProperty("fieldName") String fieldName,
            @JsonProperty("match") Object match,
            @JsonProperty("result") Object result) {
        this.fieldName = fieldName;
        this.match = match;
        this.result = result;
    }


    public String getFieldName() {
        return fieldName;
    }
    
    public Object getMatch() {
        return match;
    }

    public Object getResult() {
        return result;
    }
}
