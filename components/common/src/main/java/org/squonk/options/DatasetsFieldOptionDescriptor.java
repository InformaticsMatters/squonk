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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;


/**
 * Defines an OptionDescriptor that is the field names in common between multiple datasets
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DatasetsFieldOptionDescriptor extends OptionDescriptor<String> implements Serializable {


    public DatasetsFieldOptionDescriptor(
            @JsonProperty("typeDescriptor") TypeDescriptor<String> typeDescriptor,
            @JsonProperty("key") String key,
            @JsonProperty("label") String label,
            @JsonProperty("description") String description,
            @JsonProperty("values") String[] values,
            @JsonProperty("defaultValue") String defaultValue,
            @JsonProperty("visible") boolean visible,
            @JsonProperty("editable") boolean editable,
            @JsonProperty("minValues") Integer minValues,
            @JsonProperty("maxValues") Integer maxValues,
            @JsonProperty("modes") Mode[] modes,
            @JsonProperty("properties") Map<String, Object> properties) {
        super(typeDescriptor, key, label, description, values, defaultValue, visible, editable, minValues, maxValues, modes, properties);

    }

    public DatasetsFieldOptionDescriptor(String key, String label, String description) {
        super(String.class, key, label, description, Mode.User);
    }

}
