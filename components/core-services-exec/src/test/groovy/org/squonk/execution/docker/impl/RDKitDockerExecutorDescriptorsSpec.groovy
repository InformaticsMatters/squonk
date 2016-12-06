package org.squonk.execution.docker.impl

import spock.lang.Specification

/**
 * Created by timbo on 06/12/16.
 */
class RDKitDockerExecutorDescriptorsSpec extends Specification {

    void "test read resource"() {
        when:
        InputStream is = getClass().getResourceAsStream("foo.txt")

        then:
        is != null
    }
}
