package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by timbo on 15/01/16.
 */
public class SimpleTypeDescriptor<T> implements Serializable, TypeDescriptor<T> {

    private Class<T> type;

    public SimpleTypeDescriptor(@JsonProperty("type") Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }


    public void putOptionValue(Map<String, Object> options, String key, T value) {
        options.put(key, value);
    }

    public T readOptionValue(Map<String, Object> options, String key) {
        return (T)options.get(key);
    }
}
