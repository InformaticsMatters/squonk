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

package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by timbo on 29/02/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookSavepointDTO extends AbstractNotebookVersionDTO {

    private String creator;
    private String description;
    private String label;

    public NotebookSavepointDTO() {

    }

    public NotebookSavepointDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("notebookId") Long notebookId,
            @JsonProperty("parentId") Long parentId,
            @JsonProperty("creator") String creator,
            @JsonProperty("createdDate") Date createdDate,
            @JsonProperty("updatedDate") Date updatedDate,
            @JsonProperty("description") String description,
            @JsonProperty("label") String label,
            @JsonProperty("canvasDTO") NotebookCanvasDTO canvasDTO) {
        super(id, notebookId, parentId, createdDate, updatedDate, canvasDTO);
        this.creator = creator;
        this.description = description;
        this.label = label;
    }

    public String getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

}
