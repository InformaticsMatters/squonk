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

    protected DeleteFieldTransform(@JsonProperty("fieldName") String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

}
