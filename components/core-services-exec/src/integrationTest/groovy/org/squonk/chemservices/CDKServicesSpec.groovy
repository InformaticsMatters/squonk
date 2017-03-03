package org.squonk.chemservices

import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.util.CommonMimeTypes
import org.squonk.util.IOUtils

import org.squonk.core.HttpServiceDescriptor
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

class CDKServicesSpec extends Specification implements CommonMimeTypes{

    static String calculatorsbase = "//localhost:8092/chem-services-cdk-basic/rest/v1/calculators"

    static String B = CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON
    static String M = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON


    @Shared CamelContext context = new DefaultCamelContext()

    void setupSpec() {
        context.start()
    }

    void cleanup() {
        context?.stop()
    }


    void "fetch ServiceDescriptors"() {

        when:
        InputStream json = new URL("http:"+calculatorsbase).openStream()
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class, false)

        then:
        sds.count() > 0

        cleanup:
        json?.close()

    }

    void "rest calculators json/json"() {

        Dataset dataset = Molecules.nci10Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":contentType,
                "Accept": accept
        ]
        def output = pt.requestBodyAndHeaders("http4:"+calculatorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size == 10
        list[0].values.size() == props

        cleanup:
        json.close()

        where:
        contentType | accept  | path                | cls                  | props
        M           | B       | '/logp'             | BasicObject.class    | 3 // XLogP_CDK, AMR_CDK, ALogP_CDK
        M           | M       | '/logp'             | MoleculeObject.class | 3 // XLogP_CDK, AMR_CDK, ALogP_CDK
        M           | B       | '/donors_acceptors' | BasicObject.class    | 2
        M           | M       | '/donors_acceptors' | MoleculeObject.class | 2
        M           | B       | '/wiener_numbers'   | BasicObject.class    | 2
        M           | M       | '/wiener_numbers'   | MoleculeObject.class | 2
    }

}