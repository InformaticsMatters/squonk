package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
public class ConvertFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final Class newType;
    private final Class genericType;

    protected ConvertFieldTransform(
            @JsonProperty("fieldName")String fieldName,
            @JsonProperty("newType")Class newType,
            @JsonProperty("genericType")Class genericType) {
        this.fieldName = fieldName;
        this.newType = newType;
        this.genericType = genericType;
    }

    protected ConvertFieldTransform(String fieldName, Class newType) {
        this(fieldName, newType, null);
    }


    public String getFieldName() {
        return fieldName;
    }
    
    public Class getNewType() {
        return newType;
    }

    public Class getGenericType() {
        return genericType;
    }
}
