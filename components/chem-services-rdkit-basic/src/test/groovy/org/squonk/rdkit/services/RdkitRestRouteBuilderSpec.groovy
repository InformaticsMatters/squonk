package org.squonk.rdkit.services

import org.squonk.core.HttpServiceDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 12/02/16.
 */
class RdkitRestRouteBuilderSpec extends Specification  {

    void "calculators service descriptors to/from json"() {

        when:
        String json = JsonHandler.getInstance().objectToJson(RdkitBasicRestRouteBuilder.CALCULATORS_SERVICE_DESCRIPTOR)
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0

    }
}
