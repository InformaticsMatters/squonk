package org.squonk.io

import spock.lang.Specification

/**
 * Created by timbo on 27/12/16.
 */
class ResourceLoaderSpec extends Specification {

    void "merge urls"() {

        URL url1 = new URL('file://some.where.com/some/place/thing.txt')

        when:
        URL url2 = new URL(url1, 'somethingelse.txt')

        then:

        url2.toString() == 'file://some.where.com/some/place/somethingelse.txt'

    }
}
