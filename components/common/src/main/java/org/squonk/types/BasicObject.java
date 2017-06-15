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

package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Closeable;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple generic object that only has values. Specialized types can be created by sub-classing
 * this class. For instance, see {@link MoleculeObject}.
 * Sub-classes must override the public clone() method.
 *
 * @author Tim Dudgeon
 */
@JsonIgnoreProperties({"value"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BasicObject implements Serializable, Cloneable {

    protected final UUID uuid;

    /**
     * Properties of the object.
     */
    protected final Map<String, Object> values = new LinkedHashMap<>();

    public BasicObject(UUID uuid) {
        this.uuid = (uuid == null ? UUID.randomUUID() : uuid);
    }

    public BasicObject() {
        this(UUID.randomUUID());
    }

    public BasicObject(Map<String, Object> values) {
        this(null, values);
    }

    public BasicObject(UUID uuid, Map<String, Object> values) {
        this(uuid);
        if (values != null && !values.isEmpty()) {
            this.values.putAll(values);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values.clear();
        this.values.putAll(values);
    }

    public <T> T getValue(String key, Class<T> type) {
        return (T) values.get(key);
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    public void putValues(Map<String, Object> values) {
        this.values.putAll(values);
    }

    public void clearValues() {
        this.values.clear();
    }

    public void clearValue(String key) {
        this.values.remove(key);
    }

    /**
     * Clone this BasicObject. The resulting clone will have the same UUID as the original and have its own map of values,
     * but the contents of the new Map will be the same as the old one (sames instances).
     *
     * @return
     */
    public BasicObject clone() {
        return new BasicObject(this.uuid, values);
    }

    public void merge(BasicObject other, boolean ignoreValues) {
        if (!ignoreValues) {
            this.values.putAll(other.getValues());
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("BasicObject uuid: ")
                .append(uuid)
                .append(" values: [");
        for (Map.Entry<String, Object> e : values.entrySet()) {
            b.append(e.getKey())
                    .append(":")
                    .append(e.getValue())
                    .append(" ");
        }
        b.append("]");

        return b.toString();
    }


}
