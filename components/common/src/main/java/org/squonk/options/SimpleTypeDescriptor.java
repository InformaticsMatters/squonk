/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @Override
    public String getJsonSchemaType() {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class) {
            return "integer";
        } else if (type == Float.class) {
            return "float";
        } else if (type == Boolean.class) {
            return "boolean";
        }
        return null;
    }
}
