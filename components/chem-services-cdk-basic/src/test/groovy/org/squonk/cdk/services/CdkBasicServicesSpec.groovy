package org.squonk.cdk.services

import org.squonk.core.ServiceDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 27/11/16.
 */
class CdkBasicServicesSpec extends Specification {

    void "serviceDescriptor to/from json"() {

        when:
        String json = JsonHandler.getInstance().objectToJson(CdkBasicServices.ALL)
        Stream<ServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, ServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0

    }

}