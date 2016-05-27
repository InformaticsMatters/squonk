package org.squonk.property

import com.im.lac.types.BasicObject
import spock.lang.Specification

/**
 * Created by timbo on 27/05/16.
 */
class PropertyFilterSpec extends Specification {

    void "filter integer"() {

        expect:
        new PropertyFilter.IntegerRangeFilter("prop", incl, min, max).test(new BasicObject([prop:val])) == result

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
        new PropertyFilter.DoubleRangeFilter("prop", incl, min, max).test(new BasicObject([prop:val])) == result

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
