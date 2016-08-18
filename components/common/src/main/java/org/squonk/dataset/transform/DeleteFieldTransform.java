package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeleteFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final String condition;

    protected DeleteFieldTransform(@JsonProperty("fieldName") String fieldName,
                                   @JsonProperty("condition") String condition) {
        this.fieldName = fieldName;
        this.condition = condition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getCondition() {
        return condition;
    }
}
