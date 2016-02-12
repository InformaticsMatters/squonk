package org.squonk.chemaxon.services

import org.squonk.core.ServiceDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 12/02/16.
 */
class ChemaxonRestRouteBuilderSpec extends Specification {

    void "calculators service descriptors to/from json"() {

        when:
        String json = JsonHandler.getInstance().objectToJson(ChemaxonRestRouteBuilder.SERVICE_DESCRIPTOR_CALCULATORS)
        Stream<ServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, ServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0

    }

    void "descriptors service descriptors to/from json"() {

        when:
        String json = JsonHandler.getInstance().objectToJson(ChemaxonRestRouteBuilder.SERVICE_DESCRIPTOR_DESCRIPTORS)
        Stream<ServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, ServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0

    }

}

