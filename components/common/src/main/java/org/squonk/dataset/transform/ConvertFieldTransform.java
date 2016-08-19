package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConvertFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final Class newType;
    private final Class genericType;
    private final String onError;

    protected ConvertFieldTransform(
            @JsonProperty("fieldName")String fieldName,
            @JsonProperty("newType")Class newType,
            @JsonProperty("genericType")Class genericType,
            @JsonProperty("onError")String onError) {
        this.fieldName = fieldName;
        this.newType = newType;
        this.genericType = genericType;
        this.onError = onError;
    }

    protected ConvertFieldTransform(String fieldName, Class newType) {
        this(fieldName, newType, null, "fail");
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

    public String getOnError() {
        return onError;
    }
}
