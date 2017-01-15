package org.squonk.options

import org.squonk.types.NumberRange
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 13/01/17.
 */
class OptionDescriptorSpec extends Specification {

    void "test number range json"() {

        def od1 = new OptionDescriptor<>(NumberRange.Float.class, "key",
                "Mol weight", "Molecular weight", OptionDescriptor.Mode.User).withMinMaxValues(0,1).withDefaultValue(new NumberRange.Float(0f, 500f))

        when:
        def json = JsonHandler.getInstance().objectToJson(od1)
        println json
        def od2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:
        od2 != null
        od2.key == "key"

    }
}
