package org.squonk.cdk.services

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.ThreadPoolProfileBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.spi.ThreadPoolProfile
import org.squonk.camel.CamelCommonConstants
import org.squonk.core.ServiceDescriptor
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 12/02/16.
 */
class CdkRestRouteBuilderSpec extends Specification {

    @Shared CamelContext context = new DefaultCamelContext()

//    void setupSpec() {
//        context.addRoutes(new CdkCalculatorsRouteBuilder())
//        context.addRoutes(new CdkRestRouteBuilder())
//        ThreadPoolProfile profile = new ThreadPoolProfileBuilder(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME).poolSize(4).maxPoolSize(10).build();
//        context.getExecutorServiceManager().registerThreadPoolProfile(profile);
//        context.start()
//    }

//    void cleanup() {
//        context?.stop()
//    }


//    void "logp"() {
//
//        Dataset dataset = Molecules.nci100Dataset()
//        InputStream json = dataset.getInputStream(false)
//
//        ProducerTemplate pt = context.createProducerTemplate()
//        def headers = [:]
//
//        when:
//        def results = pt.requestBodyAndHeaders("http4:localhost:8092/", json)
//
//        cleanup:
//        json.close()
//
//    }
}
