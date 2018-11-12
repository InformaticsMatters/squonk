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

package org.squonk.jobdef;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.IODescriptor;

/**
 * Job for testing purposes only. 
 * Does absolutely nothing, but should return a JobStatus that indicates successful completion.
 *
 * @author timbo
 */
public class DoNothingJobDefinition implements CellExecutorJobDefinition {

    public DoNothingJobDefinition() {

    }

    public DoNothingJobDefinition(
            @JsonProperty("notebookId") Long notebookId,
            @JsonProperty("editableId") Long editableId,
            @JsonProperty("cellId") Long cellId,
            @JsonProperty("inputs") IODescriptor[] inputs,
            @JsonProperty("outputs") IODescriptor[] outputs) {
    }

    @Override
    public Long getNotebookId() {
        return 1l;
    }

    @Override
    public Long getEditableId() {
        return 1l;
    }

    @Override
    public Long getCellId() {
        return 1l;
    }

    @Override
    public IODescriptor[] getInputs() {
        return new IODescriptor[0];
    }

    @Override
    public IODescriptor[] getOutputs() {
        return new IODescriptor[0];
    }
}
