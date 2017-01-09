package org.squonk.cpsign.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.squonk.core.HttpServiceDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 12/02/16.
 */
class CPSignRestRouteBuilderSpec extends Specification {



    void "service descriptors to/from json"() {

        when:
        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(CPSignRestRouteBuilder.SERVICE_DESCRIPTORS)
        //println json
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0
    }


}

