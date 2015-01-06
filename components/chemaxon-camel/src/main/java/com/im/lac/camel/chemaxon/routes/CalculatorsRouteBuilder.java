package com.im.lac.camel.chemaxon.routes;

import chemaxon.struc.Molecule;
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor;
import com.im.lac.camel.chemaxon.processor.MoleculeConverterProcessor;
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
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:logpSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:atomcount")
                .process(new MoleculeConverterProcessor())
                .log("atomcount body is ${body}")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()"));

        // simple route that calcuates multiple hard coded properties
        from("direct:logp_atomcount_bondcount")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()")
                        .calculate("atom_count", "atomCount()")
                        .calculate("bond_count", "bondCount()")
                );
        
        // simple route that exemplifies filtering
        from("direct:filter_example")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .filter("mass()<400")
                        .filter("ringCount()>0")
                        .filter("rotatableBondCount()<5")
                        .filter("donorCount()<=5")
                        .filter("acceptorCount()<=10")
                        .filter("logP()<4")
                        
                );

        // dynamic route that requires the chem terms configuration to be set using
        // the ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property.
        // NOTE: if this is to be used for multiple molecules send then all together
        // as an Iterable<Molecule> or InputStream that can be converted to Iterable<Molecule>
        // get get optimum performance
        from("direct:chemTerms")
                .process(new MoleculeConverterProcessor())
                .process(new ChemAxonMoleculeProcessor());

        from("direct:chemTermsSingleMolecule")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor());
        
        from("direct:standardize")
                .convertBodyTo(Molecule.class)
                .process(new ChemAxonMoleculeProcessor()
                .standardize("aromatize")
                );
                

        from("direct:gunzip")
                .unmarshal().gzip();
        
        from("direct:gunzipAndCalculate")
                .to("direct:gunzip")
                .log("gunzip complete")
                .to("direct:logp_atomcount_bondcount")
                .log("calculation complete");

    }
}
