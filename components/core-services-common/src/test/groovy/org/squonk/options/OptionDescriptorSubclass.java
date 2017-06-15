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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by timbo on 18/03/16.
 */
public class OptionDescriptorSubclass extends OptionDescriptor<Integer> {

    OptionDescriptorSubclass(Class<Integer> type, String key, String label, String description) {
        super(type, key, label, description, OptionDescriptor.Mode.User);
    }

    @JsonCreator
    public OptionDescriptorSubclass(
            @JsonProperty("typeDescriptor") TypeDescriptor<Integer> typeDescriptor,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") Integer[] values,
            @JsonProperty("defaultValue") Integer defaultValue,
            @JsonProperty("visible") boolean visible,
            @JsonProperty("editable") boolean editable,
            @JsonProperty("minValues") Integer minValues,
            @JsonProperty("maxValues") Integer maxValues,
            @JsonProperty("modes") Mode[] modes,
            @JsonProperty("properties") Map<String,Object> properties
    ) {
        super(typeDescriptor, key, label, description, values, defaultValue, visible, editable, minValues, maxValues, modes, properties);
    }

    public String getPlanet() {
        return getProperty("planet", String.class);
    }

    public void setPlanet(String planet) {
        putProperty("planet", planet);
    }

    public Integer getPosition() {
        return getProperty("position", Integer.class);
    }

    public void setPosition(Integer position) {
        putProperty("position", position);
    }



}
