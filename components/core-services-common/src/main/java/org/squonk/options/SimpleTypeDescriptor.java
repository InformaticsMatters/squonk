package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by timbo on 15/01/16.
 */
public class SimpleTypeDescriptor<T> implements TypeDescriptor<T> {

    private Class<T> type;

    public SimpleTypeDescriptor(@JsonProperty("type") Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
