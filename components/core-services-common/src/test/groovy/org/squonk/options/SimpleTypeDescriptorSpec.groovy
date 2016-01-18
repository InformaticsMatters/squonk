package org.squonk.options

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/01/16.
 */
class SimpleTypeDescriptorSpec extends Specification {

    void "test json"() {

        def std1 = new SimpleTypeDescriptor(Integer.class);

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        def std2 = JsonHandler.getInstance().objectFromJson(json, TypeDescriptor.class)

        then:
        println json
        json != null
        std2 != null
        std2 instanceof SimpleTypeDescriptor
        std2.type == Integer.class

    }
}
