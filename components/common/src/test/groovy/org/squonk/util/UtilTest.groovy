package org.squonk.util

import spock.lang.Specification

/**
 * Created by timbo on 05/05/17.
 */
class UtilTest extends Specification  {

    void "safeEqualsIncludeNull"() {


        expect:
        Utils.safeEqualsIncludeNull(a, b) == result

        where:
        a    | b    | result
        '0'  | '0'  | true
        '0'  | '1'  | false
        '0'  | null | false
        null | '1'  | false
        null | null | true

    }
}
