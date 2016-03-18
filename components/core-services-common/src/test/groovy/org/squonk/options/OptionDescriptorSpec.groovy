package org.squonk.options

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/01/16.
 */
class OptionDescriptorSpec extends Specification {

    void "test json"() {

        def std1 = new OptionDescriptor(Integer.class, "key", "label", "description");

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptor
        std2.typeDescriptor.type == Integer.class

    }


    void "test with subclass"() {

        def std1 = new OptionDescriptorSubclass(Integer.class, "key", "label", "description");
        std1.putProperty("planet", "earth")
        std1.putProperty("position", new Integer(3))

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptorSubclass
        std2.typeDescriptor.type == Integer.class
        std2.getProperty("planet") == "earth"
        std2.getProperty("position") == 3
    }

}
