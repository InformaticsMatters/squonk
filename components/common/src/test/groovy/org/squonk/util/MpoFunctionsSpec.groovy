/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.util

import spock.lang.Specification

class MpoFunctionsSpec extends Specification {

    void "test ramp 0-1 ascending"() {

        def tx = MpoFunctions.createRampFunction(0d, 1d, 4d, 6d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 0d
        1d       | 0d
        2d       | 0d
        3d       | 0d
        4d       | 0d
        4.5d     | 0.25d
        5d       | 0.5d
        6d       | 1d
        7d       | 1d
        null     | null
    }

    void "test ramp 1-5 ascending"() {

        def tx = MpoFunctions.createRampFunction(1d, 5d, 4d, 6d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 1d
        1d       | 1d
        2d       | 1d
        3d       | 1d
        4d       | 1d
        4.5d     | 2d
        5d       | 3d
        6d       | 5d
        7d       | 5d
        null     | null
    }

    void "test ramp 1-0 descending"() {

        def tx = MpoFunctions.createRampFunction(1d, 0d, 4d, 6d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 1d
        1d       | 1d
        2d       | 1d
        3d       | 1d
        4d       | 1d
        4.5d     | 0.75d
        5d       | 0.5d
        6d       | 0d
        7d       | 0d
        null     | null
    }

    void "test ramp 5-1 descending"() {

        def tx = MpoFunctions.createRampFunction(5d, 1d, 4d, 6d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 5d
        1d       | 5d
        2d       | 5d
        3d       | 5d
        4d       | 5d
        4.5d     | 4d
        5d       | 3d
        6d       | 1d
        7d       | 1d
        null     | null
    }

    void "test hump 1"() {

        def tx = MpoFunctions.createHump1Function(0d, 2d, 1d,
                2d, 3d,
                5d, 7d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 0d
        1d       | 0d
        2d       | 0d
        3d       | 2d
        4d       | 2d
        5d       | 2d
        5.5d     | 1.75d
        6d       | 1.5d
        7d       | 1d
        null     | null
    }

    void "test hump 2"() {

        def tx = MpoFunctions.createHump2Function(0d, 2d, 1d, -1d,
                2d, 3d,
                5d, 7d,
                9d, 11d)

        expect:
        tx.transform(input) == result

        where:
        input    | result
        -1d      | 0d
        1d       | 0d
        2d       | 0d
        3d       | 2d
        4d       | 2d
        5d       | 2d
        5.5d     | 1.75d
        6d       | 1.5d
        7d       | 1d
        9d       | 1d
        10d      | 0d
        11d      | -1d
        12d      | -1d
        null     | null
    }
}
