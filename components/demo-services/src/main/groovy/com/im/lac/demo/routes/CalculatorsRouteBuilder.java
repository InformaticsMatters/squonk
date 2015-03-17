package com.im.lac.demo.routes;

import com.im.lac.camel.cdk.processor.CDKMolecularDescriptorProcessor;
import com.im.lac.camel.chemaxon.processor.ChemAxonMoleculeProcessor;
import com.im.lac.cdk.molecule.MolecularDescriptors;
import com.im.lac.types.MoleculeObject;

import java.io.InputStream;
import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide examples of services. They are supposed to
 * illustrate what real-world services would need to do
 *
 * @author timbo
 */
public class CalculatorsRouteBuilder extends RouteBuilder {

    String base = "../../lacfiledrop/";

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
                //.process(new MoleculeObjectConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("CXN_LogP", "logP()"))
                .process(new CDKMolecularDescriptorProcessor()
                    .calculate(MolecularDescriptors.Descriptor.XLogP)
                    .calculate(MolecularDescriptors.Descriptor.ALogP))
                .log("logp finished");

        from("direct:logpSingleMolecule")
                .convertBodyTo(MoleculeObject.class)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()"));

        from("direct:atomcount")
                //.process(new MoleculeObjectConverterProcessor())
                .log("atomcount body is ${body}")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("atom_count", "atomCount()"));

        // simple routes that calculates multiple hard coded properties
        from("direct:logp_atomcount_bondcount")
                //.process(new MoleculeObjectConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("logp", "logP()")
                        .calculate("atom_count", "atomCount()")
                        .calculate("bond_count", "bondCount()")
                );
        
        from("direct:lipinski")
                //.process(new MoleculeObjectConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .calculate("mol_weight", "mass()")
                        .calculate("logp", "logP()")
                        .calculate("hbd_count", "donorCount()")
                        .calculate("hba_count", "acceptorCount()")
                );

        // simple route that exemplifies filtering
        from("direct:filter_example")
                //.process(new MoleculeObjectConverterProcessor())
                .process(new ChemAxonMoleculeProcessor()
                        .filter("mass()<400")
                        .filter("ringCount()>0")
                        .filter("rotatableBondCount()<5")
                        .filter("donorCount()<=5")
                        .filter("acceptorCount()<=10")
                        .filter("logP()<4")
                );

        // filedrop service that uses the filter_example to filter the input structures
        // then uses the logp_atomcount_bondcount to calculate some properties 
        from("file:" + base + "screen/filter?antInclude=*.sdf&move=in")
                .log("running filter")
                .to("direct:filter_example")
                .to("direct:logp_atomcount_bondcount")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "screen/filter/out?fileName=${file:name.noext}.sdf");

        // dynamic route that requires the chem terms configuration to be set using
        // the ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property.
        // NOTE: if this is to be used for multiple molecules send then all together
        // as an Iterable<Molecule> or InputStream that can be converted to Iterable<Molecule>
        // get get optimum performance
        from("direct:chemTerms")
                //.process(new MoleculeObjectConverterProcessor())
                .process(new ChemAxonMoleculeProcessor());

        from("direct:chemTermsSingleMolecule")
                //.convertBodyTo(MoleculeObject.class)
                .process(new ChemAxonMoleculeProcessor());

        from("direct:standardize")
                //.convertBodyTo(MoleculeObject.class)
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