package org.squonk.http

import spock.lang.Specification

/**
 * Created by timbo on 25/03/2016.
 */
class RequestInfoSpec extends Specification {

    void "findType"() {

        expect:
        RequestInfo.findType(r, w) == t

        where:
        r << [
                'application/json',
                'application/json',
                'application/json; q=1',
                'application/jsonnnnn',
                'text/plain,application/json',
                'text/plain, application/json'
        ]
        w << [
                ['application/json'] as String[],
                ['text/plain','application/json'] as String[],
                ['text/plain','application/json'] as String[],
                ['application/json'] as String[],
                ['application/json'] as String[],
                ['application/json'] as String[]
        ]
        t << [
                'application/json',
                'application/json',
                'application/json',
                null,
                'application/json',
                'application/json'
        ]
    }

}
