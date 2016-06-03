package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by timbo on 15/01/16.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface TypeDescriptor<T> extends Serializable {

    Class<T> getType();

    void putOptionValue(Map<String, Object> options, String key, T value);

    T readOptionValue(Map<String, Object> options, String key);

}
