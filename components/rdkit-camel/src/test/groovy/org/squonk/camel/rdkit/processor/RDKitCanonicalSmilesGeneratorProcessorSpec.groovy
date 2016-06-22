package org.squonk.camel.rdkit.processor

import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.rdkit.io.RDKitMoleculeIOUtils
import spock.lang.Specification

/**
 * Created by timbo on 29/05/16.
 */
class RDKitCanonicalSmilesGeneratorProcessorSpec extends Specification {

    static List mols = [
            new MoleculeObject("C1=CC=CC=C1", "smiles"),
            new MoleculeObject("c1ccccc1", "smiles"),
            new MoleculeObject("C1=CC=CC=C1.C", "smiles"),
            new MoleculeObject("C1=CC=CC=C1.IC(I)(I)I", "smiles"),
            new MoleculeObject("C1CCCCC1.IC1=CC=CC=C1", "smiles"),
    ]

    static CamelContext context

    void setupSpec() {

        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {
                from("direct:myroute")
                        .process(new RDKitCanonicalSmilesGeneratorProcessor("cansmiles"))
            }
        })

        context.start()
    }

    void cleanupSpec() {
        context.stop()
    }


    void "by atom count"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def results = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), ["mode": RDKitMoleculeIOUtils.FragmentMode.BIGGEST_BY_ATOM_COUNT]).getItems()

        then:
        results[0].values['cansmiles'] == "c1ccccc1"
        results[1].values['cansmiles'] == "c1ccccc1"
        results[2].values['cansmiles'] == "c1ccccc1"
        results[3].values['cansmiles'] == "c1ccccc1"
        results[4].values['cansmiles'] == "C1CCCCC1"

    }

    void "by heavy atom count"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def results = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), ["mode": RDKitMoleculeIOUtils.FragmentMode.BIGGEST_BY_HEAVY_ATOM_COUNT]).getItems()

        then:
        results[0].values['cansmiles'] == "c1ccccc1"
        results[1].values['cansmiles'] == "c1ccccc1"
        results[2].values['cansmiles'] == "c1ccccc1"
        results[3].values['cansmiles'] == "c1ccccc1"
        results[4].values['cansmiles'] == "Ic1ccccc1"

    }

    void "by molweight"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def results = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), ["mode": RDKitMoleculeIOUtils.FragmentMode.BIGGEST_BY_MOLWEIGHT]).getItems()

        then:
        results[0].values['cansmiles'] == "c1ccccc1"
        results[1].values['cansmiles'] == "c1ccccc1"
        results[2].values['cansmiles'] == "c1ccccc1"
        results[3].values['cansmiles'] == "IC(I)(I)I"
        results[4].values['cansmiles'] == "Ic1ccccc1"

    }

    void "whole mol"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def results = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), ["mode": RDKitMoleculeIOUtils.FragmentMode.WHOLE_MOLECULE]).getItems()

        then:
        results[0].values['cansmiles'] == "c1ccccc1"
        results[1].values['cansmiles'] == "c1ccccc1"
        results[2].values['cansmiles'] == "C.c1ccccc1"
        results[3].values['cansmiles'] == "IC(I)(I)I.c1ccccc1"
        results[4].values['cansmiles'] == "C1CCCCC1.Ic1ccccc1"
    }


    void "return MoleculeObjectDataset"() {

        ProducerTemplate pt = context.createProducerTemplate()

        when:
        def result = pt.requestBodyAndHeaders("direct:myroute", new Dataset(MoleculeObject.class, mols), [:])

        then:
        result instanceof MoleculeObjectDataset

    }

}
