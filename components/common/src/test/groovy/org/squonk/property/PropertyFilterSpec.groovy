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

package org.squonk.property

import org.squonk.types.BasicObject
import org.squonk.types.NumberRange
import spock.lang.Specification

/**
 * Created by timbo on 27/05/16.
 */
class PropertyFilterSpec extends Specification {

    void "filter integer"() {

        expect:
        new PropertyFilter("prop", incl, new NumberRange.Integer(min, max)).test(new BasicObject([prop:val])) == result

        where:
        val  | incl  | min  | max | result
        5    | true  | 1    | 10   | true
        5.0  | true  | 1    | 10   | true
        5    | true  | 1    | null | true
        5    | true  | null | 10   | true
        5    | true  | 1    | 2    | false
        5    | true  | null | 2    | false
        null | true  | 1    | 10   | true
        null | false | 1    | 10   | false
    }

    void "filter double"() {

        expect:
        new PropertyFilter("prop", incl, new NumberRange.Double(min, max)).test(new BasicObject([prop:val])) == result

        where:
        val  | incl  | min  | max | result
        5.0  | true  | 1.0  | 10.0 | true
        5    | true  | 1.0  | 10.0 | true
        5.0  | true  | 1.0  | null | true
        5.0  | true  | null | 10.0 | true
        5.0  | true  | 1.0  | 2.0  | false
        5.0  | true  | null | 2.0  | false
        null | true  | 1.0  | 10.0 | true
        null | false | 1.0  | 10.0 | false
    }

}
