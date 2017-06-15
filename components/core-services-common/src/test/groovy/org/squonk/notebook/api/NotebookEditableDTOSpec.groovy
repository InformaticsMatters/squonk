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
class NotebookEditableDTOSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookEditableDTO e1 = new NotebookEditableDTO(1, 2, 3, "owner", new Date(), new Date(), new NotebookCanvasDTO(1))
        String json = JsonHandler.getInstance().objectToJson(e1)
        println json
        NotebookEditableDTO e2 = JsonHandler.getInstance().objectFromJson(json, NotebookEditableDTO.class)

        then:
        json != null
        json.length() > 0
        e2 != null
        e2.id == 1
        e2.notebookId == 2
        e2.parentId == 3
        e2.owner == "owner"
        e2.createdDate != null
        e2.lastUpdatedDate != null
        e2.canvasDTO != null
    }

}
