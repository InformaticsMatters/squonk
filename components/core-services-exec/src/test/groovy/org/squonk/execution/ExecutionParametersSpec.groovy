package org.squonk.execution

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

class ExecutionParametersSpec extends Specification {


    void "to from json"() {

        Map options1 = [string_option: 'string value', int_option: 999, float_option: 123.456f]
        ExecutionParameters params1 = new ExecutionParameters("hello", options1)

        when:
        String json = JsonHandler.getInstance().objectToJson(params1)
        println json
        ExecutionParameters params2 = JsonHandler.getInstance().objectFromJson(json, ExecutionParameters.class)
        String sd2 = params2.getServiceDescriptorId()
        Map options2 = params2.getOptions()

        then:
        sd2 == "hello"
        options2.size() == 3
        options2['string_option'] == 'string value'
        options2['int_option'] == 999
        options2['float_option'] == 123.456f
    }

    void "from json"() {

        def json = '''{
  "serviceDescriptorId":"id",
  "options":{
    "string_option":"string value",
    "int_option":999,
    "float_option":["java.lang.Float",123.456]
  }
}'''

        when:
        def ep = JsonHandler.getInstance().objectFromJson(json, ExecutionParameters.class)

        then:
        ep != null

    }

}
