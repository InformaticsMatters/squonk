package com.im.lac.services.chemaxon;

import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor;
import com.im.lac.types.MoleculeObject;

import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide examples of services. They are supposed to
 * illustrate what real-world services would need to do
 *
 * @author timbo
 */
public class CalculatorsRouteBuilder extends RouteBuilder {
    
    public static final String CHEMAXON_LOGP = "direct:logp";

    @Override
    public void configure() throws Exception {

        // These routes do the actual work
        // What these routes accept is determined by the registerd TypeConvertors, 
        // but the following should be accepted:
        // 1. a Molecule
        // 2. multiple Molecules as an Iterator<Molecule>
        // 3. A String representation of a single molecule (output will be a Molecule)
        // simple route that calculates a hard coded property
        from(CHEMAXON_LOGP)
                .log("logp starting")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("CXN_LogP", "logP()"))
                .log("logp finished");

        from("direct:logpSingleMolecule")
                .convertBodyTo(MoleculeObject.class)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:atomcount")
                .log("atomcount body is ${body}")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()"));

        // simple routes that calculates multiple hard coded properties
        from("direct:logp_atomcount_bondcount")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()")
                        .calculate("atom_count", "atomCount()")
                        .calculate("bond_count", "bondCount()")
                );
        
        from("direct:lipinski")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("mol_weight", "mass()")
                        .calculate("logp", "logP()")
                        .calculate("hbd_count", "donorCount()")
                        .calculate("hba_count", "acceptorCount()")
                );

        // simple route that exemplifies filtering
        from("direct:filter_example")
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
                .process(new ChemAxonMoleculeProcessor());

        from("direct:chemTermsSingleMolecule")
                .process(new ChemAxonMoleculeProcessor());

        from("direct:standardize")
                .process(new ChemAxonMoleculeProcessor()
                        .standardize("aromatize")
                );

    }
}
