package org.squonk.options

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/01/16.
 */
class MultiLineTextTypeDescriptorSpec extends Specification {

    void "test json"() {

        def td1 = new MultiLineTextTypeDescriptor(10, 20, "text/plain");

        when:
        def json = JsonHandler.getInstance().objectToJson(td1)
        def td2 = JsonHandler.getInstance().objectFromJson(json, TypeDescriptor.class)

        then:
        println json
        json != null
        td2 != null
        td2 instanceof MultiLineTextTypeDescriptor
        td2.type == String.class
        td2.rows == 10
        td2.cols == 20
        td2.mimeType == "text/plain"

    }
}
