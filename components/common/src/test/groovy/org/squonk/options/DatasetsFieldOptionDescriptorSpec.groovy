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

package org.squonk.options

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 13/03/17.
 */
class DatasetsFieldOptionDescriptorSpec extends Specification {


    void "test to/from json"() {

        def od1 = new DatasetsFieldOptionDescriptor("key",  "name", "desc").withDefaultValue("default")

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        //println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, DatasetsFieldOptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"
        od2.label == "name"
        od2.defaultValue == "default"

    }


}
