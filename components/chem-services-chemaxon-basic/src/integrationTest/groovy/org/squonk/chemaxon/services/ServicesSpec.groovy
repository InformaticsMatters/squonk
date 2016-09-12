package org.squonk.chemaxon.services

import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.camel.chemaxon.processor.screening.MoleculeScreenerProcessor
import org.squonk.core.ServiceDescriptor
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.io.JsonHandler
import org.squonk.util.CommonMimeTypes
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

class ServicesSpec extends Specification {

    static String calculatorsbase = "//" + IOUtils.getDockerGateway() + ":8092/chem-services-chemaxon-basic/rest/v1/calculators"
    static String descriptorsbase = "//" + IOUtils.getDockerGateway() + ":8092/chem-services-chemaxon-basic/rest/v1/descriptors"
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
        Stream<ServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, ServiceDescriptor.class, false)

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
                "Content-Type":c,
                "Accept": a
        ]
        def output = pt.requestBodyAndHeaders("http4:"+calculatorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size == 10
        list[0].values.size() == props

        cleanup:
        json.close()

        where:
        c | a | path                | cls                  | props
        M | B | '/logp'             | BasicObject.class    | 1
        M | M | '/logp'             | MoleculeObject.class | 1
        M | B | '/atomCount'        | BasicObject.class    | 1
        M | M | '/atomCount'        | MoleculeObject.class | 1
        M | B | '/lipinski'         | BasicObject.class    | 5 // 4 + error count
        M | M | '/lipinski'         | MoleculeObject.class | 5 // 4 + error count
    }

    void "rest clustering json/json"() {

        Dataset dataset = Molecules.nci100Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":c,
                "Accept": a
        ]
        def output = pt.requestBodyAndHeaders("http4:"+descriptorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size == count
        list[0].values.size() == props

        cleanup:
        json.close()

        where:
        c | a | path                        | cls                  | props | count
        M | B | '/clustering/spherex/ecfp4' | BasicObject.class    | 1     | 100
        M | M | '/clustering/spherex/ecfp4' | MoleculeObject.class | 1     | 100

    }


    void "rest screening json/json"() {

        Dataset dataset = Molecules.nci100Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":c,
                "Accept": a,
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_source"): 'CC1=CC(=O)C=CC1=O',
                (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_format"): 'smiles',
                (MoleculeScreenerProcessor.HEADER_THRESHOLD): 0.4
        ]

        def output = pt.requestBodyAndHeaders("http4:"+descriptorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size >=1 && list.size < 100
        list[0].values.size() == 1

        cleanup:
        json.close()

        where:
        c | a | path                       | cls
        M | B | '/screening/ecfp4'         | BasicObject.class
        M | M | '/screening/ecfp4'         | MoleculeObject.class
        M | B | '/screening/pharmacophore' | BasicObject.class
        M | M | '/screening/pharmacophore' | MoleculeObject.class

    }


}