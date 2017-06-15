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

import spock.lang.Specification

/**
 * Created by timbo on 27/04/17.
 */
class DatasetFieldTypeDescriptorSpec extends Specification {

    void "test filter"() {

        when:
        def d = new DatasetFieldTypeDescriptor('foo', [Number.class] as Class[])

        then:
        d.filter('bar', Float.class)
        d.filter('bar', Double.class)
        !d.filter('bar', String.class)

    }

    void "null input name becomes input"() {

        when:
        def d = new DatasetFieldTypeDescriptor(null, [Number.class] as Class[])

        then:
        d.inputName == "input"
    }

}
