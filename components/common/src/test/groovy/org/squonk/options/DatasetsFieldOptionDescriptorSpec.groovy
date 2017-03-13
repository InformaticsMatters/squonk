package org.squonk.options

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 13/03/17.
 */
class DatasetsFieldOptionDescriptorSpec extends Specification {


    void "test to/from json"() {

        def od1 = new DatasetsFieldOptionDescriptor("key",  "name", "desc")

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        //println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, DatasetsFieldOptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"
        od2.label == "name"

    }


}
