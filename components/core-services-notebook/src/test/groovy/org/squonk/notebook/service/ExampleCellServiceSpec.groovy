package org.squonk.notebook.service

import spock.lang.Specification

/**
 * Created by timbo on 19/12/15.
 */
class ExampleCellServiceSpec extends Specification {

    void "test get cell defs"() {

        when:
        def cells = ExampleCellService.createDescriptors()

        then:
        cells.size() > 0

    }
}
