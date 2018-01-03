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

package org.squonk.types

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 * Created by timbo on 04/08/16.
 */
class NumberRangeSpec extends Specification {

    private static ObjectMapper mapper = new ObjectMapper()

    void "integer to/from json"() {

        def r1 = new NumberRange.Integer(0, 10)

        when:
        def json = mapper.writeValueAsString(r1)
        def r2 = mapper.readValue(json, NumberRange.class)

        then:
        r2 != null
        r2.minValue == 0
        r2.maxValue == 10
    }

    void "integer undefined to/from json"() {

        def r1 = new NumberRange.Integer()

        when:
        def json = mapper.writeValueAsString(r1)
        def r2 = mapper.readValue(json, NumberRange.class)

        then:
        r2 != null
        r2.minValue == null
        r2.maxValue == null
    }

    void "float to/from json"() {

        def r1 = new NumberRange.Float(1.1f, 10.1f)

        when:

        def json = mapper.writeValueAsString(r1)
        def r2 = mapper.readValue(json, NumberRange.class)

        then:
        r2 != null
        r2.minValue == 1.1f
        r2.maxValue == 10.1f
    }

    void "double to/from json"() {

        def r1 = new NumberRange.Double(1.1d, 10.1d)

        when:

        def json = mapper.writeValueAsString(r1)
        def r2 = mapper.readValue(json, NumberRange.class)

        then:
        r2 != null
        r2.minValue == 1.1d
        r2.maxValue == 10.1d
    }

    void "integer from string"() {

        def r = new NumberRange.Integer(s)

        expect:
        r.minValue == a
        r.maxValue == b

        where:
        s      || a | b
        "0|2"  || 0 | 2
        "1|2"  || 1 | 2
        "-1|2" || -1 | 2
        "1|"   || 1 | null
        "|2"   || null | 2
        "|"    || null | null
    }

    void "float from string"() {

        def r = new NumberRange.Float(s)

        expect:
        r.minValue == a
        r.maxValue == b

        where:
        s        || a | b
        "0|2"    || 0f | 2f
        "1.1|2"  || 1.1f | 2f
        "-1.1|2" || -1.1f | 2f
        "1.1|"   || 1.1f | null
        "|2.2"   || null | 2.2f
        "|"      || null | null
    }

    void "double from string"() {

        def r = new NumberRange.Double(s)

        expect:
        r.minValue == a
        r.maxValue == b

        where:
        s        || a | b
        "0|2"    || 0d | 2d
        "1.1|2"  || 1.1d | 2d
        "-1.1|2" || -1.1d | 2d
        "1.1|"   || 1.1d | null
        "|2.2"   || null | 2.2d
        "|"      || null | null
    }

    void "integer to string"() {

        def r = new NumberRange.Integer(a, b)

        expect:
        s.equals(r.toString())

        where:
        s      || a | b
        "0|2"  || 0 | 2
        "1|2"  || 1 | 2
        "-1|2" || -1 | 2
        "1|"   || 1 | null
        "|2"   || null | 2
        "|"    || null | null
    }

    void "float to string"() {

        def r = new NumberRange.Float(a, b)

        expect:
        s.equals(r.toString())

        where:
        s          || a | b
        "0.0|2.0"  || 0f | 2f
        "1.1|2.0"  || 1.1f | 2f
        "-1.1|2.0" || -1.1f | 2f
        "1.1|"     || 1.1f | null
        "|2.2"     || null | 2.2f
        "|"        || null | null
    }

    void "double to string"() {

        def r = new NumberRange.Double(a, b)

        expect:
        s.equals(r.toString())

        where:
        s          || a | b
        "0.0|2.0"  || 0d | 2d
        "1.1|2.0"  || 1.1d | 2d
        "-1.1|2.0" || -1.1d | 2d
        "1.1|"     || 1.1d | null
        "|2.2"     || null | 2.2d
        "|"        || null | null
    }

    void "predicate integer"() {

        def r = new NumberRange.Integer(a, b)

        expect:
        r.test(c) == z

        where:
        a    | b    | c   | z
        0    | 10   | 5   | true
        0    | 10   | 15  | false
        0    | null | 5   | true
        null | 10   | 5   | true
        null | null | 5   | true
        0    | 10   | 5f  | true
        0    | 10   | 15f | false
        0    | 10   | 5d  | true
        0    | 10   | 15d | false

    }

    void "predicate float"() {

        def r = new NumberRange.Float(a, b)

        expect:
        r.test(c) == z

        where:
        a    | b    | c   | z
        0f   | 10f  | 5f  | true
        0f   | 10f  | 15f | false
        0f   | null | 5f  | true
        null | 10f  | 5f  | true
        null | null | 5f  | true
        0f   | 10f  | 5i  | true
        0f   | 10f  | 15i | false
        0f   | 10f  | 5d  | true
        0f   | 10f  | 15d | false

    }

    void "predicate double"() {

        def r = new NumberRange.Double(a, b)

        expect:
        r.test(c) == z

        where:
        a    | b    | c   | z
        0d   | 10d  | 5d  | true
        0d   | 10d  | 15d | false
        0d   | null | 5d  | true
        null | 10d  | 5d  | true
        null | null | 5d  | true
        0d   | 10d  | 5i  | true
        0d   | 10d  | 15i | false
        0d   | 10d  | 5f  | true
        0d   | 10d  | 15f | false

    }

    void "invalid range"() {

        when:
        r = new NumberRange.Double(2d, 1d)

        then:
        thrown(IllegalArgumentException.class)
    }

}
