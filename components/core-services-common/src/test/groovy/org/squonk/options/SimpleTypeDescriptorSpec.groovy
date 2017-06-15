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
 * Created by timbo on 15/01/16.
 */
class SimpleTypeDescriptorSpec extends Specification {

    void "test json"() {

        def std1 = new SimpleTypeDescriptor(Integer.class);

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        def std2 = JsonHandler.getInstance().objectFromJson(json, TypeDescriptor.class)

        then:
        println json
        json != null
        std2 != null
        std2 instanceof SimpleTypeDescriptor
        std2.type == Integer.class

    }
}
