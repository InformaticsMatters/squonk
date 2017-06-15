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

import org.squonk.types.MoleculeObject
import org.squonk.types.NumberRange
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 13/01/17.
 */
class OptionDescriptorSpec extends Specification {

    void "test number range json"() {

        def od1 = new OptionDescriptor<>(NumberRange.Float.class, "key",
                "name", "desc", OptionDescriptor.Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 500f))

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"

    }

    void "test boolean json"() {

        def od1 = new OptionDescriptor<>(Boolean.class, "key",
                "name", "desc", OptionDescriptor.Mode.User).withMinMaxValues(0,1).withDefaultValue(Boolean.TRUE)

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"

    }


    void "test field type restriction json"() {

        def od1 = new OptionDescriptor<>(new DatasetFieldTypeDescriptor([Number.class] as Class[]),
                "key", "name", "desc", OptionDescriptor.Mode.User)
        od1 = od1.withMinMaxValues(1, 1)

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"

    }


}
