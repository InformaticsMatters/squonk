package org.squonk.camel.processor

import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import static org.squonk.util.CommonConstants.*
import spock.lang.Specification

/**
 * Created by timbo on 29/05/16.
 */
class PropertyFilterProcessorSpec extends Specification {

    static List mols = [
            new MoleculeObject("mol1", "smiles", [a:1, b:1.1]),
            new MoleculeObject("mol2", "smiles", [a:2, b:2.2]),
            new MoleculeObject("mol3", "smiles", [a:3, b:3.3]),
            new MoleculeObject("mol4", "smiles", [a:4, b:4.4]),
            new MoleculeObject("mol5", "smiles", [a:5, b:5.5]),
            new MoleculeObject("mol5", "smiles", [:])
    ]

    static CamelContext context

    void setupSpec() {

        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {

                from("direct:preset_nulls_fail")
                        .process(new PropertyFilterProcessor("fieldName")
                .filterInteger("a", false, 2, 4)
                .filterDouble("b", false, 3.0d, 10.0d))

                from("direct:preset_nulls_pass")
                        .process(new PropertyFilterProcessor("fieldName")
                        .filterInteger("a", true, 2, 4)
                        .filterDouble("b", true, 3.0d, 10.0d))

                from("direct:dynamic")
                        .process(new PropertyFilterProcessor("fieldName")
                        .filterInteger("a")
                        .filterDouble("b"))
            }
        })

        context.start()
    }

    void cleanupSpec() {
        context.stop()
    }


    void "preset nulls fail check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:preset_nulls_fail", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): mode]).getItems().size() == count

        where:
        count | mode
        2     | VALUE_INCLUDE_PASS
        4     | VALUE_INCLUDE_FAIL
        6     | VALUE_INCLUDE_ALL
    }

    void "preset nulls pass check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:preset_nulls_pass", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): mode]).getItems().size() == count

        where:
        count | mode
        3     | VALUE_INCLUDE_PASS
        3     | VALUE_INCLUDE_FAIL
        6     | VALUE_INCLUDE_ALL
    }

    void "dynamic check"() {

        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        pt.requestBodyAndHeaders("direct:dynamic", new Dataset(MoleculeObject.class, mols), headers).getItems().size() == count

        where:
        count | headers
        2     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a.min": 2, "a.max": 4, "b.min": 3.0d, "b.max":10.0d]
        4     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_FAIL, "a.min": 2, "a.max": 4, "b.min": 3.0d, "b.max":10.0d]
        6     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_ALL, "a.min": 2, "a.max": 4, "b.min": 3.0d, "b.max":10.0d]
        4     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a.max": 4, "b.max":10.0d]
        2     | [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS, "a.min": 2, "a.max":3]

    }


    void "return MoleculeObjectDataset"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def result = pt.requestBodyAndHeaders("direct:preset_nulls_pass", new Dataset(MoleculeObject.class, mols), [(OPTION_FILTER_MODE): VALUE_INCLUDE_PASS])

        then:
        result instanceof MoleculeObjectDataset

    }

}
