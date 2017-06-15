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
class MultiLineTextTypeDescriptorSpec extends Specification {

    void "test json"() {

        def td1 = new MultiLineTextTypeDescriptor(10, 20, "text/plain");

        when:
        def json = JsonHandler.getInstance().objectToJson(td1)
        def td2 = JsonHandler.getInstance().objectFromJson(json, TypeDescriptor.class)

        then:
        println json
        json != null
        td2 != null
        td2 instanceof MultiLineTextTypeDescriptor
        td2.type == String.class
        td2.rows == 10
        td2.cols == 20
        td2.mimeType == "text/plain"

    }
}
