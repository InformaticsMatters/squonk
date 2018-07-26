package org.squonk.types

import org.squonk.io.FileDataSource
import spock.lang.Specification

class SDFFileHandlerSpec extends Specification {

    void "instantiate"() {

        when:
        def data = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        SDFileHandler handler = new SDFileHandler()
        def value = handler.create(data)

        then:
        value != null
        value instanceof SDFile
    }
}
