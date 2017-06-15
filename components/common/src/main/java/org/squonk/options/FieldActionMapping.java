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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by timbo on 17/05/16.
 */
public class FieldActionMapping {

    private final Map<String,String> mappings = new LinkedHashMap<>();


    public FieldActionMapping(@JsonProperty("mappings") Map<String,String> mappings) {
        this.mappings.putAll(mappings);
    }

    public FieldActionMapping() { }

    public void addMapping(String field, String action) {
        mappings.put(field, action);
    }

    public void removeMapping(String field) {
        mappings.remove(field);
    }

    @JsonIgnore
    public String getMapping(String field) {
        return mappings.get(field);
    }

    public Map<String,String> getMappings() {
        return mappings;
    }
}
