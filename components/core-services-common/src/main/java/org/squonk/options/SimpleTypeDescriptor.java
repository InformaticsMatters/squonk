package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

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
}
