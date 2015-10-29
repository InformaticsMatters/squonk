package com.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class DeleteFieldTransform extends AbstractTransform {

    private final String fieldName;

    protected DeleteFieldTransform(@JsonProperty("fieldName") String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

}
