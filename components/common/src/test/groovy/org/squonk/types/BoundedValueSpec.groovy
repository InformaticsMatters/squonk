package org.squonk.types

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 * Created by timbo on 04/08/16.
 */
class BoundedValueSpec extends Specification {

    private static ObjectMapper mapper = new ObjectMapper()


    void "float to/from json"() {

        def r1 = new BoundedValue.Float(1.1f, 0.1f, 2.1f)

        when:

        def json = mapper.writeValueAsString(r1)
        def r2 = mapper.readValue(json, BoundedValue.class)

        then:
        r2 != null
        r2.value == 1.1f
        r2.upperBound == 2.1f
        r2.lowerBound == 0.1f
    }

    void "double to/from json"() {

        def r1 = new BoundedValue.Double(1.1d, 0.1d, 2.1d)

        when:

        def json = mapper.writeValueAsString(r1)
        println json
        def r2 = mapper.readValue(json, BoundedValue.class)

        then:
        r2 != null
        r2.value == 1.1d
        r2.upperBound == 2.1d
        r2.lowerBound == 0.1d
    }

    void "float from string"() {

        def r = new BoundedValue.Float(s)

        expect:
        r.value == v
        r.upperBound == u
        r.lowerBound == l

        where:
        s              || v     | u    | l
        "2|1|3"        || 2f    | 3f   | 1f
        "1.1|0.1|2.1"  || 1.1f  | 2.1f | 0.1f
        "-1.1|-2|2"    || -1.1f | 2f   | -2f
    }

    void "double from string"() {

        def r = new BoundedValue.Double(s)

        expect:
        r.value == v
        r.upperBound == u
        r.lowerBound == l

        where:
        s             || v     | u    | l
        "2|1|3"       || 2d    | 3d   | 1d
        "1.1|0.1|2.1" || 1.1d  | 2.1d | 0.1d
        "-1.1|-2|2"   || -1.1d | 2d   | -2d
    }


    void "float to string"() {

        def r = new BoundedValue.Float(v, l, u)

        expect:
        s.equals(r.toString())

        where:
        s               || v     | u    | l
        "0.0|-1.0|2.0"  || 0f    | 2f   | -1f
        "1.1|0.1|2.0"   || 1.1f  | 2f   | 0.1f
        "-1.1|-2.2|2.0" || -1.1f | 2f   | -2.2f

    }

    void "double to string"() {

        def r = new BoundedValue.Double(v, l, u)

        expect:
        s.equals(r.toString())

        where:
        s               || v     | u    | l
        "0.0|-1.0|2.0"  || 0d    | 2d   | -1d
        "1.1|0.1|2.0"   || 1.1d  | 2d   | 0.1d
        "-1.1|-2.2|2.0" || -1.1d | 2d   | -2.2d

    }

    void compare() {
        def r1 = new BoundedValue.Double(a, -100d, 100d)
        def r2 = new BoundedValue.Double(b, -100d, 100d)

        expect:
        c == r1.compareTo(r2)

        where:
        a     | b     | c
        1d    | 2d    | -1
        1.1d  | 1.1d  | 0
        1.1d  | -2d   | 1
    }

    void "null values not permitted"() {

        expect:
        def r2 = null
        try {
            r2 = new BoundedValue.Double(a, b, c)
        } catch (NullPointerException e) {
            1 == 1
        }
        r2 == null

        where:
        a     | b     | c
        null  | 2d    | -1
        1.1d  | null  | 0
        1.1d  | 2d    | null

    }


    void "invalid bounds"() {

        expect:
        def r2 = null
        try {
            r2 = new BoundedValue.Double(v, l, u)
        } catch (IllegalArgumentException e) {
            1 == 1
        }
        r2 == null

        where:
        v     | u     | l
        3d    | 2d    | -1
        1.1d  | 3d    | 2d

    }

}
