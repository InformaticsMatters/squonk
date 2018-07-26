package org.squonk.execution

import org.squonk.core.DockerServiceDescriptor
import org.squonk.core.ServiceDescriptor
import org.squonk.io.IODescriptor
import org.squonk.types.SDFile
import org.squonk.types.io.JsonHandler
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

class ExecutionParametersSpec extends Specification {


    void "to from json"() {

        def inputiods = [new IODescriptor("input", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        def outputiods = [new IODescriptor("output", CommonMimeTypes.MIME_TYPE_MDL_SDF, SDFile.class)] as IODescriptor[]
        DockerServiceDescriptor sd1 = new DockerServiceDescriptor("id", "name", inputiods, outputiods)
        Map options1 = [string_option: 'string value', int_option: 999, float_option: 123.456f]
        ExecutionParameters params1 = new ExecutionParameters(sd1, options1)

        when:
        String json = JsonHandler.getInstance().objectToJson(params1)
        println json
        ExecutionParameters params2 = JsonHandler.getInstance().objectFromJson(json, ExecutionParameters.class)
        ServiceDescriptor sd2 = params2.getServiceDescriptor()
        Map options2 = params2.getOptions()

        then:
        sd2 instanceof DockerServiceDescriptor
        sd2.getId() == "id"
        options2.size() == 3
        options2['string_option'] == 'string value'
        options2['int_option'] == 999
        options2['float_option'] == 123.456f
    }

    void "from json"() {

        def json = '''{
  "serviceDescriptor":{
    "@class":"org.squonk.core.DockerServiceDescriptor",
    "serviceConfig":{
      "id":"id",
      "name":"name",
      "inputDescriptors":[{"name":"input","mediaType":"chemical/x-mdl-sdfile","primaryType":"org.squonk.types.SDFile"}],
      "outputDescriptors":[{"name":"output","mediaType":"chemical/x-mdl-sdfile","primaryType":"org.squonk.types.SDFile"}]
    }
  },
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
