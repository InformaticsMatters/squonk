package org.squonk.types

import com.im.lac.types.MoleculeObject
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.http.RequestInfo
import spock.lang.Shared
import spock.lang.Specification

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON
import static org.squonk.api.MimeTypeResolver.MIME_TYPE_MDL_SDF

/**
 * Created by timbo on 25/03/2016.
 */
class DatasetHandlerSpec extends Specification {

    static TypeResolver resolver = new TypeResolver()
    static def input = [
            new MoleculeObject('C', 'smiles', [name: 'venus']),
            new MoleculeObject('CC', 'smiles', [name: 'mercury']),
            new MoleculeObject('CCC', 'smiles', [name: 'earth']),
    ]
    static String[] datasetMime = [MIME_TYPE_DATASET_MOLECULE_JSON] as String[]

    @Shared DefaultCamelContext context

    void setupSpec() {
        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {

                from("direct:foo").log("foo received")
            }
        })
    }

    void cleanupSpec() {
        context?.shutdown()
    }

    Exchange send(String contentType, String accept, InputStream is) {
        ProducerTemplate pt = context.createProducerTemplate()
        pt.request("direct:foo", { Exchange exch ->
            exch.in.setHeader('Content-Type', contentType)
            exch.in.setHeader('Accept', accept)
            exch.in.setHeader('Content-Encoding', "gzip")
            exch.in.setHeader('Accept-Encoding', "gzip")
            exch.in.setBody(is)
        })
    }

    void "send mo want mo"() {

        Dataset ds = new Dataset(MoleculeObject.class, input)
        DatasetHandler handler = new DatasetHandler(MoleculeObject.class)

        when:
        Exchange req = send(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_MOLECULE_JSON, ds.getInputStream(true))
        RequestInfo info = RequestInfo.build(datasetMime, datasetMime, req)
        def o = handler.fetchInput(req, MIME_TYPE_DATASET_MOLECULE_JSON, resolver, info)

        then:
        o != null
        o instanceof Dataset
        o.type == MoleculeObject.class
        o.items.size() == 3
    }

    void "send sdf want mo"() {

        FileInputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        DatasetHandler handler = new DatasetHandler(MoleculeObject.class)

        when:
        Exchange req = send(MIME_TYPE_MDL_SDF, MIME_TYPE_DATASET_MOLECULE_JSON, is)
        RequestInfo info = RequestInfo.build(datasetMime, datasetMime, req)
        def o = handler.fetchInput(req, MIME_TYPE_DATASET_MOLECULE_JSON, resolver, info)

        then:
        o != null
        o instanceof Dataset
        o.type == MoleculeObject.class
        o.items.size() == 36
    }

}
