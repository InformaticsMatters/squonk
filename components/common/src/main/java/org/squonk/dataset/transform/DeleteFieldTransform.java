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
public class DeleteFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final String condition;

    protected DeleteFieldTransform(@JsonProperty("fieldName") String fieldName,
                                   @JsonProperty("condition") String condition) {
        this.fieldName = fieldName;
        this.condition = condition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getCondition() {
        return condition;
    }
}
