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
class NotebookDTOSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookDTO d1 = new NotebookDTO(1, "name", "description", "owner", new Date(), new Date(), ['public', 'banana'], 0, 0, 0)
        String json = JsonHandler.getInstance().objectToJson(d1)
        NotebookDTO d2 = JsonHandler.getInstance().objectFromJson(json, NotebookDTO.class)

        then:
        json != null
        json.length() > 0
        d2 != null
        d2.id == 1
        d2.name == "name"
        d2.description == "description"
        d2.owner == "owner"
        d2.createdDate != null
        d2.lastUpdatedDate != null
        d2.layers.size() == 2
        d2.layers[0] == 'public'
    }

}
