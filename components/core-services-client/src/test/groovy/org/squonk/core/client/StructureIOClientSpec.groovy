package org.squonk.core.client

import org.squonk.io.DepictionParameters
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 04/09/16.
 */
class StructureIOClientSpec extends Specification {

    @Shared
    StructureIOClient client = new StructureIOClient.CDK()

    void "one line svg message"() {



        when:
        String svg =client.renderErrorSVG(new DepictionParameters(100, 75), "I am an error")
        println svg

        then:
        svg != null
    }

    void "two line svg message"() {



        when:
        String svg =client.renderErrorSVG(new DepictionParameters(100, 75), "I am an error", "So am I")
        println svg

        then:
        svg != null
    }
}
