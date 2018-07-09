package org.squonk.types

import spock.lang.Specification

class SDFFileHandlerSpec extends Specification {

    void "instantiate"() {

        when:
        def input = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        SDFileHandler handler = new SDFileHandler()
        def value = handler.create(input)

        then:
        value != null
        value instanceof SDFile

        cleanup:
        input.close()

    }
}
