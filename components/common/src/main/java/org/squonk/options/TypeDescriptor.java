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

    /** Get the JSON schema type, and optionally the format.
     * The returned array may contain one ot two elements.
     *
     *
     * @return The type as the first element of the array. If format is defined this will be the second element.
     */
    @JsonIgnore
    String[] getJsonSchemaType();

}
