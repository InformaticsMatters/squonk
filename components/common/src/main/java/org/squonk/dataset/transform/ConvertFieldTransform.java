package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class ConvertFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final Class newType;

    protected ConvertFieldTransform(@JsonProperty("fieldName")String fieldName, @JsonProperty("newType")Class newType) {
        this.fieldName = fieldName;
        this.newType = newType;
    }

    public String getFieldName() {
        return fieldName;
    }
    
    public Class getNewType() {
        return newType;
    }

}
