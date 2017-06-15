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

package org.squonk.notebook.api

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 11/03/16.
 */
class NotebookSavepointDTOSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookSavepointDTO s1 = new NotebookSavepointDTO(1, 2, 3, "creator", new Date(), new Date(), "description", "label", new NotebookCanvasDTO(1))
        String json = JsonHandler.getInstance().objectToJson(s1)
        NotebookSavepointDTO s2 = JsonHandler.getInstance().objectFromJson(json, NotebookSavepointDTO.class)

        then:
        json != null
        json.length() > 0
        s2 != null
        s2.id == 1
        s2.notebookId == 2
        s2.parentId == 3
        s2.creator == "creator"
        s2.createdDate != null
        s2.lastUpdatedDate != null
        s2.description == "description"
        s2.label == "label"
        s2.canvasDTO != null
    }

}
