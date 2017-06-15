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

package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConvertFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final Class newType;
    private final Class genericType;
    private final String onError;

    protected ConvertFieldTransform(
            @JsonProperty("fieldName")String fieldName,
            @JsonProperty("newType")Class newType,
            @JsonProperty("genericType")Class genericType,
            @JsonProperty("onError")String onError) {
        this.fieldName = fieldName;
        this.newType = newType;
        this.genericType = genericType;
        this.onError = onError;
    }

    protected ConvertFieldTransform(String fieldName, Class newType) {
        this(fieldName, newType, null, "fail");
    }


    public String getFieldName() {
        return fieldName;
    }
    
    public Class getNewType() {
        return newType;
    }

    public Class getGenericType() {
        return genericType;
    }

    public String getOnError() {
        return onError;
    }
}
