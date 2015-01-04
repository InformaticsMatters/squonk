package com.im.lac.camel.chemaxon.routes;

import chemaxon.struc.Molecule;
import com.im.lac.camel.chemaxon.processor.ChemTermsProcessor;
import com.im.lac.camel.chemaxon.processor.MoleculeConverterProcessor;
import com.im.lac.chemaxon.io.MoleculeIOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class CalculatorsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // These routes do the actual work
        // What these routes accept is determined by the registerd TypeConvertors, 
        // but the following should be accepted:
        // 1. a Molecule
        // 2. multiple Molecules as an Iterator<Molecule>
        // 3. A String representation of a single molecule (output will be a Molecule)
        // simple route that calculates a hard coded property
        from("direct:logp")
                .process(new MoleculeConverterProcessor())
                .process(new ChemTermsProcessor()
                        .add("logP()", "logp"));

        from("direct:logpSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemTermsProcessor()
                        .add("logP()", "logp"));

        from("direct:atomcount")
                .process(new MoleculeConverterProcessor())
                .log("atomcount body is ${body}")
                .process(new ChemTermsProcessor()
                        .add("atomCount()", "atom_count"));

        // simple route that calcuates multiple hard coded properties
        from("direct:logp_atomcount_bondcount")
                .process(new MoleculeConverterProcessor())
                .process(new ChemTermsProcessor()
                        .add("logP()", "logp")
                        .add("atomCount()", "atomCount")
                        .add("bondCount()", "bondCount")
                );

        // dynamic route that requires the chem terms configuration to be set using
        // the ChemTermsProcessor.PROP_EVALUATORS_DEFINTION header property.
        // NOTE: if this is to be used for multiple molecules send then all together
        // as an Iterable<Molecule> or InputStream that can be converted to Iterable<Molecule>
        // get get optimum performance
        from("direct:chemTerms")
                .process(new MoleculeConverterProcessor())
                .process(new ChemTermsProcessor());

        from("direct:chemTermsSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemTermsProcessor());

        from("direct:gunzip")
                .unmarshal().gzip();
        
        from("direct:gunzipAndCalculate")
                .to("direct:gunzip")
                .log("gunzip complete")
                .to("direct:logp_atomcount_bondcount")
                .log("calculation complete");

    }
}
